package gloomcore.paper.placeholder.internal.key;

import java.util.Arrays;
import java.util.Objects;

public final class PlaceholderKey {
    private final String baseKey;
    private final String[] args;
    private final int hashCode; // 缓存哈希码

    public PlaceholderKey(String baseKey, String[] args) {
        this.baseKey = Objects.requireNonNull(baseKey);
        this.args = args.clone();
        this.hashCode = computeHashCode();
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
