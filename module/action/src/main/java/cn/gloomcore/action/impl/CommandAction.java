package cn.gloomcore.action.impl;

import cn.gloomcore.action.DefaultAction;
import cn.gloomcore.replacer.ReplacerUtil;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Function;

public class CommandAction implements DefaultAction {
    private Function<Player, String> command;

    @Override
    public boolean initFromString(String s, Function<String, ?> argPreprocessor) {
        if (s.isEmpty()) {
            return false;
        }
        if (ReplacerUtil.contains(s)) {
            command = (player) -> ReplacerUtil.apply(s, player);
        } else {
            command = (player) -> s;
        }
        return true;
    }

    @Override
    public void run(Player player, Consumer<Boolean> callback) {
        player.performCommand(command.apply(player));
        callback.accept(true);
    }
}
