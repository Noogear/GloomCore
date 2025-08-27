package cn.gloomcore.paper.placeholder.fixedPlaceholder.decorator;

import cn.gloomcore.paper.placeholder.CacheText;
import cn.gloomcore.paper.placeholder.fixedPlaceholder.PlayerCacheHandler;
import cn.gloomcore.paper.placeholder.fixedPlaceholder.FixedPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface CacheDecorator extends FixedPlaceholder {

    @NotNull
    static CacheDecorator of(final String params, final boolean isShared, final PlayerCacheHandler placeholderHandler, final long intervalMillis, final ActionDecorator action) {
        if (isShared || placeholderHandler == null) {
            return new CacheDecorator() {
                private final CacheText cacheText = new CacheText(action.process(), System.currentTimeMillis());

                @Override
                public @Nullable String process(@Nullable Player player) {
                    return process();
                }

                @Override
                public @Nullable String process() {
                    long interval = System.currentTimeMillis() - cacheText.getLastUpdate();
                    if (interval > intervalMillis || interval < 0) {
                        return cacheText.update(action.process(), System.currentTimeMillis());
                    }
                    return cacheText.getText();
                }
            };
        } else {
            return new CacheDecorator() {
                @Override
                public @Nullable String process(@Nullable Player player) {
                    if (player != null) {
                        return placeholderHandler.getOrUpdate(player.getUniqueId(), params, intervalMillis, () -> action.process(player));
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
