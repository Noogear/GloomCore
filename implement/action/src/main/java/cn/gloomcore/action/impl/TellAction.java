package cn.gloomcore.action.impl;

import cn.gloomcore.action.PlayerAction;
import cn.gloomcore.replacer.PlaceholderUtil;
import cn.gloomcore.replacer.StringReplacer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TellAction implements PlayerAction {
    private final Function<Player, String> message;

    protected TellAction(Function<Player, String> message) {
        this.message = message;
    }

    public static PlayerAction initFromString(String s) {
        if (s.isEmpty()) {
            return null;
        }
        if (PlaceholderUtil.checkPapi(s)) {
            return new TellAction((player) -> PlaceholderUtil.parsePapi(s, player));
        } else {
            Component component = MiniMessage.miniMessage().deserialize(s);
            return new TellAction((player) -> s);
        }
    }

    @Override
    public boolean runByPlayer(@NotNull Player player, @Nullable StringReplacer replacer) {
        String message = this.message.apply(player);
        message = replacer != null ? replacer.apply(message, player.getUniqueId()) : message;
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
        return true;
    }
}
