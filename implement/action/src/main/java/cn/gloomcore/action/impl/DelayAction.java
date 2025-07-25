package cn.gloomcore.action.impl;

import cn.gloomcore.action.PlayerAction;
import cn.gloomcore.replacer.PlaceholderUtil;
import cn.gloomcore.scheduler.entity.EntityScheduler;
import com.google.common.primitives.Longs;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;
import java.util.function.Function;

public class DelayAction implements PlayerAction {
    private final EntityScheduler entityScheduler;
    private final Function<Player, Long> delay;

    protected DelayAction(Function<Player, Long> delay) {
        this.delay = delay;
        this.entityScheduler = EntityScheduler.get(JavaPlugin.getProvidingPlugin(DelayAction.class));
    }

    public static PlayerAction initFromString(String s) {
        if (s.isEmpty()) {
            return null;
        }
        if (PlaceholderUtil.checkPapi(s)) {
            return new DelayAction((player -> Longs.tryParse(PlaceholderUtil.parsePapi(s, player))));
        } else {
            Long delayTick = Longs.tryParse(s);
            if (delayTick == null) {
                return null;
            }
            return new DelayAction((player -> delayTick));
        }
    }

    @Override
    public void run(Player player, Consumer<Boolean> callback) {
        if (callback != null) {
            Long delayTick = this.delay.apply(player);
            if (delayTick == null) {
                callback.accept(false);
                return;
            }
            entityScheduler.runLater(player,
                    () -> callback.accept(true),
                    () -> callback.accept(false),
                    delayTick);
        }
    }
}
