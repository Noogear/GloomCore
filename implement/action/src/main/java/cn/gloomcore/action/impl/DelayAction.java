package cn.gloomcore.action.impl;

import cn.gloomcore.action.DefaultAction;
import cn.gloomcore.replacer.ReplacerUtil;
import com.google.common.primitives.Longs;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Function;

public class DelayAction implements DefaultAction {
    private Function<Player, Long> delay;

    @Override
    public boolean initFromString(String s) {
        if (s.isEmpty()) {
            return false;
        }
        if (ReplacerUtil.checkPapi(s)) {
            delay = (player -> Longs.tryParse(ReplacerUtil.parsePapi(s, player)));
        } else {
            Long delayTick = Longs.tryParse(s);
            if (delayTick == null) {
                return false;
            }
            delay = (player -> delayTick);
        }
        return true;
    }

    @Override
    public void run(Player player, Consumer<Boolean> callback) {
        Long delayTick = this.delay.apply(player);
        if (delayTick == null) {
            callback.accept(false);
        }

    }
}
