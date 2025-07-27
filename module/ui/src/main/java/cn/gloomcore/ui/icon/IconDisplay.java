package cn.gloomcore.ui.icon;

import cn.gloomcore.item.ItemModifier;
import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IconDisplay {

    ItemStack DEFAULT_ICON = new ItemStack(Material.STONE);

    static @NotNull IconDisplay of(@NotNull ItemStack itemStack, @Nullable ItemModifier itemModifier) {
        return new IconDisplay() {
            @Override
            public @NotNull ItemStack item() {
                return itemStack.clone();
            }

            @Override
            public @NotNull ItemStack item(@Nullable StringReplacer replacer) {
                if (itemModifier != null) {
                    return itemModifier.modify(item(), replacer);
                }
                return item();
            }

        };
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

    @NotNull ItemStack item();

    @NotNull
    default ItemStack item(@Nullable StringReplacer replacer) {
        return item();
    }

}
