package cn.gloomcore.item.impl;

import cn.gloomcore.common.ObjectUtil;
import cn.gloomcore.item.ItemMetaModifier;
import cn.gloomcore.item.ItemModifier;
import cn.gloomcore.replacer.PlaceholderUtil;
import cn.gloomcore.replacer.StringReplacer;
import com.google.common.primitives.Ints;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomModelDataModifier implements ItemMetaModifier {
    protected final String customModelData;

    protected CustomModelDataModifier(String customModelData) {
        this.customModelData = customModelData;
    }

    public static ItemModifier initFromObject(ItemStack original, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            original.editMeta(meta -> {
                meta.setCustomModelData(ObjectUtil.toInt(value));
            });
            return null;
        }
        String string = ObjectUtil.toString(value);
        if (PlaceholderUtil.checkPapi(string)) {
            return new CustomModelDataModifier(string);
        } else {
            Integer integer = Ints.tryParse(string);
            if (integer != null) {
                original.editMeta(meta -> {
                    meta.setCustomModelData(integer);
                });
            }
            return null;
        }
    }

    @Override
    public @NotNull ItemMeta modifyMeta(@NotNull ItemMeta meta, @Nullable StringReplacer replacer) {
        Integer integer = Ints.tryParse(replacer == null ? customModelData : replacer.apply(customModelData));
        if (integer != null) {
            meta.setCustomModelData(integer);
        }
        return meta;
    }

}
