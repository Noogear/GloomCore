package gloomcore.paper.contract.function;

import java.util.Objects;

/**
 * 表示一个接受三个参数并返回 boolean 值的谓词函数接口。
 *
 * <p>这是一个函数式接口，其唯一的抽象方法是{@link #test(Object, Object, Object)}。
 *
 * @param <T> 第一个参数的类型
 * @param <U> 第二个参数的类型
 * @param <V> 第三个参数的类型
 */
@FunctionalInterface
public interface TriPredicate<T, U, V> {
    /**
     * 对给定的三个参数执行谓词测试。
     *
     * @param t 第一个参数
     * @param u 第二个参数
     * @param v 第三个参数
     * @return 如果参数满足谓词条件则返回 true，否则返回 false
     */
    boolean test(T t, U u, V v);

    /**
     * 返回一个组合谓词，该谓词表示此谓词与另一个谓词的逻辑与操作。
     *
     * @param other 将与此谓词进行逻辑与操作的另一个谓词
     * @return 表示此谓词与另一个谓词逻辑与操作的组合谓词
     * @throws NullPointerException 如果 other 为 null
     */
    default TriPredicate<T, U, V> and(TriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (t, u, v) -> this.test(t, u, v) && other.test(t, u, v);
    }

    /**
     * 返回表示此谓词逻辑非的谓词。
     *
     * @return 表示此谓词逻辑非的谓词
     */
    default TriPredicate<T, U, V> negate() {
        return (t, u, v) -> !this.test(t, u, v);
    }

    /**
     * 返回一个组合谓词，该谓词表示此谓词与另一个谓词的逻辑或操作。
     *
     * @param other 将与此谓词进行逻辑或操作的另一个谓词
     * @return 表示此谓词与另一个谓词逻辑或操作的组合谓词
     * @throws NullPointerException 如果 other 为 null
     */
    default TriPredicate<T, U, V> or(TriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (t, u, v) -> this.test(t, u, v) || other.test(t, u, v);
    }
}