package gloomcore.contract.action;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 表示一个可执行的、独立的异步操作单元。
 * 这个接口的实例代表一个可复用的“动作”，可以被 ActionChain 编排。
 *
 * @param <C> 操作的输入上下文类型 (Context)
 *            <R> 操作的输出结果类型 (Result)
 */
@FunctionalInterface
public interface Action<C, R> {

    /**
     * 创建一个 Action，它忽略输入，并异步地提供一个 R 类型的新值。
     *
     * @param <T> 输入类型 (将被忽略)
     * @param <R> 输出类型
     */
    static <T, R> Action<T, R> supplyAsync(Supplier<R> supplier, Executor executor) {
        Objects.requireNonNull(supplier, "Supplier cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return _ -> CompletableFuture.supplyAsync(supplier, executor);
    }

    /**
     * 创建一个 Action，它执行一个副作用 (Runnable)，然后将原始输入上下文 C 不变地传递下去。
     * 这是一个 C -> C 的 Action。
     *
     * @param <C> 上下文类型 (输入和输出相同)
     */
    static <C> Action<C, C> runAsync(Runnable runnable, Executor executor) {
        Objects.requireNonNull(runnable, "Runnable cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return context -> CompletableFuture.runAsync(runnable, executor).thenApply(v -> context);
    }

    /**
     * 创建一个 Action，它忽略输入，并立即返回一个已知的 R 类型的值。
     *
     * @param <T> 输入类型 (将被忽略)
     * @param <R> 输出类型
     */
    static <T, R> Action<T, R> completed(R result) {
        return _ -> CompletableFuture.completedFuture(result);
    }

    /**
     * 将此 Action 作为第一步，开始构建一个新的 ActionChain。
     * 这提供了一种流畅的方式来从一个已有的 Action 扩展出一个更复杂的流程。
     *
     * @return 一个新的 ActionChain.Builder，其初始状态就是执行当前的 Action。
     */
    default ActionChain.Builder<C, R> toChain() {
        return new ActionChain.Builder<>(this);
    }

    /**
     * 执行此 Action 的核心逻辑。
     */
    CompletableFuture<R> execute(@Nullable C context);
}
