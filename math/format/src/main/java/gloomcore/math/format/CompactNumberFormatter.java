package gloomcore.math.format;

import java.util.Map;
import java.util.TreeMap;

/**
 * 一个高性能、线程安全的格式化器，用于将数字转换为紧凑且易于人类阅读的字符串。
 * <p>
 * 该类通过将大数值缩放到预定义的阈值并附加相应单位，来实现格式化。
 * 例如，它可以将 {@code 15500} 格式化为 {@code "15.5K"} (当精度为1时)。
 *
 * <h3>基本用法</h3>
 * <pre>{@code
 * Map<Double, String> config = Map.of(
 *     1000.0, "K",
 *     1_000_000.0, "M",
 *     1_000_000_000.0, "G"
 * );
 *
 * // 通过静态工厂方法创建实例
 * CompactNumberFormatter formatter = CompactNumberFormatter.of(config);
 *
 * // 进行格式化
 * System.out.println(formatter.format(1, 2567.0));      // 输出: 2.6K
 * System.out.println(formatter.format(2, 1234567.0));  // 输出: 1.23M
 * System.out.println(formatter.format(0, 987));         // 输出: 987
 * }</pre>
 *
 */
public class CompactNumberFormatter {
    private static final long[] POW10_CACHE = new long[18];
    private static final char[] DIGITS = "0123456789".toCharArray();

    static {
        long value = 1;
        for (int i = 0; i < POW10_CACHE.length; i++) {
            POW10_CACHE[i] = value;
            value *= 10;
        }
    }

    private final double[] thresholds;
    private final char[][] units;
    private final int maxUnitIndex;
    private final ThreadLocal<StringBuilder> buffer;

    /**
     * 私有构造函数，强制通过静态工厂方法创建实例。
     *
     * @param thresholds 有序的阈值数组。
     * @param units      对应的单位二维字符数组。
     */
    private CompactNumberFormatter(double[] thresholds, char[][] units) {
        this.thresholds = thresholds;
        this.units = units;
        this.maxUnitIndex = thresholds.length - 1;
        this.buffer = ThreadLocal.withInitial(() -> new StringBuilder(32));
    }

    /**
     * 通过Map创建格式化器实例的静态工厂方法。
     *
     * @param configuration 一个Map，其键(Key)是阈值(double)，值(Value)是单位(String)。
     * @return 一个新的 NumberQuantizeFinal 实例。
     */
    public static CompactNumberFormatter of(Map<Double, String> configuration) {
        if (configuration == null || configuration.isEmpty()) {
            return new CompactNumberFormatter(new double[0], new char[0][]);
        }

        TreeMap<Double, String> sortedMap = new TreeMap<>();
        configuration.forEach((threshold, symbol) -> {
            if (threshold > 0 && symbol != null) {
                sortedMap.put(threshold, symbol);
            }
        });

        int size = sortedMap.size();
        double[] thresholds = new double[size];
        char[][] units = new char[size][];
        int index = 0;
        for (Map.Entry<Double, String> entry : sortedMap.entrySet()) {
            thresholds[index] = entry.getKey();
            units[index] = entry.getValue().toCharArray();
            index++;
        }

        return new CompactNumberFormatter(thresholds, units);
    }

    /**
     * 将给定的 double 值格式化为紧凑的字符串表示形式。
     *
     * @param precision 小数点后的精度位数。必须在 0 到 17 之间。
     * @param value     要进行格式化的数值。
     * @return 格式化后的字符串，例如 "12.3K"。
     * @throws IllegalArgumentException 如果精度值超出允许的范围。
     */
    public String format(double value, int precision) {
        if (precision < 0 || precision >= POW10_CACHE.length) {
            throw new IllegalArgumentException("精度必须在 0 到 " + (POW10_CACHE.length - 1) + " 之间。");
        }

        final long scale = POW10_CACHE[precision];
        final StringBuilder buf = buffer.get();
        buf.setLength(0);

        final int unitIndex = findOptimalUnit(value);

        double scaledValue = value;
        char[] unitSymbol = null;

        if (unitIndex != -1) {
            scaledValue = value / thresholds[unitIndex];
            unitSymbol = units[unitIndex];
        }

        final long fixed = (long) (scaledValue * scale + 0.5);

        writeFormattedNumber(buf, fixed, scale, precision);

        if (unitSymbol != null) {
            buf.append(unitSymbol);
        }

        return buf.toString();
    }

    /**
     * 使用二分查找算法，高效地找到最适合给定值的单位索引。
     * <p>
     * 此方法查找的是 {@code thresholds} 数组中不大于 {@code value} 的最大元素的索引。
     *
     * @param value 要格式化的原始数值。
     * @return 匹配的单位在数组中的索引；如果没有任何阈值适用，则返回 -1。
     */
    private int findOptimalUnit(double value) {
        if (maxUnitIndex < 0 || value < thresholds[0]) {
            return -1;
        }

        int low = 0;
        int high = maxUnitIndex;

        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (value >= thresholds[mid]) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    /**
     * 将一个以 long 形式表示的定点数写入 StringBuilder。
     *
     * @param buf       目标 StringBuilder。
     * @param num       经过缩放和四舍五入后的数值 (例如，12.34 表示为 1234)。
     * @param scale     缩放因子 (10 的 {@code precision} 次幂)。
     * @param precision 小数部分的位数。
     */
    private void writeFormattedNumber(StringBuilder buf, long num, long scale, int precision) {
        final long integerPart = num / scale;
        buf.append(integerPart);

        if (precision > 0) {
            buf.append('.');
            final long decimalPart = num % scale;

            char[] decimalChars = new char[precision];
            long tempDecimal = decimalPart;
            for (int i = precision - 1; i >= 0; i--) {
                decimalChars[i] = DIGITS[(int) (tempDecimal % 10)];
                tempDecimal /= 10;
            }
            buf.append(decimalChars);
        }
    }
}
