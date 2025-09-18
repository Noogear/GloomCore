package gloomcore.math.interval.intervalMap;

import gloomcore.math.interval.Interval;
import gloomcore.math.interval.IntervalQueryEngine;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 一个动态的、可修改的区间地图实现，基于 {@link TreeMap} (红黑树)。
 * <p>
 * <b>性能特点:</b>
 * <ul>
 *   <li>查询 (getValue/getInterval): O(log n)</li>
 *   <li>插入/覆盖 (put): O(log n)</li>
 * </ul>
 * <b>最佳场景:</b>
 * 当需要频繁地添加、删除或修改区间，并且需要处理动态重叠时，这是理想的选择。
 * 例如，用于构建和预处理需要提交给静态查询引擎的数据。
 *
 * @param <T> 区间关联的值的类型。
 */
public class BaseIntervalMap<T> implements IntervalQueryEngine<T> {

    private final NavigableMap<Integer, Interval<T>> intervalsByStart = new TreeMap<>();

    /**
     * 插入一个新区间。该方法会自动处理与现有区间的重叠，新区间会覆盖所有重叠部分。
     *
     * @param start 区间的起始点（包含）。
     * @param end   区间的结束点（包含）。
     * @param value 与该区间关联的值。
     */
    public void put(int start, int end, T value) {
        // ... 逻辑保持不变 ...
        if (start > end) throw new IllegalArgumentException("Interval start cannot be greater than end.");
        Map.Entry<Integer, Interval<T>> startFloorEntry = intervalsByStart.floorEntry(start);
        if (startFloorEntry != null) {
            Interval<T> floorInterval = startFloorEntry.getValue();
            if (floorInterval.end() >= start) {
                intervalsByStart.remove(floorInterval.start());
                if (floorInterval.start() < start)
                    intervalsByStart.put(floorInterval.start(), new Interval<>(floorInterval.start(), start - 1, floorInterval.value()));
            }
        }
        Map.Entry<Integer, Interval<T>> endFloorEntry = intervalsByStart.floorEntry(end);
        if (endFloorEntry != null) {
            Interval<T> floorInterval = endFloorEntry.getValue();
            if (floorInterval.end() > end) {
                intervalsByStart.remove(floorInterval.start());
                intervalsByStart.put(end + 1, new Interval<>(end + 1, floorInterval.end(), floorInterval.value()));
            }
        }
        intervalsByStart.subMap(start, true, end, true).clear();
        intervalsByStart.put(start, new Interval<>(start, end, value));
    }

    @Override
    public Interval<T> getInterval(int point) {
        Map.Entry<Integer, Interval<T>> entry = intervalsByStart.floorEntry(point);
        return (entry != null && entry.getValue().contains(point)) ? entry.getValue() : null;
    }

    @Override
    public T getValue(int point) {
        Interval<T> interval = getInterval(point);
        return (interval != null) ? interval.value() : null;
    }
}
