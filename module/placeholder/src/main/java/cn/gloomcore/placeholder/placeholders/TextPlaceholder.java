package cn.gloomcore.placeholder.placeholders;


import cn.gloomcore.placeholder.PlaceholderManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TextPlaceholder {

    @NotNull
    String process(@Nullable OfflinePlayer player);

    @NotNull
    default String process() {
        return process(null);
    }

    @NotNull
    default TextPlaceholder withoutRequesting(@NotNull String text) {
        return player -> text;
    }

    @NotNull
    default TextPlaceholder ofCache(boolean isShared, PlaceholderManager manager, final long intervalMillis, final TextPlaceholder placeholder) {
        if (isShared || manager == null) {
            return new TextPlaceholder() {
                private String cacheText = "";
                private long lastTime = 0;

                @Override
                public @NotNull String process(@Nullable OfflinePlayer player) {
                    if (System.currentTimeMillis() - lastTime > intervalMillis) {
                        lastTime = System.currentTimeMillis();
                        return cacheText = placeholder.process(player);
                    }
                    return cacheText;
                }
            };
        } else {
            return new TextPlaceholder() {



            };
        }


    }

}
