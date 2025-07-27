package cn.gloomcore.action;

import cn.gloomcore.replacer.StringReplacer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlayerAction extends Action {

    boolean runByPlayer(@NotNull Player player, @Nullable StringReplacer replacer);

    @Override
    default void run(@Nullable Player player, @Nullable BooleanConsumer callback, @Nullable StringReplacer replacer) {
        if (callback != null) {
            callback.accept(player != null && runByPlayer(player, replacer));
        }
    }

}
