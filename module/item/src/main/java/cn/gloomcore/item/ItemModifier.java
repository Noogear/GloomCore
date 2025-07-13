package cn.gloomcore.item;

import cn.gloomcore.replacer.ReplacerCache;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemModifier extends BaseModify<ItemStack> {

    @NotNull ItemStack modify(@NotNull ItemStack original, @Nullable ReplacerCache replacerCache);

    @Override
    default @NotNull ItemStack modify(@NotNull ItemStack original) {
        return modify(original, null);
    }
}
