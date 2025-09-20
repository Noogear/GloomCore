package gloomcore.contract;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 表示一个可执行的、独立的异步操作单元。该接口实例为可复用“动作”，可被 ActionChain 编排。
 *
 * @param <C> 上下文输入类型 (Context)
 * @param <R> 结果输出类型 (Result)
 */
@FunctionalInterface
public interface Action<C, R> {

    /**
     * 创建忽略输入并异步提供结果的 Action。
     *
     * @param <T>      被忽略的输入类型
     * @param <R>      输出结果类型
     * @param supplier 结果提供者（在线程池中执行）
     * @param executor 执行异步任务的线程池
     * @return 异步提供结果的 Action
     */
    static <T, R> Action<T, R> supplyAsync(Supplier<R> supplier, Executor executor) {
        Objects.requireNonNull(supplier, "Supplier cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return c -> CompletableFuture.supplyAsync(supplier, executor);
    }

    /**
     * 创建执行副作用后原样传递上下文的异步 Action。
     *
     * @param <C>      上下文类型
     * @param runnable 副作用逻辑
     * @param executor 执行副作用的线程池
     * @return C -> C 的异步副作用 Action
     */
    static <C> Action<C, C> runAsync(Runnable runnable, Executor executor) {
        Objects.requireNonNull(runnable, "Runnable cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        return context -> CompletableFuture.runAsync(runnable, executor).thenApply(v -> context);
    }

    /**
     * 创建一个立即完成并返回常量结果的 Action。
     *
     * @param <T>    被忽略的输入类型
     * @param <R>    输出结果类型
     * @param result 常量结果
     * @return 立即完成的常量 Action
     */
    static <T, R> Action<T, R> completed(R result) {
        return c -> CompletableFuture.completedFuture(result);
    }

    /**
     * 以当前 Action 作为起点创建新的链式构建器。
     *
     * @return 新的 ActionChain.Builder
     */
    default ActionChain.Builder<C, R> toChain() {
        return new ActionChain.Builder<>(this);
    }

    /**
     * 执行核心逻辑：
     * 1. 不得返回 null；
     * 2. 可同步 (completedFuture) 或异步完成；
     * 3. 异常通过 CompletableFuture 异常完成返回。
     *
     * @param context 上下文（可为 null）
     * @return 结果 Future
     */
    CompletableFuture<R> execute(@Nullable C context);
}
