package gloomcore.paper.placeholder.util;

import gloomcore.paper.placeholder.util.decorator.PlayerCacheDecorator;
import gloomcore.paper.placeholder.util.decorator.SharedCacheDecorator;
import gloomcore.paper.placeholder.util.internal.FixedPlaceholder;
import gloomcore.paper.placeholder.util.internal.PlaceholderAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PlaceholderBuilder {

    private final PlaceholderManager manager;
    private final String key;

    private PlaceholderAction action;
    private Duration cacheDuration = Duration.ZERO;
    private boolean isCacheShared = false;

    PlaceholderBuilder(PlaceholderManager manager, String key) {
        this.manager = manager;
        this.key = key;
    }

    public PlaceholderBuilder from(@NotNull String staticText) {
        this.action = PlaceholderAction.of(staticText);
        return this;
    }

    public PlaceholderBuilder from(@NotNull Supplier<String> supplier) {
        this.action = PlaceholderAction.of(supplier);
        return this;
    }

    public PlaceholderBuilder fromPlayer(@NotNull Function<Player, String> function) {
        this.action = PlaceholderAction.of(function);
        return this;
    }

    public PlaceholderBuilder withCache(@NotNull Duration duration) {
        this.cacheDuration = duration;
        return this;
    }

    public PlaceholderBuilder shared() {
        this.isCacheShared = true;
        return this;
    }

    public PlaceholderBuilder perPlayer() {
        this.isCacheShared = false;
        return this;
    }

    public PlaceholderManager register() {
        if (action == null) {
            throw new IllegalStateException("Placeholder action/source must be defined via from() or fromPlayer() for key: '" + key + "'");
        }

        FixedPlaceholder finalFixedPlaceholder = buildPlaceholder();
        manager.register(key, finalFixedPlaceholder);
        return manager;
    }

    private FixedPlaceholder buildPlaceholder() {
        if (cacheDuration == null || cacheDuration.isZero() || cacheDuration.isNegative()) {
            return this.action;
        }

        if (isCacheShared) {
            return new SharedCacheDecorator(this.action, this.cacheDuration.toMillis());
        } else {
            return new PlayerCacheDecorator(this.action, this.cacheDuration.toMillis(), this.key, this.manager.playerCacheHandler);
        }
    }
}
