package cn.gloomcore.action.impl;

import cn.gloomcore.action.Action;
import cn.gloomcore.action.PlayerAction;
import cn.gloomcore.replacer.ReplacerUtil;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class TellAction implements PlayerAction {
    private final Function<Player, Component> message;

    protected TellAction(Function<Player, Component> message) {
        this.message = message;
    }

    public static Action initFromString(String s) {
        if (s.isEmpty()) {
            return null;
        }
        if (ReplacerUtil.checkPapi(s)) {
            return new TellAction((player) -> MiniMessage.miniMessage().deserialize(ReplacerUtil.parsePapi(s, player)));
        } else {
            Component component = MiniMessage.miniMessage().deserialize(s);
            return new TellAction((player) -> component);
        }
    }

    @Override
    public void run(Player player, BooleanConsumer callback) {
        player.sendMessage(message.apply(player));
        callback.accept(true);
    }
}
