package cn.gloomcore.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.Function;

public abstract class AbstractGui implements InventoryHolder {

    private Function<Player, Component> title;

    public AbstractGui setTitle(Function<Player, Component> title) {
        this.title = title;
        return this;
    }

    public AbstractGui setTitle(Component title) {
        this.title = (player) -> title;
        return this;
    }

    public Component title(Player player) {
        return title.apply(player);
    }


}
