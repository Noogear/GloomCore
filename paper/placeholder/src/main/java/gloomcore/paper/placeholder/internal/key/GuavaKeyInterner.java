package gloomcore.paper.placeholder.internal.key;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

public final class GuavaKeyInterner {

    private static final Interner<StringArrayKey> stringArrayKeyInterner = Interners.newWeakInterner();
    private static final Interner<PlaceholderKey> placeholderKeyInterner = Interners.newWeakInterner();

    private GuavaKeyInterner() {
    }

    public static StringArrayKey intern(String[] array) {
        if (array == null) {
            return null;
        }
        return stringArrayKeyInterner.intern(new StringArrayKey(array));
    }

    public static PlaceholderKey intern(String baseKey, String[] args) {
        return placeholderKeyInterner.intern(new PlaceholderKey(baseKey, args));
    }

}
