package gloomcore.paper.placeholder.internal.key;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.util.Arrays;
import java.util.Objects;

public final class PlaceholderKey {
    private static final Interner<PlaceholderKey> KEY_INTERNER = Interners.newWeakInterner();

    private final String baseKey;
    private final String[] args;
    private final int hashCode;

    private PlaceholderKey(String baseKey, String[] args) {
        this.baseKey = Objects.requireNonNull(baseKey);
        this.args = args;
        this.hashCode = computeHashCode();
    }

    public static PlaceholderKey intern(String baseKey, String[] args) {
        return KEY_INTERNER.intern(new PlaceholderKey(baseKey, args));
    }

    private int computeHashCode() {
        int result = baseKey.hashCode();
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PlaceholderKey other = (PlaceholderKey) obj;
        return this.baseKey.equals(other.baseKey) &&
                Arrays.equals(this.args, other.args);
    }

}
