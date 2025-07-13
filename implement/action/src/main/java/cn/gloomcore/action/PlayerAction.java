package cn.gloomcore.action;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface PlayerAction extends ConsumerAction<Boolean,Player > {

    @Override
    void run(Player player, Consumer<Boolean> callback);

}
