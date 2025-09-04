package gloomcore.paper.placeholder.internal;

import java.util.function.Supplier;

public class CacheEntry {

    private volatile State currentState;

    public CacheEntry(String text, long lastUpdate) {
        this.currentState = new State(text, lastUpdate);
    }

    public String getOrUpdate(long intervalMillis, Supplier<String> supplier) {
        State state = this.currentState;
        if (isCacheValid(state, intervalMillis)) {
            return state.text;
        }
        synchronized (this) {
            state = this.currentState;
            if (isCacheValid(state, intervalMillis)) {
                return state.text;
            }
            String newText = supplier.get();
            this.currentState = new State(newText, System.currentTimeMillis());
            return newText;
        }
    }

    private boolean isCacheValid(State state, long intervalMillis) {
        return (System.currentTimeMillis() - state.lastUpdate) <= intervalMillis;
    }

    private record State(String text, long lastUpdate) {
    }
}
