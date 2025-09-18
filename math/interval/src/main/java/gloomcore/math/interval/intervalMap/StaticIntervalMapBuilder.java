package gloomcore.math.interval.intervalMap;

import gloomcore.math.interval.Interval;
import gloomcore.math.interval.IntervalQueryEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 为构建静态的、只读优化的区间查询引擎提供通用逻辑的抽象基类。
 * <p>
 * <b>重要提示:</b>
 * 此构建器假定用户提供的区间是互不重叠的。任何区间的重叠解析逻辑都应在
 * 使用此构建器之前完成（例如，通过先使用 {@link BaseIntervalMap} 进行数据预处理）。
 *
 * @param <T> 区间关联的值的类型。
 * @param <B> 具体的 Builder 子类型，用于支持链式调用。
 */
public abstract class StaticIntervalMapBuilder<T, B extends StaticIntervalMapBuilder<T, B>> {

    protected final List<Interval<T>> intervals = new ArrayList<>();

    /**
     * 添加一个区间到构建器中。
     *
     * @param start 区间的起始点（包含）。
     * @param end   区间的结束点（包含）。
     * @param value 与该区间关联的值。
     * @return Builder 实例，用于链式调用。
     */
    @SuppressWarnings("unchecked")
    public B put(int start, int end, T value) {
        intervals.add(new Interval<>(start, end, value));
        return (B) this;
    }

    /**
     * 按照区间的起始点对已收集的区间进行排序。这是构建静态查询引擎的必要步骤。
     */
    protected void sortIntervals() {
        if (!intervals.isEmpty()) {
            intervals.sort(Comparator.comparingInt(Interval::start));
        }
    }

    /**
     * 构建最终的、不可变的、为查询优化的 {@link IntervalQueryEngine}。
     *
     * @return 一个静态查询引擎的实例。
     */
    public abstract IntervalQueryEngine<T> build();
}
