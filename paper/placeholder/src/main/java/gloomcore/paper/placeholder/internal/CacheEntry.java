package gloomcore.paper.placeholder.internal;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class CacheEntry {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private volatile String text;
    private volatile long lastUpdate;

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

    public String getOrUpdate(long intervalMillis, Supplier<String> supplier) {
        readLock.lock();
        try {
            if (isCacheValid(intervalMillis)) {
                return this.text;
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            if (isCacheValid(intervalMillis)) {
                return this.text;
            }
            this.text = supplier.get();
            this.lastUpdate = System.currentTimeMillis();
            return this.text;
        } finally {
            writeLock.unlock();
        }
    }

    public String update(String text, long lastUpdate) {
        this.text = text;
        this.lastUpdate = lastUpdate;
        return text;
    }

    private boolean isCacheValid(long intervalMillis) {
        return (System.currentTimeMillis() - this.lastUpdate) <= intervalMillis;
    }
}
