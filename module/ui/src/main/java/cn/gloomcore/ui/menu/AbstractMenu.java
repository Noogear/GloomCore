package cn.gloomcore.ui.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractMenu implements InventoryHolder {
    protected final UUID playerId;
    protected final Function<Player, Component> title;
    private final Inventory inventory;

    protected AbstractMenu(UUID playerId, Function<Player, Component> title) {
        this.playerId = playerId;
        this.title = title;
    }
}
