package gloomcore.contract.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * 表示接受三个参数并产生结果的函数接口。
 *
 * <p>这是一个函数式接口，其唯一的抽象方法是{@link #apply(Object, Object, Object)}。
 *
 * @param <T> 第一个参数的类型
 * @param <U> 第二个参数的类型
 * @param <V> 第三个参数的类型
 * @param <R> 函数结果的类型
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    /**
     * 将此函数应用于给定的三个参数。
     *
     * @param t 第一个参数
     * @param u 第二个参数
     * @param v 第三个参数
     * @return 函数的结果
     */
    R apply(T t, U u, V v);

    /**
     * 返回一个组合函数，该函数首先应用此函数，然后将结果应用于给定的 after 函数。
     *
     * @param <W>   after 函数的结果类型
     * @param after 在此函数之后应用的函数
     * @return 一个组合函数
     * @throws NullPointerException 如果 after 为 null
     */
    default <W> TriFunction<T, U, V, W> andThen(Function<? super R, ? extends W> after) {
        Objects.requireNonNull(after);
        return (t, u, v) -> after.apply(this.apply(t, u, v));
    }
}