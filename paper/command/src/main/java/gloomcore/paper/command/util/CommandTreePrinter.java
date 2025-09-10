package gloomcore.paper.command.util;

import gloomcore.paper.command.framework.AbstractCommandNode;
import gloomcore.paper.command.interfaces.ICommandNode;
import gloomcore.paper.command.interfaces.IDescribed;
import gloomcore.paper.command.interfaces.IExecutable;
import gloomcore.paper.command.interfaces.IRedirectable;
import gloomcore.paper.command.interfaces.ISuggestable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 将自定义命令节点树渲染为可读 ASCII 文本（稳定排序便于调试）。
 */
public final class CommandTreePrinter {

    private CommandTreePrinter() {
    }

    /**
     * 打印单棵命令树。
     *
     * @param root 根节点
     * @return ASCII 文本
     */
    public static String toText(ICommandNode root) {
        Objects.requireNonNull(root, "root");
        StringBuilder sb = new StringBuilder();
        if (!(root instanceof AbstractCommandNode n)) {
            sb.append(root.getName()).append('\n');
            return sb.toString();
        }
        appendNode(sb, n, "", true);
        return sb.toString();
    }

    /**
     * 打印多棵根命令树。
     *
     * @param roots 根节点集合
     * @return 合并后的 ASCII 文本
     */
    public static String toText(Collection<? extends ICommandNode> roots) {
        if (roots == null || roots.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<ICommandNode> list = new ArrayList<>(roots);
        list.sort(Comparator.comparing(ICommandNode::getName));
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ICommandNode node = list.get(i);
            boolean last = (i == size - 1);
            if (node instanceof AbstractCommandNode n) {
                appendNode(sb, n, "", last);
            } else {
                sb.append(last ? "└─ " : "├─ ")
                        .append(node.getName())
                        .append('\n');
            }
        }
        return sb.toString();
    }

    private static void appendNode(StringBuilder sb, AbstractCommandNode node, String prefix, boolean tail) {
        sb.append(prefix)
                .append(tail ? "└─ " : "├─ ")
                .append(labelFor(node))
                .append('\n');

        List<ICommandNode> children = new ArrayList<>(CommandNodeUtils.childrenOf(node));
        children.sort(Comparator.comparing(ICommandNode::getName));
        int size = children.size();
        for (int i = 0; i < size; i++) {
            ICommandNode child = children.get(i);
            boolean last = (i == size - 1);
            String childPrefix = prefix + (tail ? "   " : "│  ");
            if (child instanceof AbstractCommandNode n) {
                appendNode(sb, n, childPrefix, last);
            } else {
                sb.append(childPrefix)
                        .append(last ? "└─ " : "├─ ")
                        .append(child.getName())
                        .append('\n');
            }
        }
    }

    private static String labelFor(AbstractCommandNode node) {
        String base = CommandNodeUtils.baseToken(node);
        StringBuilder sb = new StringBuilder(base);
        if (node instanceof IExecutable) {
            sb.append(" *");
        }
        if (node instanceof ISuggestable) {
            sb.append(" ~");
        }
        if (node instanceof IRedirectable r && r.getRedirectTarget() != null) {
            sb.append(r.isFork() ? " ->(fork)" : " ->");
        }
        if (node instanceof IDescribed d) {
            String desc = d.getDescription();
            if (desc != null && !desc.isBlank()) {
                sb.append("  // ").append(desc.trim());
            }
        }
        return sb.toString();
    }

    private static Collection<ICommandNode> safeChildren(ICommandNode node) { // 标记为弃用，兼容旧引用，后续可删除
        return CommandNodeUtils.childrenOf(node);
    }
}
