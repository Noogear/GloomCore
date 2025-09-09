package gloomcore.paper.command.util;

import gloomcore.paper.command.interfaces.ICommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * 命令树文本缓存。在命令注册完成后写入，供日志/调试或对外查询使用。
 */
public enum CommandTreeCache {

    INSTANCE;

    private final Map<String, String> perRoot = new ConcurrentHashMap<>();
    private final AtomicReference<String> combined = new AtomicReference<>("");
    // 基于 Source 的 MiniMessage 渲染缓存（LRU + TTL）
    private final MiniCache miniAllCache = new MiniCache(256, 2000);
    private final MiniCache miniPerRootCache = new MiniCache(512, 2000);
    private volatile Instant lastUpdated = null;
    // 最近一次构建的根节点快照，用于按权限快速渲染 MiniMessage
    private volatile List<ICommandNode> lastRoots = List.of();

    private static String styleFingerprint(CommandTreeStyle s) {
        return String.join(",",
                s.getTreeColor(), s.getLiteralColor(), s.getArgumentColor(),
                s.getDescriptionColor(), s.getSymbolColor(), s.getRedirectColor());
    }

    /**
     * 缓存单个根命令的文字树，并合并进 roots 快照。
     */
    public void cacheSingle(ICommandNode root) {
        Objects.requireNonNull(root, "root");
        String text = CommandTreePrinter.toText(root);
        perRoot.put(root.getName(), text);
        this.combined.set(String.join("", perRoot.values()));
        this.lastUpdated = Instant.now();
        // 合并快照
        List<ICommandNode> copy = new ArrayList<>(lastRoots);
        copy.removeIf(n -> Objects.equals(n.getName(), root.getName()));
        copy.add(root);
        lastRoots = List.copyOf(copy);
        // 失效 MiniMessage 渲染缓存
        miniAllCache.clear();
        miniPerRootCache.clear();
    }

    /**
     * 一次性缓存多个根命令的文字树，并更新 roots 快照。
     */
    public void cacheAll(Collection<? extends ICommandNode> roots) {
        Objects.requireNonNull(roots, "roots");
        perRoot.clear();
        for (ICommandNode root : roots) {
            perRoot.put(root.getName(), CommandTreePrinter.toText(root));
        }
        this.combined.set(CommandTreePrinter.toText(roots));
        this.lastUpdated = Instant.now();
        this.lastRoots = List.copyOf(roots);
        // 失效 MiniMessage 渲染缓存
        miniAllCache.clear();
        miniPerRootCache.clear();
    }

    /**
     * 获取合并后的多根命令文字树（ASCII）。
     */
    public String getCombined() {
        return combined.get();
    }

    /**
     * 获取指定根命令的文字树（ASCII）。
     */
    public String getForRoot(String rootName) {
        return perRoot.getOrDefault(rootName, "");
    }

    /**
     * 返回最近一次注册的根节点快照（只读）。
     */
    public List<ICommandNode> getRootsSnapshot() {
        return lastRoots;
    }

    /**
     * 基于权限为给定的 source 渲染整棵命令树（MiniMessage），使用缓存以提升性能。
     */
    public String renderMiniFor(CommandSourceStack source) {
        List<ICommandNode> roots = this.lastRoots;
        if (roots == null || roots.isEmpty()) return "";
        String key = cacheKey(source, "__ALL__");
        String cached = miniAllCache.get(key);
        if (cached != null) return cached;
        String rendered = CommandTreeMiniMessage.toMiniMessage(roots, source);
        miniAllCache.put(key, rendered);
        return rendered;
    }

    /**
     * 基于权限为给定的 source 渲染单个根（MiniMessage），使用缓存以提升性能。
     */
    public String renderMiniForRoot(String rootName, CommandSourceStack source) {
        if (rootName == null || rootName.isBlank()) return "";
        String key = cacheKey(source, rootName);
        String cached = miniPerRootCache.get(key);
        if (cached != null) return cached;
        for (ICommandNode n : lastRoots) {
            if (rootName.equals(n.getName())) {
                String rendered = CommandTreeMiniMessage.toMiniMessage(n, source);
                miniPerRootCache.put(key, rendered);
                return rendered;
            }
        }
        return "";
    }

    /**
     * 清空缓存。
     */
    public void clear() {
        perRoot.clear();
        combined.set("");
        lastUpdated = null;
        lastRoots = List.of();
        miniAllCache.clear();
        miniPerRootCache.clear();
    }

    /**
     * 将当前缓存输出到日志（若为空则忽略）。
     */
    public void logCombined(Logger logger) {
        if (logger != null) {
            String text = getCombined();
            if (!text.isEmpty()) {
                logger.info("\n" + text);
            }
        }
    }

    // ------------- 内部：MiniMessage 渲染缓存 -------------

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    private String cacheKey(CommandSourceStack source, String suffix) {
        String senderKey;
        try {
            if (source.getSender() instanceof org.bukkit.entity.Player p) {
                senderKey = p.getUniqueId().toString();
            } else {
                senderKey = source.getSender().getName();
            }
        } catch (Throwable t) {
            senderKey = "unknown";
        }
        long stamp = (lastUpdated == null) ? 0L : lastUpdated.toEpochMilli();
        String styleFp = styleFingerprint(CommandTreeStyle.get());
        return senderKey + "|" + stamp + "|" + styleFp + "|" + suffix;
    }

    private static final class MiniCache {
        private final int maxEntries;
        private final long ttlMillis;
        private final LinkedHashMap<String, Entry> map;

        MiniCache(int maxEntries, long ttlMillis) {
            this.maxEntries = Math.max(16, maxEntries);
            this.ttlMillis = Math.max(0, ttlMillis);
            this.map = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
                    return size() > MiniCache.this.maxEntries;
                }
            };
        }

        synchronized String get(String k) {
            Entry e = map.get(k);
            if (e == null) return null;
            if (ttlMillis > 0 && (System.currentTimeMillis() - e.t) > ttlMillis) {
                map.remove(k);
                return null;
            }
            return e.v;
        }

        synchronized void put(String k, String v) {
            map.put(k, new Entry(v, System.currentTimeMillis()));
        }

        synchronized void clear() {
            map.clear();
        }

        private record Entry(String v, long t) {
        }
    }
}
