package gloomcore.math.interval;

/**
 * 代表一个不可变的整数区间 [start, end]，并关联一个泛型值。
 * <p>
 * 这是一个作为所有查询引擎实现之间标准数据交换格式的共享模型。
 * 该类是线程安全的。
 *
 * @param start 区间的起始点（包含）。
 * @param end   区间的结束点（包含）。
 * @param value 与该区间关联的数据。
 * @param <T>   关联数据的类型。
 */
public record Interval<T>(int start, int end, T value) {
    public Interval {
        if (start > end) {
            throw new IllegalArgumentException("Interval start cannot be greater than end.");
        }
    }

    /**
     * 检查给定的点是否位于此区间内（包含边界）。
     *
     * @param point 要检查的整数点。
     * @return 如果点在区间内，则为 true；否则为 false。
     */
    public boolean contains(int point) {
        return point >= start && point <= end;
    }
}
