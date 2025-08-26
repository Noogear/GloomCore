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
            return (player) -> itemModifier.modify(itemStack.clone());
        } else {
            return of(itemStack);
        }
    }

    static @NotNull IconDisplay of(@Nullable ItemModifier itemModifier) {
        return of(DEFAULT_ICON, itemModifier);
    }

    static @NotNull IconDisplay of(@NotNull ItemStack itemStack) {
        return (player) -> itemStack.clone();
    }

    static @NotNull IconDisplay of() {
        return (player) -> DEFAULT_ICON.clone();
    }

    static @NotNull IconDisplay empty() {
        return (player) -> ItemStack.empty();
    }

    default IconDisplay snapshot() {
        return IconDisplay.of(this.parse());
    }

    default @NotNull ItemStack parse() {
        return parse(null);
    }

    @NotNull ItemStack parse(@Nullable Player player);

}
