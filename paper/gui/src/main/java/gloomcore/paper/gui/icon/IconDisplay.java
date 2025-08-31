package gloomcore.paper.gui.icon;

import gloomcore.contract.template.ITemplate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 图标显示接口，用于根据玩家信息生成物品显示
 * <p>
 * 该接口继承自ITemplate接口，定义了如何根据Player上下文生成ItemStack的模板规范。
 * </p>
 */
public interface IconDisplay extends ITemplate<Player, ItemStack> {
}
