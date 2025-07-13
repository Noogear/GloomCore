package cn.gloomcore.gui.display;

import cn.gloomcore.item.ItemModifier;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModifyDisplay {
    private final List<ItemModifier> modifies;

    public ModifyDisplay(List<ItemModifier> modifies) {
        this.modifies = modifies;
    }

    public ModifyDisplay() {
        this(new ArrayList<>());
    }

    public ModifyDisplay addModify(ItemModifier modify) {
        modifies.add(modify);
        return this;
    }

    public ItemStack modify(ItemStack itemStack) {
        if (modifies.isEmpty()) {
            return itemStack;
        }
        for (ItemModifier modify : modifies) {
            modify.modify(itemStack);
        }
        return itemStack;
    }

    public boolean isEmpty() {
        return modifies == null || modifies.isEmpty();
    }


}
