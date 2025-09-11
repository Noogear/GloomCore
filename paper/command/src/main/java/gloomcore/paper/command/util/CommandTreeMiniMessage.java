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
     * 计算建议文本：若路径中存在第一个参数占位符 <...> ，截断到第一个 '>' 为止；
     * 否则在末尾追加空格便于继续输入。
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
            if (r.isFork()) {
                sb.append("(fork)");
            }
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
}
