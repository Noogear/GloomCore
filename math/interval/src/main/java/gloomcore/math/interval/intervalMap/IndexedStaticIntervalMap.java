package gloomcore.math.interval.intervalMap;

import gloomcore.math.interval.Interval;
import gloomcore.math.interval.IntervalQueryEngine;

/**
 * 一个为极限查询性能设计的只读区间查询引擎，采用分桶索引（lookup table）机制。
 * <p>
 * <b>性能特点:</b>
 * <ul>
 *   <li>查询: 近似 O(1)。通过索引直接定位到小范围，然后进行极短的线性扫描。</li>
 *   <li>内存: O(n + M/B)，其中 M 是整数总跨度 (maxEnd - minStart)，B 是桶大小。</li>
 *   <li>构建: O(n log n + M/B)。</li>
 * </ul>
 * <b>最佳场景:</b>
 * 对查询延迟有最苛刻要求的系统（如高频交易、网络包过滤）。
 * <b>注意:</b> 当区间数据分布极其稀疏时（M 值巨大），内存消耗会变得非常高。
 *
 * @param <T> 区间关联的值的类型。
 */
public final class IndexedStaticIntervalMap<T> implements IntervalQueryEngine<T> {

    private final Interval<T>[] intervals;
    private final int[] lookupIndex;
    private final int minPoint;
    private final int blockShift;

    private IndexedStaticIntervalMap(Interval<T>[] intervals, int[] lookupIndex, int minPoint, int blockShift) {
        this.intervals = intervals;
        this.lookupIndex = lookupIndex;
        this.minPoint = minPoint;
        this.blockShift = blockShift;
    }

    @Override
    public Interval<T> getInterval(int point) {
        if (point < minPoint || intervals.length == 0) return null;
        int blockIndex = (point - minPoint) >> blockShift;
        if (blockIndex >= lookupIndex.length) {
            Interval<T> lastInterval = intervals[intervals.length - 1];
            return lastInterval.contains(point) ? lastInterval : null;
        }
        int searchStartIndex = lookupIndex[blockIndex];
        for (int i = searchStartIndex; i < intervals.length; i++) {
            Interval<T> current = intervals[i];
            if (current.start() > point) {
                if (i > 0 && intervals[i - 1].contains(point)) return intervals[i - 1];
                return null;
            }
            if (current.contains(point)) return current;
        }
        return null;
    }

    @Override
    public T getValue(int point) {
        Interval<T> interval = getInterval(point);
        return (interval != null) ? interval.value() : null;
    }

    /**
     * {@link IndexedStaticIntervalMap} 的构建器。
     */
    public static class Builder<T> extends StaticIntervalMapBuilder<T, Builder<T>> {
        private int blockSize = 4096;

        public Builder<T> withBlockSize(int size) {
            // ... 逻辑保持不变 ...
            if (size <= 0 || (size & (size - 1)) != 0)
                throw new IllegalArgumentException("Block size must be a positive power of two.");
            this.blockSize = size;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public IndexedStaticIntervalMap<T> build() {
            if (intervals.isEmpty()) return new IndexedStaticIntervalMap<T>(new Interval[0], new int[0], 0, 0);
            sortIntervals();
            Interval<T>[] builtIntervals = intervals.toArray(new Interval[0]);
            int min = builtIntervals[0].start();
            int max = 0;
            for (Interval<T> interval : builtIntervals) if (interval.end() > max) max = interval.end();
            int shift = Integer.numberOfTrailingZeros(blockSize);
            int indexSize = ((max - min) >> shift) + 1;
            int[] index = new int[indexSize];
            int intervalIdx = 0;
            for (int i = 0; i < indexSize; i++) {
                int blockStartPoint = min + (i << shift);
                while (intervalIdx < builtIntervals.length - 1 && builtIntervals[intervalIdx].start() < blockStartPoint)
                    intervalIdx++;
                while (intervalIdx > 0 && builtIntervals[intervalIdx].start() > blockStartPoint) intervalIdx--;
                index[i] = intervalIdx;
            }
            return new IndexedStaticIntervalMap<>(builtIntervals, index, min, shift);
        }
    }
}
