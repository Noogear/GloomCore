package cn.gloomcore.item.impl;

import cn.gloomcore.common.ObjectUtil;
import cn.gloomcore.item.ItemMetaModify;
import cn.gloomcore.item.ItemModifier;
import cn.gloomcore.replacer.ReplacerCache;
import cn.gloomcore.replacer.ReplacerUtil;
import com.google.common.primitives.Ints;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomModelDataModifier implements ItemMetaModify {
    private final String customModelData;

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
        if (ReplacerUtil.checkPapi(string)) {
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
    public @NotNull ItemMeta modifyMeta(@NotNull ItemMeta meta, @Nullable ReplacerCache replacerCache) {
        Integer integer = Ints.tryParse(replacerCache == null ? customModelData : replacerCache.get(customModelData));
        if (integer != null) {
            meta.setCustomModelData(integer);
        }
        return meta;
    }

}
