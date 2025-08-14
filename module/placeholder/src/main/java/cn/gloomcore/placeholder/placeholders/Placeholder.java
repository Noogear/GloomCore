package cn.gloomcore.placeholder.placeholders;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Placeholder {

    @NotNull
    String request(@Nullable OfflinePlayer player);

    @NotNull
    default String request() {
        return request(null);
    }

    @NotNull
    default Placeholder withoutRequesting(@NotNull String text) {
        return player -> text;
    }

}
