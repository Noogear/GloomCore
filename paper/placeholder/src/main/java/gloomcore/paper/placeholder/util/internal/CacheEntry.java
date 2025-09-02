package gloomcore.paper.placeholder.util.internal;

public class CacheEntry {
    private String text;
    private long lastUpdate;

    public CacheEntry(String text, long lastUpdate) {
        this.text = text;
        this.lastUpdate = lastUpdate;
    }

    public String getText() {
        return text;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String update(String text, long lastUpdate) {
        this.text = text;
        this.lastUpdate = lastUpdate;
        return text;
    }
}
