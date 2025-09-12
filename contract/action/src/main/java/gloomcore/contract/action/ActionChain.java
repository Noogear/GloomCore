package gloomcore.contract.action;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * 一个统一的、类型安全的动作链，无缝支持同步和异步操作。
 * 整个链条在内部基于 CompletableFuture 构建，执行总是非阻塞的。
 *
 * @param <C> 初始上下文的类型
 * @param <R> 最终结果的类型
 */
public class ActionChain<C, R> implements Action<C, R> {

    private final Function<C, CompletableFuture<R>> composedAction;

    private ActionChain(Function<C, CompletableFuture<R>> composedAction) {
        this.composedAction = composedAction;
    }

    /**
     * 创建一个代表“空”链的初始函数，它只是将输入原样传递下去。
     * 这个方法解决了构造函数模糊的问题。
     */
    private static <T> Function<T, CompletableFuture<T>> createInitialChain() {
        return CompletableFuture::completedFuture;
    }

    /**
     * 获取一个新的建造者实例来开始构建链条。
     */
    public static <T> Builder<T, T> newBuilder() {
        // 现在调用私有方法，不再有歧义
        return new Builder<>(createInitialChain());
    }


    /**
     * 启动并执行整个动作链。
     * 总是返回一个 CompletableFuture，即使链条中只包含同步操作。
     *
     * @param initialContext 链条的初始输入
     * @return 一个代表链条最终结果的 CompletableFuture
     */
    @Override
    public CompletableFuture<R> execute(@Nullable C initialContext) {
        return composedAction.apply(initialContext);
    }

    /**
     * ActionChain 的建造者。
     *
     * @param <C> 初始上下文的类型
     * @param <R> 当前链条最后一个动作的输出类型
     */
    public static class Builder<C, R> {

        private final Function<C, CompletableFuture<R>> currentChain;

        private Builder(Function<C, CompletableFuture<R>> currentChain) {
            this.currentChain = currentChain;
        }

        Builder(Action<C, R> initialAction) {
            this.currentChain = initialAction::execute;
        }

        /**
         * 添加一个同步动作。
         * 该函数将在前一个阶段完成后，在同一个线程或默认的ForkJoinPool线程中执行。
         *
         * @param syncAction 同步的转换函数 (T -> U)
         * @return 新的建造者
         */
        public <NR> Builder<C, NR> addSync(Function<R, NR> syncAction) {
            Objects.requireNonNull(syncAction, "Sync action cannot be null.");
            return new Builder<>(currentChain.andThen(future -> future.thenApply(syncAction)));
        }

        /**
         * 在指定的 Executor 上异步地添加一个同步动作。
         *
         * @param syncAction 同步的转换函数 (T -> U)
         * @param executor   用于执行该同步函数的线程池
         * @return 新的建造者
         */
        public <NR> Builder<C, NR> addSync(Function<R, NR> syncAction, Executor executor) {
            Objects.requireNonNull(syncAction, "Sync action cannot be null.");
            Objects.requireNonNull(executor, "Executor cannot be null.");
            return new Builder<>(currentChain.andThen(future -> future.thenApplyAsync(syncAction, executor)));
        }

        /**
         * 添加一个异步动作。
         *
         * @param asyncAction 异步的转换函数 (T -> CompletableFuture<U>)
         * @return 新的建造者
         */
        public <NR> Builder<C, NR> addAsync(Function<R, CompletableFuture<NR>> asyncAction) {
            Objects.requireNonNull(asyncAction, "Async action cannot be null.");
            return new Builder<>(currentChain.andThen(future -> future.thenCompose(asyncAction)));
        }

        /**
         * 在指定的 Executor 上调度并执行一个异步动作。
         *
         * @param asyncAction 异步的转换函数 (T -> CompletableFuture<U>)
         * @param executor    用于调度该异步函数的线程池
         * @return 新的建造者
         */
        public <NR> Builder<C, NR> addAsync(Function<R, CompletableFuture<NR>> asyncAction, Executor executor) {
            Objects.requireNonNull(asyncAction, "Async action cannot be null.");
            Objects.requireNonNull(executor, "Executor cannot be null.");
            return new Builder<>(currentChain.andThen(future -> future.thenComposeAsync(asyncAction, executor)));
        }

        /**
         * 添加一个预先定义好的、类型安全的 Action 实例。
         * 这是将可复用业务逻辑块组合进链条的核心方法。
         *
         * @param action 一个 Action<R, NR> 实例，它的输入类型 R 必须匹配当前链的输出类型 R。
         * @param <NR>   该 Action 的输出类型，也将成为链条新的输出类型。
         * @return 新的建造者
         */
        public <NR> Builder<C, NR> addAction(Action<R, NR> action) {
            Objects.requireNonNull(action, "Action cannot be null.");
            // action.execute 的签名完美匹配 addAsync 所需的函数签名
            return this.addAsync(action::execute);
        }

        public Builder<C, R> onError(Function<Throwable, R> recoveryFunction) {
            Function<C, CompletableFuture<R>> newChain = currentChain.andThen(
                    future -> future.exceptionally(recoveryFunction)
            );
            return new Builder<>(newChain);
        }

        /**
         * 完成构建，返回一个可执行的 ActionChain 实例。
         */
        public ActionChain<C, R> build() {
            return new ActionChain<>(currentChain);
        }
    }
}
