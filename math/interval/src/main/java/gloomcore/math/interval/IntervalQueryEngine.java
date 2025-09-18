package gloomcore.math.interval;

/**
 * 定义了所有整数区间查询引擎必须实现的公共接口。
 * <p>
 * 该接口的核心功能是将一个整数点映射到一个包含它的区间，并检索与该区间关联的数据。
 * 实现类可以是动态的（可修改）或静态的（只读优化）。
 *
 * @param <T> 区间关联的值的类型。
 */
public interface IntervalQueryEngine<T> {

    /**
     * 根据一个整数点，查找并返回其所在区间关联的值。
     *
     * @param point 要查询的整数点。
     * @return 如果找到匹配的区间，则返回其关联的值；否则返回 null。
     */
    T getValue(int point);

    /**
     * 根据一个整数点，查找并返回其所在的完整 {@link Interval} 对象。
     *
     * @param point 要查询的整数点。
     * @return 如果找到匹配的区间，则返回该 {@code Interval} 对象；否则返回 null。
     */
    Interval<T> getInterval(int point);
}
