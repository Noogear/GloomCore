package gloomcore.contract;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * 一个统一的、类型安全的动作链，无缝支持同步和异步操作。\n * 整个链条在内部基于 {@link CompletableFuture} 构建，执行总是非阻塞的。
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
     *
     * @param <T> 初始与返回的占位泛型类型
     * @return 一个将输入包装为已完成 {@code CompletableFuture} 的函数
     */
    private static <T> Function<T, CompletableFuture<T>> createInitialChain() {
        return CompletableFuture::completedFuture;
    }

    /**
     * 获取一个新的建造者实例来开始构建链条。
     *
     * @param <T> 初始上下文与当前输出的类型（链刚开始时输入输出相同）
     * @return 新的 Builder 实例
     */
    public static <T> Builder<T, T> newBuilder() {
        return new Builder<>(createInitialChain());
    }

    /**
     * 启动并执行整个动作链。\n * 总是返回一个 {@code CompletableFuture}，即使链条中只包含同步操作。
     *
     * @param initialContext 链条的初始输入（可为 null，具体取决于链内动作是否接受）
     * @return 代表链条最终结果的 {@code CompletableFuture}
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
         * 添加一个同步动作。\n * 在前一个阶段完成后于相同线程（或默认 ForkJoinPool）执行。
         *
         * @param <NR>       新结果类型
         * @param syncAction 同步转换函数，接收上一步结果 {@code R}，返回新结果 {@code NR}
         * @return 新的 Builder，其输出类型更新为 {@code NR}
         */
        public <NR> Builder<C, NR> addSync(Function<R, NR> syncAction) {
            Objects.requireNonNull(syncAction, "Sync action cannot be null.");
            return new Builder<>(currentChain.andThen(future -> future.thenApply(syncAction)));
        }

        /**
         * 在指定的线程池上异步地添加一个同步动作。\n * 函数本身仍然是同步的，只是调度到指定 {@link Executor}。
         *
         * @param <NR>       新结果类型
         * @param syncAction 同步转换函数
         * @param executor   执行该同步函数的线程池
         * @return 新的 Builder，其输出类型更新为 {@code NR}
         */
        public <NR> Builder<C, NR> addSync(Function<R, NR> syncAction, Executor executor) {
            Objects.requireNonNull(syncAction, "Sync action cannot be null.");
            Objects.requireNonNull(executor, "Executor cannot be null.");
            return new Builder<>(currentChain.andThen(future -> future.thenApplyAsync(syncAction, executor)));
        }

        /**
         * 添加一个异步动作。\n * 上一步的结果 {@code R} 经 {@code asyncAction} 转换为 {@code CompletableFuture<NR>} 并扁平化。
         *
         * @param <NR>        新结果类型
         * @param asyncAction 异步转换函数（签名：{@code R -> CompletableFuture<NR>}}）
         * @return 新的 Builder，其输出类型更新为 {@code NR}
         */
        public <NR> Builder<C, NR> addAsync(Function<R, CompletableFuture<NR>> asyncAction) {
            Objects.requireNonNull(asyncAction, "Async action cannot be null.");
            return new Builder<>(currentChain.andThen(future -> future.thenCompose(asyncAction)));
        }

        /**
         * 在指定 {@link Executor} 上调度并执行一个异步动作。
         *
         * @param <NR>        新结果类型
         * @param asyncAction 异步转换函数（签名：{@code R -> CompletableFuture<NR>}}）
         * @param executor    用于调度该异步函数的线程池
         * @return 新的 Builder，其输出类型更新为 {@code NR}
         */
        public <NR> Builder<C, NR> addAsync(Function<R, CompletableFuture<NR>> asyncAction, Executor executor) {
            Objects.requireNonNull(asyncAction, "Async action cannot be null.");
            Objects.requireNonNull(executor, "Executor cannot be null.");
            return new Builder<>(currentChain.andThen(future -> future.thenComposeAsync(asyncAction, executor)));
        }

        /**
         * 添加一个预先定义好的、类型安全的 {@link Action} 实例。\n * 可复用业务逻辑块的组合方式。
         *
         * @param <NR>   新结果类型
         * @param action 一个 {@code Action<R, NR>} 实例，它的输入类型 {@code R} 必须匹配当前链的输出类型
         * @return 新的 Builder，其输出类型更新为 {@code NR}
         */
        public <NR> Builder<C, NR> addAction(Action<R, NR> action) {
            Objects.requireNonNull(action, "Action cannot be null.");
            return this.addAsync(action::execute);
        }

        /**
         * 为当前链条添加异常恢复逻辑。\n * 若之前阶段出现异常，将以提供的函数生成“补偿”结果并继续成功完成。
         *
         * @param recoveryFunction 异常 -> 恢复结果 {@code R} 的映射函数
         * @return 带有异常恢复能力的新 Builder（输出类型不变）
         */
        public Builder<C, R> onError(Function<Throwable, R> recoveryFunction) {
            Function<C, CompletableFuture<R>> newChain = currentChain.andThen(
                    future -> future.exceptionally(recoveryFunction)
            );
            return new Builder<>(newChain);
        }

        /**
         * 完成构建，返回一个可执行的 {@link ActionChain} 实例。
         *
         * @return 构建完成的 ActionChain
         */
        public ActionChain<C, R> build() {
            return new ActionChain<>(currentChain);
        }
    }
}
