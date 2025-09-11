package gloomcore.paper.command.util;

import gloomcore.paper.command.framework.AbstractCommandNode;
import gloomcore.paper.command.interfaces.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 将自定义命令节点树渲染为可读 ASCII 文本（稳定排序便于调试）。
 *
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
    public static String toText(CommandNode root) {
        Objects.requireNonNull(root, "root");
        StringBuilder sb = new StringBuilder();
        if (!(root instanceof AbstractCommandNode n)) {
            sb.append(root.getName()).append('\n');
            return sb.toString();
        }
        appendNode(sb, n, "", true);
        return sb.toString();
    }


    private static void appendNode(StringBuilder sb, AbstractCommandNode node, String prefix, boolean tail) {
        sb.append(prefix)
                .append(tail ? "└─ " : "├─ ")
                .append(labelFor(node))
                .append('\n');

        List<CommandNode> children = new ArrayList<>(CommandNodeUtils.childrenOf(node));
        children.sort(Comparator.comparing(CommandNode::getName));
        int size = children.size();
        for (int i = 0; i < size; i++) {
            CommandNode child = children.get(i);
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
        if (node instanceof ExecutableNode) {
            sb.append(" *");
        }
        if (node instanceof SuggestableNode) {
            sb.append(" ~");
        }
        if (node instanceof RedirectableNode r && r.getRedirectTarget() != null) {
            sb.append(r.isFork() ? " ->(fork)" : " ->");
        }
        if (node instanceof DescribedNode d) {
            String desc = d.getDescription();
            if (desc != null && !desc.isBlank()) {
                sb.append("  // ").append(desc.trim());
            }
        }
        return sb.toString();
    }
}
