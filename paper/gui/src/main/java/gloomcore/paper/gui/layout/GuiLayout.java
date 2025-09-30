package gloomcore.paper.gui.layout;

import org.bukkit.event.inventory.InventoryType;

public interface GuiLayout {

    int width();

    int height();

    InventoryType inventoryType();

    int size();
}
