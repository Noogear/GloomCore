package cn.gloomcore.item;

import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemMetaModifier extends ItemModifier {

    @NotNull
    ItemMeta modifyMeta(@NotNull ItemMeta meta, @Nullable StringReplacer replacer);

    @Override
    default @NotNull ItemStack modify(@NotNull ItemStack original, StringReplacer replacer) {
        ItemMeta itemMeta = original.getItemMeta();
        if (itemMeta != null) {
            original.setItemMeta(this.modifyMeta(itemMeta, replacer));
        }
        return original;
    }
}
