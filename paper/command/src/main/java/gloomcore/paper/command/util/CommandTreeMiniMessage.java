package gloomcore.paper.command.util;

import gloomcore.paper.command.framework.LiteralNode;
import gloomcore.paper.command.framework.argument.ArgumentNode;
import gloomcore.paper.command.framework.argument.CustomArgumentNode;
import gloomcore.paper.command.interfaces.CommandNode;
import gloomcore.paper.command.interfaces.ExecutableNode;
import gloomcore.paper.command.interfaces.RedirectableNode;
import gloomcore.paper.command.interfaces.SuggestableNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.*;

/**
 * 将命令节点树渲染为 MiniMessage 文本，带权限过滤与点击建议命令（到第一个参数为止）。
 * 返回的字符串可直接交给 MiniMessage.miniMessage().deserialize(..) 处理。
 * 输出对子节点名称排序，保证稳定性，便于缓存与对比。
 */
public final class CommandTreeMiniMessage {

    private CommandTreeMiniMessage() {
    }

    /**
     * 渲染多棵根命令。
     *
     * @param roots  根集合
     * @param source 权限来源
     * @return MiniMessage 文本
     */
    public static String toMiniMessage(Collection<? extends CommandNode> roots, CommandSourceStack source) {
        if (roots == null || roots.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<CommandNode> sorted = new ArrayList<>(roots);
        sorted.sort(Comparator.comparing(CommandNode::getName));
        for (int i = 0; i < sorted.size(); i++) {
            CommandNode node = sorted.get(i);
            boolean last = (i == sorted.size() - 1);
            if (CommandNodeUtils.isAllowed(node, source)) {
                appendNode(sb, node, source, "", last, "/" + node.getName());
            }
        }
        return sb.toString();
    }

    /**
     * 渲染单棵根命令。
     *
     * @param root   根节点
     * @param source 权限来源
     * @return MiniMessage 文本
     */
    public static String toMiniMessage(CommandNode root, CommandSourceStack source) {
        Objects.requireNonNull(root, "root");
        if (!CommandNodeUtils.isAllowed(root, source)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        appendNode(sb, root, source, "", true, "/" + root.getName());
        return sb.toString();
    }

    // ======================== 分页渲染 ========================
    /**
     * 分页渲染多棵根命令（1 基页码）。
     * - page <= 0 时按 1 处理；pageSize <= 0 时按 1 处理。
     * - 分页时不拆分子树；若当前页剩余行数不足以容纳某个子树，则换到下一页，并从父节点（含必要祖先链）续上。
     */
    public static String toMiniMessage(Collection<? extends CommandNode> roots,
                                       CommandSourceStack source,
                                       int page,
                                       int pageSize) {
        if (roots == null || roots.isEmpty()) {
            return "";
        }
        int targetPage = Math.max(1, page);
        int sizePerPage = Math.max(1, pageSize);

        List<NodeModel> rootModels = new ArrayList<>();
        List<CommandNode> sorted = new ArrayList<>(roots);
        sorted.sort(Comparator.comparing(CommandNode::getName));
        for (CommandNode n : sorted) {
            if (CommandNodeUtils.isAllowed(n, source)) {
                rootModels.add(buildModel(n, source));
            }
        }
        if (rootModels.isEmpty()) {
            return "";
        }

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
     * 分页渲染单棵根命令（1 基页码）。
     * - page <= 0 时按 1 处理；pageSize <= 0 时按 1 处理。
     * - 分页时不拆分子树；若当前页剩余行数不足以容纳某个子树，则换到下一页，并从父节点（含必要祖先链）续上。
     */
    public static String toMiniMessage(CommandNode root,
                                       CommandSourceStack source,
                                       int page,
                                       int pageSize) {
        Objects.requireNonNull(root, "root");
        if (!CommandNodeUtils.isAllowed(root, source)) {
            return "";
        }
        int targetPage = Math.max(1, page);
        int sizePerPage = Math.max(1, pageSize);
        NodeModel model = buildModel(root, source);
        Pager pager = new Pager(targetPage, sizePerPage);
        pager.emitSubtree(model, "", true, "/" + model.node.getName());
        return pager.result();
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

    private static NodeModel buildModel(CommandNode node, CommandSourceStack source) {
        List<CommandNode> raw = new ArrayList<>(CommandNodeUtils.childrenOf(node));
        raw.sort(Comparator.comparing(CommandNode::getName));
        List<NodeModel> vis = new ArrayList<>();
        for (CommandNode c : raw) {
            if (CommandNodeUtils.isAllowed(c, source)) {
                vis.add(buildModel(c, source));
            }
        }
        return new NodeModel(node, Collections.unmodifiableList(vis));
    }

    private static final class AncEntry {
        final NodeModel model; final String prefix; final boolean tail; final String path;
        AncEntry(NodeModel m, String p, boolean t, String path) { this.model = m; this.prefix = p; this.tail = t; this.path = path; }
    }

    private static final class Pager {
        final int targetPage; final int pageSize;
        int currentPage = 1; int remaining;
        boolean producedTarget = false;
        final StringBuilder pageBuf = new StringBuilder();
        final Deque<AncEntry> chain = new ArrayDeque<>();
        Pager(int targetPage, int pageSize) { this.targetPage = targetPage; this.pageSize = pageSize; this.remaining = pageSize; }
        String result() { return producedTarget ? pageBuf.toString() : ""; }

        void emitSubtree(NodeModel node, String prefix, boolean tail, String path) {
            ensureSpaceOrTurnPage();
            appendLine(node.node, prefix, tail, path);
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
        private void newPage() { currentPage++; remaining = pageSize; }
        private void reprintContextForNewPage() {
            if (chain.isEmpty()) return;
            int allow = Math.max(0, pageSize - 1);
            if (allow == 0) return;
            int ctx = Math.min(chain.size(), allow);
            AncEntry[] arr = chain.toArray(new AncEntry[0]);
            int start = chain.size() - ctx;
            for (int i = start; i < arr.length; i++) {
                AncEntry e = arr[i];
                appendLine(e.model.node, e.prefix, e.tail, e.path);
            }
        }
        private void appendLine(CommandNode node, String prefix, boolean tail, String path) {
            if (remaining <= 0) { newPage(); }
            if (currentPage == targetPage) {
                producedTarget = true;
                CommandTreeStyle style = CommandTreeStyle.get();
                String label = labelFor(node, style);
                String desc = CommandNodeUtils.description(node);
                String hover = desc.isEmpty() ? stripTags(label) : (stripTags(label) + "\n" + desc);
                String suggest = argAwareSuggest(path, node);
                pageBuf.append(style.openTree())
                        .append(prefix)
                        .append(tail ? "└─ " : "├─ ")
                        .append(style.closeTree())
                        .append(clickable(label, suggest, hover));
                if (!desc.isEmpty()) {
                    pageBuf.append(" ")
                            .append(style.openDescription())
                            .append("- ")
                            .append(escapeMini(desc))
                            .append(style.closeDescription());
                }
                pageBuf.append('\n');
            }
            remaining--; if (remaining < 0) remaining = 0;
        }
    }

    // ======================== 原完整渲染 ========================
    private static void appendNode(StringBuilder sb,
                                   CommandNode node,
                                   CommandSourceStack source,
                                   String prefix,
                                   boolean tail,
                                   String currentPath) {
        CommandTreeStyle style = CommandTreeStyle.get();
        String label = labelFor(node, style);
        String desc = CommandNodeUtils.description(node);
        String hover = desc.isEmpty() ? stripTags(label) : (stripTags(label) + "\n" + desc);
        String suggest = argAwareSuggest(currentPath, node);

        sb.append(style.openTree())
                .append(prefix)
                .append(tail ? "└─ " : "├─ ")
                .append(style.closeTree())
                .append(clickable(label, suggest, hover));
        if (!desc.isEmpty()) {
            sb.append(" ")
                    .append(style.openDescription())
                    .append("- ")
                    .append(escapeMini(desc))
                    .append(style.closeDescription());
        }
        sb.append('\n');

        List<CommandNode> children = new ArrayList<>(CommandNodeUtils.childrenOf(node));
        children.sort(Comparator.comparing(CommandNode::getName));
        List<CommandNode> visible = new ArrayList<>();
        for (CommandNode c : children) {
            if (CommandNodeUtils.isAllowed(c, source)) {
                visible.add(c);
            }
        }
        for (int i = 0; i < visible.size(); i++) {
            CommandNode child = visible.get(i);
            boolean childLast = (i == visible.size() - 1);
            String childPrefix = prefix + (tail ? "   " : "│  ");
            String childPath = nextPath(currentPath, child);
            appendNode(sb, child, source, childPrefix, childLast, childPath);
        }
    }

    private static String nextPath(String current, CommandNode node) {
        String name = node.getName();
        if (node instanceof LiteralNode) {
            return current + " " + name;
        }
        if (node instanceof ArgumentNode<?> || node instanceof CustomArgumentNode<?, ?>) {
            return current + " <" + name + ">";
        }
        return current + " " + name;
    }

    /**
     * 计算建议文本：若路径中存在第一个参数占位符 <...> ，截断到第一个 '>' 为止；否则末尾留空格。
     */
    private static String argAwareSuggest(String currentPath, CommandNode node) {
        int lt = currentPath.indexOf('<');
        if (lt >= 0) {
            int gt = currentPath.indexOf('>', lt + 1);
            if (gt > lt) {
                return currentPath.substring(0, gt + 1) + " ";
            }
        }
        if (node instanceof ArgumentNode<?> || node instanceof CustomArgumentNode<?, ?>) {
            if (!currentPath.contains("<" + node.getName() + ">")) {
                currentPath = currentPath + " <" + node.getName() + ">";
            }
            return currentPath + " ";
        }
        return currentPath + " ";
    }

    private static String labelFor(CommandNode node, CommandTreeStyle style) {
        boolean arg = CommandNodeUtils.isArgument(node);
        String open = arg ? style.openArgument() : style.openLiteral();
        String close = arg ? style.closeArgument() : style.closeLiteral();
        String base = CommandNodeUtils.baseToken(node);
        StringBuilder sb = new StringBuilder(open).append(escapeMini(base)).append(close);
        if (node instanceof ExecutableNode) {
            sb.append(" ").append(style.openSymbol()).append("*").append(style.closeSymbol());
        }
        if (node instanceof SuggestableNode) {
            sb.append(" ").append(style.openSymbol()).append("~").append(style.closeSymbol());
        }
        if (node instanceof RedirectableNode r && r.getRedirectTarget() != null) {
            sb.append(" ").append(style.openRedirect()).append("->");
            if (r.isFork()) sb.append("(fork)");
            sb.append(style.closeRedirect());
        }
        return sb.toString();
    }

    private static String clickable(String text, String suggest, String hover) {
        return "<click:suggest_command:'" + escapeAttr(suggest) + "'>" +
                "<hover:show_text:'" + escapeAttr(hover) + "'>" + text + "</hover></click>";
    }

    private static String stripTags(String labeled) {
        return labeled.replaceAll("<[^>]+>", "");
    }

    private static String escapeMini(String s) {
        return (s == null) ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttr(String s) {
        return (s == null) ? "" : s.replace("'", "\\'").replace("\n", " ");
    }

    // ======================== 统计辅助 ========================
    /** 统计多根（按权限过滤后）的总行数 */
    public static int totalLines(Collection<? extends CommandNode> roots, CommandSourceStack source) {
        if (roots == null || roots.isEmpty()) return 0;
        List<CommandNode> sorted = new ArrayList<>(roots);
        sorted.sort(Comparator.comparing(CommandNode::getName));
        int sum = 0;
        for (CommandNode n : sorted) {
            if (CommandNodeUtils.isAllowed(n, source)) sum += buildModel(n, source).lines;
        }
        return sum;
    }

    /** 统计单根（按权限过滤后）的总行数 */
    public static int totalLines(CommandNode root, CommandSourceStack source) {
        Objects.requireNonNull(root, "root");
        if (!CommandNodeUtils.isAllowed(root, source)) return 0;
        return buildModel(root, source).lines;
    }

    /** 计算总页数（ceil(lines/pageSize)，pageSize<=0 按 1）- 多根 */
    public static int totalPages(Collection<? extends CommandNode> roots, CommandSourceStack source, int pageSize) {
        int ps = Math.max(1, pageSize);
        int lines = totalLines(roots, source);
        return lines == 0 ? 0 : (int) Math.ceil(lines / (double) ps);
    }

    /** 计算总页数（ceil(lines/pageSize)，pageSize<=0 按 1）- 单根 */
    public static int totalPages(CommandNode root, CommandSourceStack source, int pageSize) {
        int ps = Math.max(1, pageSize);
        int lines = totalLines(root, source);
        return lines == 0 ? 0 : (int) Math.ceil(lines / (double) ps);
    }
}
