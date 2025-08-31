package gloomcore.contract.action;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 表示一个可执行的异步操作的函数式接口。
 * <p>
 * Action接口定义了一个可以异步执行的操作，该操作接收一个上下文对象并返回一个CompletableFuture，
 * 该CompletableFuture包含处理后的上下文结果。
 *
 * @param <C> 操作上下文的类型
 */
@FunctionalInterface
public interface Action<C> {

    /**
     * 创建一个Action，该Action将使用给定的Supplier异步提供上下文。
     * <p>
     * 注意：此 Action 会忽略执行时传入的任何现有上下文，并由 Supplier 提供一个新的上下文。
     *
     * @param supplier 用于提供上下文的Supplier，不能为空
     * @param executor 用于异步执行的Executor，不能为空
     * @param <C>      上下文类型
     * @return 新创建的Action
     */
    static <C> Action<C> supplyAsync(Supplier<C> supplier, Executor executor) {
        Objects.requireNonNull(supplier, "Supplier cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return context -> CompletableFuture.supplyAsync(supplier, executor);
    }

    /**
     * 创建一个Action，该Action将使用给定的Runnable异步执行操作。
     * <p>
     * 注意：此 Action 会在执行完 Runnable 后，保留并传递执行时传入的原始上下文。
     *
     * @param runnable 要执行的Runnable，不能为空
     * @param executor 用于异步执行的Executor，不能为空
     * @param <C>      上下文类型
     * @return 新创建的Action
     */
    static <C> Action<C> runAsync(Runnable runnable, Executor executor) {
        Objects.requireNonNull(runnable, "Runnable cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return context -> CompletableFuture.runAsync(runnable, executor).thenApply(v -> context);
    }

    /**
     * 创建一个已完成的Action，该Action直接返回给定的上下文。
     * 这是 {@link #completed(Object)} 的别名。
     *
     * @param context 要返回的上下文
     * @param <C>     上下文类型
     * @return 新创建的已完成的Action
     */
    static <C> Action<C> of(C context) {
        return completed(context);
    }

    /**
     * 创建一个已完成的Action，该Action直接返回给定的上下文。
     *
     * @param context 要返回的上下文
     * @param <C>     上下文类型
     * @return 新创建的已完成的Action
     */
    static <C> Action<C> completed(C context) {
        return ctx -> CompletableFuture.completedFuture(context);
    }

    /**
     * 执行此 Action 的核心逻辑。
     * 通常，你不需要直接调用此方法，而是通过构建链并调用 {@link #launch(Object)} 来执行。
     */
    CompletableFuture<C> execute(C context);

    /**
     * 返回一个新Action，该Action在当前Action执行完成后对结果应用给定的函数。
     *
     * @param function 要应用的函数，不能为空
     * @return 组合后的Action
     */
    default Action<C> thenApply(Function<C, C> function) {
        Objects.requireNonNull(function, "Function cannot be null.");
        return context -> this.execute(context).thenApply(function);
    }

    /**
     * 返回一个新Action，该Action在当前Action执行完成后对结果执行给定的消费操作。
     *
     * @param consumer 要执行的消费操作，不能为空
     * @return 组合后的Action
     */
    default Action<C> thenAccept(Consumer<C> consumer) {
        Objects.requireNonNull(consumer, "Consumer cannot be null.");
        return context -> this.execute(context).thenAccept(consumer).thenApply(v -> context);
    }

    /**
     * 返回一个新 Action，它会在当前 Action 成功后，执行下一个 Action。
     * 这适用于将两个返回 Action 的操作链接在一起。
     *
     * @param nextAction 要执行的下一个 Action，不能为空
     * @return 组合后的 Action
     */
    default Action<C> thenChain(Action<C> nextAction) {
        Objects.requireNonNull(nextAction, "Next Action cannot be null.");
        return context -> this.execute(context).thenCompose(nextAction::execute);
    }

    /**
     * 返回一个新Action，该Action在当前Action执行完成后，使用指定的执行器异步地对结果应用给定的函数。
     *
     * @param function 要应用的函数，不能为空
     * @param executor 用于异步执行的Executor，不能为空
     * @return 组合后的Action
     */
    default Action<C> thenApplyAsync(Function<C, C> function, Executor executor) {
        Objects.requireNonNull(function, "Function cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return context -> this.execute(context).thenApplyAsync(function, executor);
    }

    /**
     * 返回一个新Action，该Action在当前Action执行完成后，使用指定的执行器异步地对结果执行给定的消费操作。
     *
     * @param consumer 要执行的消费操作，不能为空
     * @param executor 用于异步执行的Executor，不能为空
     * @return 组合后的Action
     */
    default Action<C> thenAcceptAsync(Consumer<C> consumer, Executor executor) {
        Objects.requireNonNull(consumer, "Consumer cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return context -> this.execute(context).thenAcceptAsync(consumer, executor).thenApply(v -> context);
    }

    /**
     * 返回一个新 Action，它会在当前 Action 成功后，使用指定的执行器异步执行下一个 Action。
     *
     * @param nextAction 要执行的下一个 Action，不能为空
     * @param executor   用于异步执行的 Executor，不能为空
     * @return 组合后的 Action
     */
    default Action<C> thenChainAsync(Action<C> nextAction, Executor executor) {
        Objects.requireNonNull(nextAction, "Next Action cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return context -> this.execute(context).thenComposeAsync(nextAction::execute, executor);
    }


    /**
     * 注册一个异常处理器，用于从失败中恢复。
     * 如果当前 Action 执行失败，该处理器会被调用，并使用其返回的备用值作为新的成功结果，
     * 使得操作链可以继续成功地执行下去。
     *
     * @param recoveryFunction 用于处理异常并返回备用值的函数
     * @return 一个新的、增加了恢复逻辑的 Action
     */
    default Action<C> recover(Function<Throwable, C> recoveryFunction) {
        Objects.requireNonNull(recoveryFunction, "Recovery function cannot be null.");
        return context -> this.execute(context).exceptionally(recoveryFunction);
    }

    /**
     * 注册一个当 Action 执行失败时触发的回调（副作用）。
     * 这个方法用于执行如记录日志等操作。它不会改变 Action 的失败状态，
     * 异常会继续向下传播，直到被 {@code recover} 或 {@code launch} 处理。
     *
     * @param errorConsumer 接收异常并执行操作的消费者
     * @return 一个新的、增加了失败回调的 Action
     */
    default Action<C> onError(Consumer<Throwable> errorConsumer) {
        Objects.requireNonNull(errorConsumer, "Error consumer cannot be null.");
        return context -> this.execute(context).whenComplete((result, ex) -> {
            if (ex != null) {
                errorConsumer.accept(ex);
            }
        });
    }

    /**
     * 返回一个新的 Action，如果当前 Action 在指定的超时时间内未完成，
     * 它将以 {@link TimeoutException} 异常结束。
     *
     * @param timeout 超时时长
     * @param unit    时间单位
     * @return 增加了超时逻辑的新 Action
     */
    default Action<C> orTimeout(long timeout, TimeUnit unit) {
        Objects.requireNonNull(unit, "Time unit cannot be null.");
        return context -> this.execute(context).orTimeout(timeout, unit);
    }

    /**
     * 启动此操作链并执行，使用指定的错误消费者进行异常处理。
     *
     * @param initialContext 初始上下文，作为操作链的起始输入
     * @param errorConsumer  用于处理执行过程中发生的异常的消费者函数
     * @return 包含最终结果的 CompletableFuture。如果发生未恢复的异常，结果将为 null。
     */
    default CompletableFuture<C> launch(@Nullable C initialContext, BiConsumer<String, Throwable> errorConsumer) {
        return this.execute(initialContext)
                .exceptionally(ex -> {
                    errorConsumer.accept("Action chain failed with an unhandled exception:", ex);
                    return null;
                });
    }

    /**
     * 启动此操作链并执行，使用一个通用的安全异常处理器。
     *
     * @param initialContext 初始上下文
     * @return 包含最终结果的 CompletableFuture。如果发生未恢复的异常，结果将为 null。
     */
    default CompletableFuture<C> launch(@Nullable C initialContext) {
        return this.execute(initialContext)
                .exceptionally(ex -> {
                    System.err.println("Action chain failed with an unhandled exception (stack trace follows):");
                    ex.printStackTrace();
                    return null;
                });
    }
}
