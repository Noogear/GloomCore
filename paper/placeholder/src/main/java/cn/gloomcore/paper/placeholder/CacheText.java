package cn.gloomcore.paper.placeholder;

public class CacheText {
    private String text;
    private long lastUpdate;

    public CacheText(String text, long lastUpdate) {
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
