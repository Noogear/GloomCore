package gloomcore.api.function;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * 表示接受三个参数且不返回结果的操作。
 *
 * <p>这是一个函数式接口，其唯一的抽象方法是{@link #accept(Object, Object, Object)}。
 * 该接口可以用于需要对三个对象执行操作但不返回结果的场景。
 *
 * @param <T> 第一个参数的类型
 * @param <U> 第二个参数的类型
 * @param <V> 第三个参数的类型
 */
@NullMarked
@FunctionalInterface
public interface TriConsumer<T, U, V> {
    /**
     * 对给定的三个参数执行此操作。
     *
     * @param t 第一个参数
     * @param u 第二个参数
     * @param v 第三个参数
     */
    void accept(T t, U u, V v);

    /**
     * 返回一个组合的 TriConsumer，按顺序执行当前操作和指定的 after 操作。
     * 如果 after 操作抛出异常，它将被转发给调用者。
     *
     * @param after 在当前操作之后执行的操作
     * @return 一个组合的 TriConsumer
     * @throws NullPointerException 如果 after 为 null
     */
    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);
        return (t, u, v) -> {
            this.accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}