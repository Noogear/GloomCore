package gloomcore.paper.command.interfaces;

import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 为命令节点声明所需权限（与 IRequireable 组合，二者需同时满足）。
 */
public interface IPermission {
    /**
     * 返回所需权限（null/空白表示不需要权限）。
     *
     * @return 权限节点字符串或 null
     */
    String getPermission();

    /**
     * 基于 CommandSourceStack 判断是否具备权限。
     *
     * @param source 调用来源
     * @return true=允许，false=无权限
     */
    default boolean hasPermission(CommandSourceStack source) {
        String perm = getPermission();
        if (perm == null || perm.isBlank()) {
            return true;
        }
        return source.getSender().hasPermission(perm);
    }
}
