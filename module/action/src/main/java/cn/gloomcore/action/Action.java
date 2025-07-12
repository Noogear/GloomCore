package cn.gloomcore.action;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface Action {

    void run(Player player);

}
