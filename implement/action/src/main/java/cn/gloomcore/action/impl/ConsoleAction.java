package cn.gloomcore.action.impl;

import cn.gloomcore.action.Action;
import cn.gloomcore.action.PlayerAction;
import cn.gloomcore.replacer.ReplacerUtil;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class ConsoleAction implements PlayerAction {
    private final Function<Player, String> command;

    protected ConsoleAction(Function<Player, String> command) {
        this.command = command;
    }

    public static Action initFromString(String s) {
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
    public void run(Player player, BooleanConsumer callback) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.apply(player));
        callback.accept(true);
    }
}
