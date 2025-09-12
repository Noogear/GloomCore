package gloomcore.paper.command.util;

import gloomcore.paper.command.interfaces.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 命令节点通用辅助方法集合，提供子节点获取、基础 token 与描述处理，以及权限/条件组合。
 *
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
    public static Collection<CommandNode> childrenOf(CommandNode node) {
        if (node instanceof ParentNode p) {
            Collection<CommandNode> c = p.getChildren();
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
    public static boolean isArgument(CommandNode node) {
        return node != null && node.getNodeType() == CommandNode.NodeType.ARGUMENT;
    }

    /**
     * 返回基础 token（参数使用 {@code <name>} 形式，字面量为名称本身）。
     *
     * @param node 节点
     * @return 基础 token
     */
    public static String baseToken(CommandNode node) {
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
    public static String description(CommandNode node) {
        if (node instanceof DescribedNode d) {
            String s = d.getDescription();
            if (s != null && !s.isBlank()) {
                return s.trim();
            }
        }
        return "";
    }

    /**
     * 组合“执行要求”(RequireableNode#getRequirement) 与 “权限”(PermissionNode) 为一个统一的 Brigadier requires 谓词。
     * - 若两者均为放行（恒 true / 空权限），返回 ALWAYS_TRUE 以避免冗余注册。
     * - 返回的谓词与 {@link #isAllowed(CommandNode, CommandSourceStack)} 判定相一致。
     *
     * @param node 目标节点
     * @return 合成后的 requires 谓词（可能为 ALWAYS_TRUE）
     */
    public static Predicate<CommandSourceStack> effectiveRequirement(CommandNode node) {
        Predicate<CommandSourceStack> req = RequireableNode.ALWAYS_TRUE;
        if (node instanceof RequireableNode r) {
            Predicate<CommandSourceStack> base = r.getRequirement();
            if (base != null && base != RequireableNode.ALWAYS_TRUE) {
                req = base;
            }
        }
        if (node instanceof PermissionNode p) {
            String perm = p.getPermission();
            if (perm != null && !perm.isBlank()) {
                Predicate<CommandSourceStack> permPred = src -> src.getSender().hasPermission(perm);
                req = (req == RequireableNode.ALWAYS_TRUE) ? permPred : req.and(permPred);
            }
        }
        return req;
    }

    /**
     * 判定节点对给定来源是否允许（权限与 requirement 组合）。
     *
     * @param node   节点
     * @param source 来源
     * @return 是否允许
     */
    public static boolean isAllowed(CommandNode node, CommandSourceStack source) {
        return effectiveRequirement(node).test(source);
    }
}
