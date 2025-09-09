package gloomcore.paper.command.util;

import gloomcore.paper.command.framework.LiteralNode;
import gloomcore.paper.command.framework.argument.ArgumentNode;
import gloomcore.paper.command.framework.argument.CustomArgumentNode;
import gloomcore.paper.command.interfaces.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 将命令节点树渲染为 MiniMessage 文本，带权限过滤与点击建议命令（到第一个参数为止）。
 * 返回的是 MiniMessage 字符串，调用方用 MiniMessage.miniMessage().deserialize(...) 发送。
 */
public final class CommandTreeMiniMessage {

    private CommandTreeMiniMessage() {
    }

    public static String toMiniMessage(Collection<? extends ICommandNode> roots, CommandSourceStack source) {
        if (roots == null || roots.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0, size = roots.size();
        for (ICommandNode node : roots) {
            boolean last = (++i == size);
            if (isAllowed(node, source)) {
                String rootPath = "/" + node.getName();
                appendNode(sb, node, source, "", last, rootPath);
            }
        }
        return sb.toString();
    }

    public static String toMiniMessage(ICommandNode root, CommandSourceStack source) {
        Objects.requireNonNull(root, "root");
        if (!isAllowed(root, source)) return "";
        StringBuilder sb = new StringBuilder();
        appendNode(sb, root, source, "", true, "/" + root.getName());
        return sb.toString();
    }

    private static void appendNode(StringBuilder sb,
                                   ICommandNode node,
                                   CommandSourceStack source,
                                   String prefix,
                                   boolean tail,
                                   String currentPath) {
        CommandTreeStyle style = CommandTreeStyle.get();
        String label = labelFor(node, style);
        String desc = descriptionOf(node);
        String hover = desc.isEmpty() ? stripTags(label) : (stripTags(label) + "\n" + desc);
        String suggest = argAwareSuggest(currentPath, node);

        // 树枝前缀与连接线加颜色
        sb.append(style.openTree()).append(prefix).append(tail ? "└─ " : "├─ ").append(style.closeTree())
                .append(clickable(label, suggest, hover));
        if (!desc.isEmpty())
            sb.append(" ").append(style.openDescription()).append("- ").append(escapeMini(desc)).append(style.closeDescription());
        sb.append("\n");

        List<ICommandNode> children = new ArrayList<>(safeChildren(node));
        int shown = 0;
        for (ICommandNode child : children) if (isAllowed(child, source)) shown++;
        int seen = 0;
        for (ICommandNode child : children) {
            if (!isAllowed(child, source)) continue;
            boolean childLast = (++seen == shown);
            String childPrefix = prefix + (tail ? "   " : "│  ");
            String childPath = nextPath(currentPath, child);
            appendNode(sb, child, source, childPrefix, childLast, childPath);
        }
    }

    private static String nextPath(String current, ICommandNode node) {
        String name = node.getName();
        if (node instanceof LiteralNode) {
            return current + " " + name;
        }
        if (node instanceof ArgumentNode<?> || node instanceof CustomArgumentNode<?, ?>) {
            return current + " <" + name + ">";
        }
        return current + " " + name;
    }

    // 计算建议文本：若路径中存在第一个参数占位符 <...> ，则截断到第一个 '>' 为止；否则在末尾追加空格便于继续输入。
    private static String argAwareSuggest(String currentPath, ICommandNode node) {
        int lt = currentPath.indexOf('<');
        if (lt >= 0) {
            int gt = currentPath.indexOf('>', lt + 1);
            if (gt > lt) {
                String upToArg = currentPath.substring(0, gt + 1);
                return upToArg + " ";
            }
        }
        // 没遇到过参数，占位：如果当前节点就是参数，确保包含它自身占位
        if (node instanceof ArgumentNode<?> || node instanceof CustomArgumentNode<?, ?>) {
            if (!currentPath.contains("<" + node.getName() + ">")) {
                currentPath = currentPath + " <" + node.getName() + ">";
            }
            return currentPath + " ";
        }
        return currentPath + " ";
    }

    private static boolean isAllowed(ICommandNode node, CommandSourceStack source) {
        if (node instanceof IRequireable r) {
            Predicate<CommandSourceStack> req = r.getRequirement();
            if (req != null && !req.test(source)) return false;
        }
        if (node instanceof IPermission p) {
            return p.hasPermission(source);
        }
        return true;
    }

    private static String labelFor(ICommandNode node, CommandTreeStyle style) {
        boolean isArgument = (node.getNodeType() == ICommandNode.NodeType.ARGUMENT);
        String open = isArgument ? style.openArgument() : style.openLiteral();
        String close = isArgument ? style.closeArgument() : style.closeLiteral();

        String base = isArgument ? ("<" + node.getName() + ">") : node.getName();

        StringBuilder sb = new StringBuilder(open).append(escapeMini(base)).append(close);
        if (node instanceof IExecutable)
            sb.append(" ").append(style.openSymbol()).append("*").append(style.closeSymbol());
        if (node instanceof ISuggestable)
            sb.append(" ").append(style.openSymbol()).append("~").append(style.closeSymbol());
        if (node instanceof IRedirectable r && r.getRedirectTarget() != null) {
            sb.append(" ").append(style.openRedirect()).append("->");
            if (r.isFork()) sb.append("(fork)");
            sb.append(style.closeRedirect());
        }
        return sb.toString();
    }

    private static String descriptionOf(ICommandNode node) {
        if (node instanceof IDescribed d) {
            String s = d.getDescription();
            if (s != null && !s.isBlank()) return s.trim();
        }
        return "";
    }

    private static String clickable(String text, String suggest, String hover) {
        return "<click:suggest_command:'" + escapeAttr(suggest) + "'><hover:show_text:'" + escapeAttr(hover) + "'>" + text + "</hover></click>";
    }

    private static String stripTags(String labeled) {
        return labeled.replaceAll("<[^>]+>", "");
    }

    private static Collection<ICommandNode> safeChildren(ICommandNode node) {
        if (node instanceof IParentNode p) {
            Collection<ICommandNode> c = p.getChildren();
            return (c == null) ? List.of() : c;
        }
        return List.of();
    }

    private static String escapeMini(String s) {
        if (s == null) return "";
        return s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttr(String s) {
        if (s == null) return "";
        return s.replace("'", "\\'").replace("\n", " ");
    }
}
