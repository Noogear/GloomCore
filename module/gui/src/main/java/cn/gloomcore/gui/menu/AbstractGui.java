package cn.gloomcore.gui.menu;

import cn.gloomcore.gui.icon.Icon;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractGui implements Menu {
    protected final Function<Player, Component> title;
    protected final Map<Integer, Icon> slotMap;
    protected Player owner;
    protected @Nullable Inventory inventoryCache;

    protected AbstractGui(Function<Player, Component> title, Map<Integer, Icon> slotMap) {
        this.title = title;
        this.slotMap = slotMap;
    }

    protected AbstractGui(Component title, Map<Integer, Icon> slotMap) {
        this(player -> title, slotMap);
    }

    @Override
    public Component title() {
        return title.apply(owner);
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public Icon onClick(InventoryClickEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (!topInv.equals(event.getClickedInventory())) {
            event.setCancelled(true);
            return null;
        }
        Icon icon = slotMap.get(event.getSlot());
        if (icon == null) {
            event.setCancelled(true);
            return null;
        }
        return icon.onClick(event);
    }

    @Override
    public boolean isEmpty(int slot) {
        return getItemStack(slot) == null;
    }

    @Override
    public ItemStack getItemStack(int slot) {
        return getInventory().getItem(slot);
    }

    public void setSlot(int slot, Icon icon) {
        slotMap.put(slot, icon);
    }

    public void setSlotIfAbsent(int slot, Icon icon) {
        if (!isEmpty(slot)) {
            setSlot(slot, icon);
        }
    }

    public void removeSlot(int slot) {
        slotMap.remove(slot);
    }

    @Override
    public void open() {
        owner.openInventory(getInventory());
    }

    @Override
    public void close() {
        getInventory().close();
    }
}
