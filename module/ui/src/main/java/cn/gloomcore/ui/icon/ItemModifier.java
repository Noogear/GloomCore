package cn.gloomcore.ui.icon;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ItemModifier {

    @NotNull ItemStack modify(@NotNull ItemStack original);

}
