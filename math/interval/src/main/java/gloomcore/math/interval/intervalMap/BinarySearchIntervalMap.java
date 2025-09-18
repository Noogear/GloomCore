package gloomcore.math.interval.intervalMap;

import gloomcore.math.interval.Interval;
import gloomcore.math.interval.IntervalQueryEngine;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 一个只读优化的区间查询引擎，基于排序数组和二分查找算法。
 * <p>
 * <b>性能特点:</b>
 * <ul>
 *   <li>查询: O(log n)。由于数组在内存中的连续性，缓存局部性极好，实际性能常数因子很小。</li>
 *   <li>内存: O(n)。内存占用低，仅存储区间数据本身。</li>
 *   <li>构建: O(n log n)，主要开销在排序。</li>
 * </ul>
 * <b>最佳场景:</b>
 * 适用于“一次构建，海量查询”的场景，是性能和内存消耗之间最均衡的选择。
 *
 * @param <T> 区间关联的值的类型。
 */
public final class BinarySearchIntervalMap<T> implements IntervalQueryEngine<T> {

    private static final Comparator<Interval<?>> COMPARATOR = Comparator.comparingInt(Interval::start);
    private final Interval<T>[] intervals;

    private BinarySearchIntervalMap(Interval<T>[] intervals) {
        this.intervals = intervals;
    }

    @Override
    public Interval<T> getInterval(int point) {
        int index = Arrays.binarySearch(intervals, new Interval<>(point, point, null), COMPARATOR);
        if (index < 0) {
            index = -index - 2;
        }
        if (index >= 0 && intervals[index].contains(point)) {
            return intervals[index];
        }
        return null;
    }

    @Override
    public T getValue(int point) {
        Interval<T> interval = getInterval(point);
        return (interval != null) ? interval.value() : null;
    }

    /**
     * {@link BinarySearchIntervalMap} 的构建器。
     */
    public static class Builder<T> extends StaticIntervalMapBuilder<T, Builder<T>> {
        @Override
        public BinarySearchIntervalMap<T> build() {
            sortIntervals();
            @SuppressWarnings("unchecked")
            Interval<T>[] builtIntervals = intervals.toArray(new Interval[0]);
            return new BinarySearchIntervalMap<>(builtIntervals);
        }
    }
}
