package cn.gloomcore.action.impl;

import cn.gloomcore.action.Action;
import cn.gloomcore.action.DefaultAction;
import cn.gloomcore.replacer.ReplacerUtil;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Function;

public class CommandAction implements DefaultAction {
    private final Function<Player, String> command;

    protected CommandAction(Function<Player, String> command) {
        this.command = command;
    }

    @Override
    public Action initFromString(String s) {
        if (s.isEmpty()) {
            return null;
        }
        if (ReplacerUtil.checkPapi(s)) {
            return new CommandAction((player) -> ReplacerUtil.parsePapi(s, player));
        } else {
            return new CommandAction((player) -> s);
        }
    }

    @Override
    public void run(Player player, Consumer<Boolean> callback) {
        player.performCommand(command.apply(player));
        callback.accept(true);
    }
}
