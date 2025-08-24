package cn.gloomcore.ui.icon;

import cn.gloomcore.item.ItemModifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IconDisplay {

    ItemStack DEFAULT_ICON = new ItemStack(Material.STONE);

    static @NotNull IconDisplay of(@NotNull ItemStack itemStack, @Nullable ItemModifier itemModifier) {
        if (itemModifier != null) {
            return () -> itemModifier.modify(itemStack.clone());
        } else {
            return of(itemStack);
        }
    }

    static @NotNull IconDisplay of(@Nullable ItemModifier itemModifier) {
        return of(DEFAULT_ICON, itemModifier);
    }

    static @NotNull IconDisplay of(@NotNull ItemStack itemStack) {
        return itemStack::clone;
    }

    static @NotNull IconDisplay of() {
        return DEFAULT_ICON::clone;
    }

    static @NotNull IconDisplay empty() {
        return ItemStack::empty;
    }

    @NotNull ItemStack parse();

    default ItemStack parse(@Nullable Player player) {
        return parse();
    }

}
