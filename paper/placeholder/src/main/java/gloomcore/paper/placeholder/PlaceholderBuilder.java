package gloomcore.paper.placeholder;

import gloomcore.contract.builder.IBuilder;
import gloomcore.paper.placeholder.decorator.PlayerCacheDecorator;
import gloomcore.paper.placeholder.decorator.SharedFixedCacheDecorator;
import gloomcore.paper.placeholder.decorator.SharedParmCacheDecorator;
import gloomcore.paper.placeholder.internal.FixedPlaceholder;
import gloomcore.paper.placeholder.internal.ParmPlaceholder;
import gloomcore.paper.placeholder.internal.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PlaceholderBuilder implements IBuilder<PlaceholderBuilder, Placeholder> {
    private final PlaceholderManager manager;
    private final String key;
    private String[] path;

    private Placeholder baseAction;
    private CacheStrategy cacheStrategy = CacheStrategy.NONE;
    private Duration cacheDuration = Duration.ZERO;
    private boolean isParm = false;

    public PlaceholderBuilder(PlaceholderManager manager, String key) {
        this.manager = manager;
        this.key = key;
    }

    public PlaceholderBuilder(PlaceholderManager manager, String[] path) {
        this.manager = manager;
        this.key = String.join("_", path);
        this.path = path;
        this.isParm = true;
    }

    public PlaceholderBuilder from(@NotNull String staticText) {
        this.baseAction = FixedPlaceholder.of(staticText);
        this.cacheStrategy = CacheStrategy.NONE;
        this.isParm = false;
        return this;
    }

    public PlaceholderBuilder from(@NotNull Supplier<String> supplier) {
        this.baseAction = FixedPlaceholder.of(supplier);
        this.cacheStrategy = CacheStrategy.SHARED;
        this.isParm = false;
        return this;
    }

    public PlaceholderBuilder from(@NotNull Function<String[], String> parmFunction) {
        this.baseAction = ParmPlaceholder.of(parmFunction);
        this.cacheStrategy = CacheStrategy.SHARED;
        this.isParm = true;
        return this;
    }

    public PlaceholderBuilder fromPlayer(@NotNull Function<Player, String> function) {
        this.baseAction = FixedPlaceholder.of(function);
        this.cacheStrategy = CacheStrategy.PER_PLAYER;
        this.isParm = false;
        return this;
    }

    public PlaceholderBuilder fromPlayer(@NotNull BiFunction<Player, String[], String> parmFunction) {
        this.baseAction = ParmPlaceholder.of(parmFunction);
        this.cacheStrategy = CacheStrategy.PER_PLAYER;
        this.isParm = true;
        return this;
    }

    /**
     * 为占位符配置缓存。
     *
     * @param duration 缓存有效期
     * @return PlaceholderBuilder 自身
     */
    public PlaceholderBuilder withCache(@NotNull Duration duration) {
        this.cacheDuration = duration;
        return this;
    }

    /**
     * (可选) 显式声明使用共享缓存。
     * 仅在 fromPlayer() 后需要手动切换时使用。
     *
     * @return PlaceholderBuilder 自身
     */
    public PlaceholderBuilder shared() {
        this.isParm = !(this.baseAction instanceof FixedPlaceholder);
        this.cacheStrategy = CacheStrategy.SHARED;
        return this;
    }

    /**
     * (可选) 显式声明使用玩家独立缓存。
     * 仅在 from() 后需要手动切换时使用。
     *
     * @return PlaceholderBuilder 自身
     */
    public PlaceholderBuilder perPlayer() {
        this.cacheStrategy = CacheStrategy.PER_PLAYER;
        return this;
    }

    /**
     * 根据所有配置构建最终的 Placeholder 对象。
     * 所有的组装逻辑都在这里，清晰、集中且类型安全。
     *
     * @return 构建完成的 Placeholder。
     */
    @Override
    public Placeholder build() {
        if (baseAction == null) {
            throw new IllegalStateException("Placeholder action must be defined via from() or fromPlayer() before building.");
        }

        if (cacheDuration.isZero() || cacheDuration.isNegative() || cacheStrategy == CacheStrategy.NONE) {
            return baseAction;
        }

        switch (cacheStrategy) {
            case PER_PLAYER:
                return new PlayerCacheDecorator(baseAction, cacheDuration.toMillis(), key, manager);

            case SHARED:
                if (!isParm) {
                    if (baseAction instanceof FixedPlaceholder fixedPlaceholder) {
                        return new SharedFixedCacheDecorator(fixedPlaceholder, cacheDuration.toMillis());
                    } else {
                        throw new IllegalStateException("SHARED_FIXED cache can only be applied to a non-parameterized placeholder.");
                    }
                } else {
                    return new SharedParmCacheDecorator(baseAction, cacheDuration.toMillis());
                }
            default:
                return baseAction;
        }
    }

    public PlaceholderManager register() {
        Placeholder placeholder = this.build();
        if (isParm) {
            this.manager.register(this.path, (ParmPlaceholder) placeholder);
        } else {
            this.manager.register(this.key, (FixedPlaceholder) placeholder);
        }
        return this.manager;
    }

    @Override
    public PlaceholderBuilder configure(Consumer<PlaceholderBuilder> builderConsumer) {
        builderConsumer.accept(this);
        return this;
    }

    @Override
    public PlaceholderBuilder copy() {
        return null;
    }

    private enum CacheStrategy {
        NONE,
        PER_PLAYER,
        SHARED
    }
}