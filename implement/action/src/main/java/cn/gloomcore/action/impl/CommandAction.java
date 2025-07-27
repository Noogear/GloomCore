package cn.gloomcore.action.impl;

import cn.gloomcore.action.PlayerAction;
import cn.gloomcore.replacer.PlaceholderUtil;
import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CommandAction implements PlayerAction {
    private final Function<Player, String> command;

    protected CommandAction(Function<Player, String> command) {
        this.command = command;
    }

    public static PlayerAction initFromString(String s) {
        if (s.isEmpty()) {
            return null;
        }
        if (PlaceholderUtil.checkPapi(s)) {
            return new CommandAction((player) -> PlaceholderUtil.parsePapi(s, player));
        } else {
            return new CommandAction((player) -> s);
        }
    }

    @Override
    public boolean runByPlayer(@NotNull Player player, @Nullable StringReplacer replacer) {
        String command = this.command.apply(player);
        return player.performCommand(replacer != null ? replacer.apply(command, player.getUniqueId()) : command);
    }

}
