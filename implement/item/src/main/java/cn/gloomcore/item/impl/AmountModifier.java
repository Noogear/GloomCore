package cn.gloomcore.item.impl;

import cn.gloomcore.common.ObjectUtil;
import cn.gloomcore.item.ItemModifier;
import cn.gloomcore.replacer.ReplacerCache;
import cn.gloomcore.replacer.ReplacerUtil;
import com.google.common.primitives.Ints;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AmountModifier implements ItemModifier {
    private final String amount;

    protected AmountModifier(String amount) {
        this.amount = amount;
    }

    public static ItemModifier initFromObject(ItemStack original, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            original.setAmount(ObjectUtil.toInt(value));
            return null;
        }
        String string = ObjectUtil.toString(value);
        if (ReplacerUtil.checkPapi(string)) {
            return new AmountModifier(string);
        } else {
            Integer integer = Ints.tryParse(string);
            if (integer != null) {
                original.setAmount(integer);
            }
            return null;
        }
    }

    @Override
    public @NotNull ItemStack modify(@NotNull ItemStack original, @Nullable ReplacerCache replacerCache) {
        Integer integer = Ints.tryParse(replacerCache == null ? amount : replacerCache.get(amount));
        if (integer != null) {
            original.setAmount(integer);
        }
        return original;
    }


}
