package gloomcore.paper.command.util;

import gloomcore.paper.command.interfaces.ICommandNode;
import gloomcore.paper.command.interfaces.IDescribed;
import gloomcore.paper.command.interfaces.IParentNode;
import gloomcore.paper.command.interfaces.IPermission;
import gloomcore.paper.command.interfaces.IRequireable;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 命令节点通用辅助方法集合，提供子节点获取、基础 token 与描述处理。
 */
public final class CommandNodeUtils {

    private CommandNodeUtils() {
    }

    /**
     * 返回节点的直接子节点集合；若节点非父节点或子集合为 null 则返回不可变空集合。
     *
     * @param node 节点
     * @return 子节点集合（可能为空不可变）
     */
    public static Collection<ICommandNode> childrenOf(ICommandNode node) {
        if (node instanceof IParentNode p) {
            Collection<ICommandNode> c = p.getChildren();
            return (c == null) ? List.of() : c;
        }
        return List.of();
    }

    /**
     * 判断是否为参数节点。
     *
     * @param node 节点
     * @return true 表示为参数
     */
    public static boolean isArgument(ICommandNode node) {
        return node != null && node.getNodeType() == ICommandNode.NodeType.ARGUMENT;
    }

    /**
     * 返回基础 token（参数使用 <name> 形式，字面量为名称本身）。
     *
     * @param node 节点
     * @return 基础 token
     */
    public static String baseToken(ICommandNode node) {
        if (node == null) {
            return "";
        }
        return isArgument(node) ? ("<" + node.getName() + ">") : node.getName();
    }

    /**
     * 获取节点描述（若实现 IDescribed 且非空）。
     *
     * @param node 节点
     * @return 描述或空串
     */
    public static String description(ICommandNode node) {
        if (node instanceof IDescribed d) {
            String s = d.getDescription();
            if (s != null && !s.isBlank()) {
                return s.trim();
            }
        }
        return "";
    }

    /**
     * 判定节点对给定来源是否允许（权限与 requirement 组合）。
     *
     * @param node   节点
     * @param source 来源
     * @return 是否允许
     */
    public static boolean isAllowed(ICommandNode node, CommandSourceStack source) {
        if (node instanceof IRequireable r) {
            Predicate<CommandSourceStack> req = r.getRequirement();
            if (req != null && !req.test(source)) {
                return false;
            }
        }
        if (node instanceof IPermission p) {
            return p.hasPermission(source);
        }
        return true;
    }
}
