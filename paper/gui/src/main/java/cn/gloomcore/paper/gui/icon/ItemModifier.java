package cn.gloomcore.paper.gui.icon;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ItemModifier {

    @NotNull ItemStack modify(@NotNull ItemStack original);

}
