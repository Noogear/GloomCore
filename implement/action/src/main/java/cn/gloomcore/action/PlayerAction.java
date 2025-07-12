package cn.gloomcore.action;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface PlayerAction extends Action {

    void run(Player player, BooleanConsumer callback);

    @Override
    default void run(Player player) {
        run(player, null);
    }
}
