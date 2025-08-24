package cn.gloomcore.item;

import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemModifier extends BaseModify<ItemStack> {

    @NotNull ItemStack modify(@NotNull ItemStack original);

}
