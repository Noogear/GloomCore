package cn.gloomcore.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public abstract class AbstractGui implements InventoryHolder {
    private Function<Player, Component> title;
    private Player owner;

    public AbstractGui setTitle(Function<Player, Component> title) {
        this.title = title;
        return this;
    }

    public AbstractGui setTitle(Component title) {
        this.title = (player) -> title;
        return this;
    }

    public Component title() {
        return title.apply(owner);
    }

    public boolean isEmpty(int slot) {
        return getInventory().getItem(slot) == null;
    }

    public void setSlot(int slot, ItemStack item) {
        getInventory().setItem(slot, item);
    }

    public void setSlotIfAbsent(int slot, ItemStack item) {
        if (isEmpty(slot)) setSlot(slot, item);
    }

    public void open() {
        if (owner != null) {
            owner.openInventory(getInventory());
        }
    }

    public void close() {
        getInventory().close();
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Player owner() {
        return owner;
    }

}
