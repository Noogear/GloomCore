package gloomcore.paper.placeholder.internal.key;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.util.Arrays;

public final class StringArrayKey {
    private static final Interner<StringArrayKey> KEY_INTERNER = Interners.newWeakInterner();

    private final String[] array;
    private final int hashCode;

    private StringArrayKey(String[] array) {
        this.array = array;
        this.hashCode = Arrays.hashCode(this.array);
    }

    public static StringArrayKey intern(String[] array) {
        if (array == null) {
            return null;
        }
        return KEY_INTERNER.intern(new StringArrayKey(array));
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StringArrayKey other = (StringArrayKey) obj;
        return Arrays.equals(this.array, other.array);
    }
}
