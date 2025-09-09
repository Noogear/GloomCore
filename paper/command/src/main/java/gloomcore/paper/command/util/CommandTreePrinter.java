package gloomcore.paper.command.util;

import gloomcore.paper.command.framework.AbstractCommandNode;
import gloomcore.paper.command.interfaces.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 将自定义命令节点树渲染为可读的 ASCII 文本。
 * 调用时机：在命令加载 / 注册完成构建根节点后调用。
 */
public final class CommandTreePrinter {

    private CommandTreePrinter() {
    }

    /**
     * 打印单棵命令树。
     */
    public static String toText(ICommandNode root) {
        Objects.requireNonNull(root, "root");
        StringBuilder sb = new StringBuilder();
        if (!(root instanceof AbstractCommandNode n)) {
            sb.append(root.getName()).append("\n");
            return sb.toString();
        }
        appendNode(sb, n, "", true);
        return sb.toString();
    }

    /**
     * 打印多棵根命令树（例如注册了多个根命令）。
     */
    public static String toText(Collection<? extends ICommandNode> roots) {
        StringBuilder sb = new StringBuilder();
        if (roots == null || roots.isEmpty()) return "";
        int i = 0, size = roots.size();
        for (ICommandNode node : roots) {
            boolean last = (++i == size);
            if (node instanceof AbstractCommandNode n) {
                appendNode(sb, n, "", last);
            } else {
                sb.append(last ? "└─ " : "├─ ")
                        .append(node.getName())
                        .append("\n");
            }
        }
        return sb.toString();
    }

    private static void appendNode(StringBuilder sb, AbstractCommandNode node, String prefix, boolean tail) {
        sb.append(prefix)
                .append(tail ? "└─ " : "├─ ")
                .append(labelFor(node))
                .append("\n");

        List<ICommandNode> children = new ArrayList<>(safeChildren(node));
        for (int i = 0; i < children.size(); i++) {
            ICommandNode child = children.get(i);
            boolean last = (i == children.size() - 1);
            String childPrefix = prefix + (tail ? "   " : "│  ");
            if (child instanceof AbstractCommandNode n) {
                appendNode(sb, n, childPrefix, last);
            } else {
                sb.append(childPrefix)
                        .append(last ? "└─ " : "├─ ")
                        .append(child.getName())
                        .append("\n");
            }
        }
    }

    private static String labelFor(AbstractCommandNode node) {
        String base;

        if (Objects.requireNonNull(node.getNodeType()) == ICommandNode.NodeType.ARGUMENT) {
            base = "<" + node.getName() + ">";
        } else {
            base = node.getName();
        }

        StringBuilder sb = new StringBuilder(base);
        if (node instanceof IExecutable) sb.append(" *");
        if (node instanceof ISuggestable) sb.append(" ~");
        if (node instanceof IRedirectable r) {
            if (r.getRedirectTarget() != null) {
                sb.append(r.isFork() ? " ->(fork)" : " ->");
            }
        }
        // 追加描述（若实现 IDescribed）
        if (node instanceof IDescribed d) {
            String desc = d.getDescription();
            if (desc != null && !desc.isBlank()) sb.append("  // ").append(desc.trim());
        }
        return sb.toString();
    }

    private static String simpleTypeName(Object o) {
        return (o == null) ? "?" : o.getClass().getSimpleName();
    }

    private static Collection<ICommandNode> safeChildren(ICommandNode node) {
        if (node instanceof IParentNode p) {
            Collection<ICommandNode> c = p.getChildren();
            return (c == null) ? List.of() : c;
        }
        return List.of();
    }
}
