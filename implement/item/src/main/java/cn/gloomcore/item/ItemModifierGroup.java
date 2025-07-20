package cn.gloomcore.item;

import cn.gloomcore.replacer.ReplacerCache;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemModifierGroup implements ItemModifier {
    protected final List<ItemModifier> itemModifiers;

    public ItemModifierGroup(List<ItemModifier> itemModifiers) {
        this.itemModifiers = itemModifiers;
    }

    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack original, @Nullable ReplacerCache replacerCache) {
        if (itemModifiers != null && !itemModifiers.isEmpty()) {
            for (ItemModifier itemModifier : itemModifiers) {
                original = itemModifier.modify(original, replacerCache);
            }
        }
        return original;
    }
}
