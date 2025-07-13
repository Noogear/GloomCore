package cn.gloomcore.action.impl;

import cn.gloomcore.action.PlayerAction;
import cn.gloomcore.replacer.ReplacerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConsoleAction implements PlayerAction {
    private final Function<Player, String> command;

    protected ConsoleAction(Function<Player, String> command) {
        this.command = command;
    }

    public static PlayerAction initFromString(String s) {
        if (s.isEmpty()) {
            return null;
        }
        if (ReplacerUtil.checkPapi(s)) {
            return new ConsoleAction((player) -> ReplacerUtil.parsePapi(s, player));
        } else {
            return new ConsoleAction((player) -> s);
        }
    }

    @Override
    public void run(Player player, Consumer<Boolean> callback) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.apply(player));
        if (callback != null) {
            callback.accept(true);
        }
    }
}
