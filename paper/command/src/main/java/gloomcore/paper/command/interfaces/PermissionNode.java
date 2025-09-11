package gloomcore.paper.command.interfaces;

/**
 * 为命令节点声明所需权限（与 IRequireable 组合，二者需同时满足）。
 */
public interface PermissionNode {
    /**
     * 返回所需权限（null/空白表示不需要权限）。
     *
     * @return 权限节点字符串或 null
     */
    String getPermission();

}
