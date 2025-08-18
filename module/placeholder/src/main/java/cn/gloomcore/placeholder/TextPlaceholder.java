package cn.gloomcore.placeholder;


import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TextPlaceholder {

    @Nullable
    String process(@Nullable Player player);

    @Nullable
    default String process() {
        return process(null);
    }

    @NotNull
    default TextPlaceholder withoutRequesting(@NotNull String text) {
        return player -> text;
    }

    @NotNull
    default TextPlaceholder ofCache(final String params, final boolean isShared, final PlayerPlaceholderHandler placeholderHandler, final long intervalMillis, final TextPlaceholder nextPlaceholder) {
        if (isShared || placeholderHandler == null) {
            return new TextPlaceholder() {
                private final CacheText cacheText = new CacheText("", 0);

                @Override
                public @Nullable String process(@Nullable Player player) {
                    return process();
                }

                @Override
                public @Nullable String process() {
                    if (System.currentTimeMillis() - cacheText.getLastUpdate() > intervalMillis) {
                        return cacheText.update(nextPlaceholder.process(), System.currentTimeMillis());
                    }
                    return cacheText.getText();
                }
            };
        } else {
            return new TextPlaceholder() {
                @Override
                public @Nullable String process(@Nullable Player player) {
                    if (player != null) {
                        return placeholderHandler.getOrUpdate(player.getUniqueId(), params, intervalMillis, () -> nextPlaceholder.process(player));
                    }
                    return null;
                }

                @Override
                public @Nullable String process() {
                    return null;
                }
            };
        }


    }

}
