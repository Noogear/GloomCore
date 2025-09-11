package gloomcore.paper.command.util;

import gloomcore.paper.command.interfaces.CommandNode;
import gloomcore.paper.command.interfaces.ExecutableNode;
import gloomcore.paper.command.interfaces.RedirectableNode;
import gloomcore.paper.command.interfaces.SuggestableNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.*;

/**
 * 将命令节点树渲染为 MiniMessage 文本，带权限过滤与点击建议命令（到第一个参数为止）。
 * 返回的字符串可直接交给 MiniMessage.miniMessage().deserialize(..) 处理。
 * 输出对子节点名称排序，保证稳定性，便于缓存与对比。
 */
public enum CommandTreeMiniMessage {
    INSTANCE;

    // 缓存：运行期间默认只读；在注册/重载阶段构建与填充。
    // 采用 volatile 引用，在 clear/rebuild 时整体替换，避免同步开销与半预热可见。
    private volatile Map<String, String> LABEL_CACHE = new Object2ObjectOpenHashMap<>();
    private volatile Map<CommandNode, String> DESC_CACHE = new Object2ObjectOpenHashMap<>();
    private volatile Map<CommandNode, List<CommandNode>> CHILDREN_CACHE = new Object2ObjectOpenHashMap<>();
    private volatile Map<CommandNode, String> PLAIN_LABEL_CACHE = new Object2ObjectOpenHashMap<>();

    // ------------------ 静态包装，保持原 API 不变 ------------------
    public static void clearCaches() {
        INSTANCE.clearAll();
    }

    public static void prewarm(Collection<? extends CommandNode> roots) {
        INSTANCE.prewarmCaches(roots);
    }

    public static String toMiniMessage(Collection<? extends CommandNode> roots, CommandSourceStack source) {
        return INSTANCE.renderAll(roots, source);
    }

    public static String toMiniMessage(CommandNode root, CommandSourceStack source) {
        return INSTANCE.renderOne(root, source);
    }

    public static String toMiniMessage(Collection<? extends CommandNode> roots, CommandSourceStack source, int page, int pageSize) {
        return INSTANCE.renderAllPaged(roots, source, page, pageSize);
    }

    public static String toMiniMessage(CommandNode root, CommandSourceStack source, int page, int pageSize) {
        return INSTANCE.renderOnePaged(root, source, page, pageSize);
    }

    public static int totalLines(Collection<? extends CommandNode> roots, CommandSourceStack source) {
        return INSTANCE.countLinesAll(roots, source);
    }

    public static int totalLines(CommandNode root, CommandSourceStack source) {
        return INSTANCE.countLinesOne(root, source);
    }

    public static int totalPages(Collection<? extends CommandNode> roots, CommandSourceStack source, int pageSize) {
        return INSTANCE.countPagesAll(roots, source, pageSize);
    }

    public static int totalPages(CommandNode root, CommandSourceStack source, int pageSize) {
        return INSTANCE.countPagesOne(root, source, pageSize);
    }

    // ------------------ 实例方法实现 ------------------

    /**
     * 重建所有缓存 Map（整体替换），清除历史引用，减少同步需求。
     */
    private void clearAll() {
        LABEL_CACHE = new Object2ObjectOpenHashMap<>();
        DESC_CACHE = new Object2ObjectOpenHashMap<>();
        CHILDREN_CACHE = new Object2ObjectOpenHashMap<>();
        PLAIN_LABEL_CACHE = new Object2ObjectOpenHashMap<>();
    }

    /**
     * 预热缓存：在本地 Map 完整构建后一次性替换，避免运行期看到"半预热"状态。
     * 与权限无关；标签缓存会绑定当前全局样式签名。
     */
    private void prewarmCaches(Collection<? extends CommandNode> roots) {
        if (roots == null || roots.isEmpty()) return;
        int expected = estimateNodeCount(roots);
        CommandTreeStyle style = CommandTreeStyle.get();

        // 本地新 Map，使用预估大小减少扩容
        Map<String, String> newLabel = new Object2ObjectOpenHashMap<>(expected);
        Map<CommandNode, String> newDesc = new Object2ObjectOpenHashMap<>(expected);
        Map<CommandNode, List<CommandNode>> newChildren = new Object2ObjectOpenHashMap<>(expected);
        Map<CommandNode, String> newPlain = new Object2ObjectOpenHashMap<>(expected);

        Deque<CommandNode> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            CommandNode n = stack.pop();
            // 子节点排序（局部计算，不读取旧缓存）
            List<CommandNode> kids = computeSortedChildren(n);
            newChildren.put(n, kids);
            // 描述
            newDesc.put(n, computeDescription(n));
            // 标签（与样式相关）
            String labelKey = labelKey(n, style);
            newLabel.put(labelKey, computeLabel(n, style));
            // 纯标签
            newPlain.put(n, computePlainLabel(n));
            if (!kids.isEmpty()) stack.addAll(kids);
        }

        // 原子替换
        LABEL_CACHE = newLabel;
        DESC_CACHE = newDesc;
        CHILDREN_CACHE = newChildren;
        PLAIN_LABEL_CACHE = newPlain;
    }

    /**
     * 估算节点数用于初始化 Map 容量（两遍遍历，轻量开销）。
     */
    private int estimateNodeCount(Collection<? extends CommandNode> roots) {
        int count = 0;
        Deque<CommandNode> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            CommandNode n = stack.pop();
            count++;
            Collection<CommandNode> kids = CommandNodeUtils.childrenOf(n);
            if (!kids.isEmpty()) stack.addAll(kids);
        }
        return Math.max(count, roots.size());
    }

    // ------------------ 渲染与统计 ------------------

    /**
     * 渲染多棵根命令。
     */
    private String renderAll(Collection<? extends CommandNode> roots, CommandSourceStack source) {
        if (roots == null || roots.isEmpty()) return "";
        List<NodeModel> models = new ArrayList<>();
        List<CommandNode> sorted = new ArrayList<>(roots);
        sorted.sort(Comparator.comparing(CommandNode::getName));
        for (CommandNode node : sorted) {
            if (CommandNodeUtils.isAllowed(node, source)) {
                models.add(buildModel(node, source));
            }
        }
        if (models.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < models.size(); i++) {
            NodeModel m = models.get(i);
            boolean last = (i == models.size() - 1);
            renderSubtree(sb, m, "", last, "/" + m.node.getName());
        }
        return sb.toString();
    }

    /**
     * 渲染单棵根命令。
     */
    private String renderOne(CommandNode root, CommandSourceStack source) {
        Objects.requireNonNull(root, "root");
        if (!CommandNodeUtils.isAllowed(root, source)) return "";
        StringBuilder sb = new StringBuilder();
        NodeModel model = buildModel(root, source);
        renderSubtree(sb, model, "", true, "/" + model.node.getName());
        return sb.toString();
    }

    /**
     * 分页渲染多根（1 基页码）。
     */
    private String renderAllPaged(Collection<? extends CommandNode> roots, CommandSourceStack source, int page, int pageSize) {
        if (roots == null || roots.isEmpty()) return "";
        int targetPage = Math.max(1, page);
        int sizePerPage = Math.max(1, pageSize);
        List<NodeModel> rootModels = new ArrayList<>();
        List<CommandNode> sorted = new ArrayList<>(roots);
        sorted.sort(Comparator.comparing(CommandNode::getName));
        for (CommandNode n : sorted) {
            if (CommandNodeUtils.isAllowed(n, source)) rootModels.add(buildModel(n, source));
        }
        if (rootModels.isEmpty()) return "";
        Pager pager = new Pager(targetPage, sizePerPage);
        int lastIdx = rootModels.size() - 1;
        for (int i = 0; i < rootModels.size(); i++) {
            NodeModel m = rootModels.get(i);
            boolean tail = (i == lastIdx);
            pager.emitSubtree(m, "", tail, "/" + m.node.getName());
        }
        return pager.result();
    }

    /**
     * 分页渲染单根（1 基页码）。
     */
    private String renderOnePaged(CommandNode root, CommandSourceStack source, int page, int pageSize) {
        Objects.requireNonNull(root, "root");
        if (!CommandNodeUtils.isAllowed(root, source)) return "";
        int targetPage = Math.max(1, page);
        int sizePerPage = Math.max(1, pageSize);
        NodeModel model = buildModel(root, source);
        Pager pager = new Pager(targetPage, sizePerPage);
        pager.emitSubtree(model, "", true, "/" + model.node.getName());
        return pager.result();
    }

    /**
     * 统计行数（多根）。
     */
    private int countLinesAll(Collection<? extends CommandNode> roots, CommandSourceStack source) {
        if (roots == null || roots.isEmpty()) return 0;
        List<CommandNode> sorted = new ArrayList<>(roots);
        sorted.sort(Comparator.comparing(CommandNode::getName));
        int sum = 0;
        for (CommandNode n : sorted) if (CommandNodeUtils.isAllowed(n, source)) sum += buildModel(n, source).lines;
        return sum;
    }

    /**
     * 统计行数（单根）。
     */
    private int countLinesOne(CommandNode root, CommandSourceStack source) {
        Objects.requireNonNull(root, "root");
        if (!CommandNodeUtils.isAllowed(root, source)) return 0;
        return buildModel(root, source).lines;
    }

    /**
     * 统计页数（多根）。
     */
    private int countPagesAll(Collection<? extends CommandNode> roots, CommandSourceStack source, int pageSize) {
        int ps = Math.max(1, pageSize);
        int lines = countLinesAll(roots, source);
        return lines == 0 ? 0 : (int) Math.ceil(lines / (double) ps);

    }

    /**
     * 统计页数（单根）。
     */
    private int countPagesOne(CommandNode root, CommandSourceStack source, int pageSize) {
        int ps = Math.max(1, pageSize);
        int lines = countLinesOne(root, source);
        return lines == 0 ? 0 : (int) Math.ceil(lines / (double) ps);
    }

    // ------------------ 内部实现细节 ------------------

    private NodeModel buildModel(CommandNode node, CommandSourceStack source) {
        List<CommandNode> raw = getSortedChildren(node);
        List<NodeModel> vis = new ArrayList<>();
        for (CommandNode c : raw) if (CommandNodeUtils.isAllowed(c, source)) vis.add(buildModel(c, source));
        cacheDescription(node);
        return new NodeModel(node, Collections.unmodifiableList(vis));
    }

    private List<CommandNode> getSortedChildren(CommandNode node) {
        List<CommandNode> cached = CHILDREN_CACHE.get(node);
        if (cached != null) return cached;
        List<CommandNode> unmodifiable = computeSortedChildren(node);
        CHILDREN_CACHE.put(node, unmodifiable);
        return unmodifiable;
    }

    private List<CommandNode> computeSortedChildren(CommandNode node) {
        List<CommandNode> raw = new ArrayList<>(CommandNodeUtils.childrenOf(node));
        raw.sort(Comparator.comparing(CommandNode::getName));
        return Collections.unmodifiableList(raw);
    }

    private void renderSubtree(StringBuilder sb, NodeModel model, String prefix, boolean tail, String currentPath) {
        CommandNode node = model.node;
        appendLine(sb, node, prefix, tail, currentPath);
        int last = model.children.size() - 1;
        for (int i = 0; i < model.children.size(); i++) {
            NodeModel child = model.children.get(i);
            boolean childLast = (i == last);
            String childPrefix = prefix + (tail ? "   " : "│  ");
            String childPath = nextPath(currentPath, child.node);
            renderSubtree(sb, child, childPrefix, childLast, childPath);
        }
    }

    private String nextPath(String current, CommandNode node) {
        String name = node.getName();
        if (CommandNodeUtils.isArgument(node)) return current + " <" + name + ">";
        return current + " " + name;
    }

    private String argAwareSuggest(String currentPath, CommandNode node) {
        int lt = currentPath.indexOf('<');
        if (lt >= 0) {
            int gt = currentPath.indexOf('>', lt + 1);
            if (gt > lt) return currentPath.substring(0, gt + 1) + " ";
        }
        if (CommandNodeUtils.isArgument(node)) {
            if (!currentPath.contains("<" + node.getName() + ">"))
                currentPath = currentPath + " <" + node.getName() + ">";
            return currentPath + " ";
        }
        return currentPath + " ";
    }

    private String getLabel(CommandNode node, CommandTreeStyle style) {
        String key = labelKey(node, style);
        String cached = LABEL_CACHE.get(key);
        if (cached != null) return cached;
        String computed = computeLabel(node, style);
        LABEL_CACHE.put(key, computed);
        return computed;
    }

    private String computeLabel(CommandNode node, CommandTreeStyle style) {
        boolean arg = CommandNodeUtils.isArgument(node);
        String open = arg ? style.openArgument() : style.openLiteral();
        String close = arg ? style.closeArgument() : style.closeLiteral();
        String base = CommandNodeUtils.baseToken(node);
        StringBuilder sb = new StringBuilder(open).append(escapeMini(base)).append(close);
        if (node instanceof ExecutableNode)
            sb.append(" ").append(style.openSymbol()).append("*").append(style.closeSymbol());
        if (node instanceof SuggestableNode)
            sb.append(" ").append(style.openSymbol()).append("~").append(style.closeSymbol());
        if (node instanceof RedirectableNode r && r.getRedirectTarget() != null) {
            sb.append(" ").append(style.openRedirect()).append("->");
            if (r.isFork()) sb.append("(fork)");
            sb.append(style.closeRedirect());
        }
        return sb.toString();
    }

    private String labelKey(CommandNode node, CommandTreeStyle style) {
        StringBuilder b = new StringBuilder();
        b.append(node.getClass().getName()).append('|')
                .append(node.getName()).append('|')
                .append(CommandNodeUtils.isArgument(node) ? 'A' : 'L').append('|')
                .append((node instanceof ExecutableNode) ? 'E' : '-').append('|')
                .append((node instanceof SuggestableNode) ? 'S' : '-').append('|');
        if (node instanceof RedirectableNode r && r.getRedirectTarget() != null) {
            b.append('R');
            if (r.isFork()) b.append('F');
        } else {
            b.append('-');
        }
        b.append('|').append(style.getTreeColor())
                .append('|').append(style.getLiteralColor())
                .append('|').append(style.getArgumentColor())
                .append('|').append(style.getDescriptionColor())
                .append('|').append(style.getSymbolColor())
                .append('|').append(style.getRedirectColor());
        return b.toString();
    }

    private String clickable(String text, String suggest, String hover) {
        return "<click:suggest_command:'" + escapeAttr(suggest) + "'>" +
                "<hover:show_text:'" + escapeAttr(hover) + "'>" + text + "</hover></click>";
    }

    private String escapeMini(String s) {
        return (s == null) ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private String escapeAttr(String s) {
        return (s == null) ? "" : s.replace("'", "\\'").replace("\n", " ");
    }

    private void appendLine(StringBuilder out, CommandNode node, String prefix, boolean tail, String path) {
        renderLine(out, node, prefix, tail, path);
    }

    private void renderLine(StringBuilder out, CommandNode node, String prefix, boolean tail, String path) {
        CommandTreeStyle style = CommandTreeStyle.get();
        String label = getLabel(node, style);
        String desc = getDescription(node);
        String hoverTitle = getPlainLabel(node);
        String hover = desc.isEmpty() ? hoverTitle : (hoverTitle + "\n" + desc);
        String suggest = argAwareSuggest(path, node);
        out.append(style.openTree())
                .append(prefix)
                .append(tail ? "└─ " : "├─ ")
                .append(style.closeTree())
                .append(clickable(label, suggest, hover));
        if (!desc.isEmpty()) {
            out.append(" ")
                    .append(style.openDescription())
                    .append("- ")
                    .append(escapeMini(desc))
                    .append(style.closeDescription());
        }
        out.append('\n');
    }

    private String getPlainLabel(CommandNode node) {
        String s = PLAIN_LABEL_CACHE.get(node);
        if (s != null) return s;
        s = computePlainLabel(node);
        PLAIN_LABEL_CACHE.put(node, s);
        return s;
    }

    private String computePlainLabel(CommandNode node) {
        StringBuilder sb = new StringBuilder(CommandNodeUtils.baseToken(node));
        if (node instanceof ExecutableNode) sb.append(" *");
        if (node instanceof SuggestableNode) sb.append(" ~");
        if (node instanceof RedirectableNode r && r.getRedirectTarget() != null) {
            sb.append(" ->");
            if (r.isFork()) sb.append("(fork)");
        }
        return sb.toString();
    }

    private String getDescription(CommandNode node) {
        String s = DESC_CACHE.get(node);
        if (s != null) return s;
        s = computeDescription(node);
        DESC_CACHE.put(node, s);
        return s;
    }

    private String computeDescription(CommandNode node) {
        String raw = CommandNodeUtils.description(node);
        return raw.isEmpty() ? "" : raw;
    }

    private void cacheDescription(CommandNode node) {
        getDescription(node);
    }

    // 用于分页的轻量模型（已按权限过滤并排序），并预计算子树行数
    private static final class NodeModel {
        final CommandNode node;
        final List<NodeModel> children;
        final int lines; // 包含自己的一行

        NodeModel(CommandNode node, List<NodeModel> children) {
            this.node = node;
            this.children = children;
            int sum = 1;
            for (NodeModel c : children) sum += c.lines;
            this.lines = sum;
        }
    }

    private record AncEntry(NodeModel model, String prefix, boolean tail, String path) {
    }


    // 设为非静态内部类，以便调用外部实例方法（渲染行等）
    private final class Pager {
        final int targetPage;
        final int pageSize;
        final StringBuilder pageBuf = new StringBuilder();
        final Deque<AncEntry> chain = new ArrayDeque<>();
        int currentPage = 1;
        int remaining;
        boolean producedTarget = false;

        Pager(int targetPage, int pageSize) {
            this.targetPage = targetPage;
            this.pageSize = pageSize;
            this.remaining = pageSize;
        }

        String result() {
            return producedTarget ? pageBuf.toString() : "";
        }

        void emitSubtree(NodeModel node, String prefix, boolean tail, String path) {
            ensureSpaceOrTurnPage();
            appendLine(pageBuf, node.node, prefix, tail, path);
            chain.addLast(new AncEntry(node, prefix, tail, path));
            int last = node.children.size() - 1;
            for (int i = 0; i < node.children.size(); i++) {
                NodeModel ch = node.children.get(i);
                boolean chTail = (i == last);
                String chPrefix = prefix + (tail ? "   " : "│  ");
                String chPath = nextPath(path, ch.node);
                if (ch.lines > remaining) {
                    turnPageAndReprintContext();
                }
                emitSubtree(ch, chPrefix, chTail, chPath);
            }
            chain.removeLast();
        }

        private void ensureSpaceOrTurnPage() {
            if (remaining > 0) return;
            newPage();
            reprintContextForNewPage();
        }

        private void turnPageAndReprintContext() {
            newPage();
            reprintContextForNewPage();
        }

        private void newPage() {
            currentPage++;
            remaining = pageSize;
        }

        private void reprintContextForNewPage() {
            if (chain.isEmpty()) return;
            int allow = Math.max(0, pageSize - 1);
            if (allow == 0) return;
            int ctx = Math.min(chain.size(), allow);
            AncEntry[] arr = chain.toArray(new AncEntry[0]);
            int start = chain.size() - ctx;
            for (int i = start; i < arr.length; i++) {
                AncEntry e = arr[i];
                appendLine(pageBuf, e.model.node, e.prefix, e.tail, e.path);
            }
        }

        private void appendLine(StringBuilder out, CommandNode node, String prefix, boolean tail, String path) {
            if (remaining <= 0) {
                newPage();
            }
            if (currentPage == targetPage) {
                producedTarget = true;
                CommandTreeMiniMessage.this.renderLine(out, node, prefix, tail, path);
            }
            remaining--;
            if (remaining < 0) remaining = 0;
        }
    }
}
