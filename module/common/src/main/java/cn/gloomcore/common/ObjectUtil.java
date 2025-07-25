package cn.gloomcore.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectUtil {

    public static int toInt(Object value) {
        if (value instanceof Number num) {
            return num.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    public static String toString(Object value) {
        return String.valueOf(value);
    }

    public static String toString(Object value, String defaultValue) {
        return value != null ? toString(value) : defaultValue;
    }

    public static double toDouble(Object value) {
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    public static boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public static short toShort(Object value) {
        if (value instanceof Number num) {
            return num.shortValue();
        }
        return Short.parseShort(value.toString());
    }

    public static List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object element : list) {
                result.add(toString(element));
            }
            return result;
        } else if (value instanceof String str) {
            return Collections.singletonList(str);
        }
        return Collections.singletonList(toString(value));
    }

}
