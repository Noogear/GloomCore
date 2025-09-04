package gloomcore.paper.placeholder.internal.key;

import java.util.Arrays;

public final class StringArrayKey {
    private final String[] array;
    private final int hashCode;

    public StringArrayKey(String[] array) {
        this.array = array.clone();
        this.hashCode = Arrays.hashCode(this.array);
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
