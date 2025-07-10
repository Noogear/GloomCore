package cn.gloomcore.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.Supplier;

public abstract class AbstractGUI implements InventoryHolder {

    private Supplier<Component> title;

    public AbstractGUI setTitle(Supplier<Component> title) {
        this.title = title;
        return this;
    }

    public AbstractGUI setTitle(Component title) {
        this.title = () -> title;
        return this;
    }

    public Component title() {
        return title.get();
    }

}
