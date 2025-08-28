package cn.gloomcore.paper.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SkullFactory {
    private static final Gson GSON = new Gson();
    private static final UUID NIL_UUID = new UUID(0, 0);
    private static final String TEXTURE_URL_PREFIX = "http://textures.minecraft.net/texture/";
    private static JavaPlugin plugin;
    private static Executor asyncExecutor;
    private static Executor mainThreadExecutor;
    private static Cache<String, CompletableFuture<PlayerProfile>> profileCache;

    private SkullFactory() {
    }

    public static void init(@NotNull JavaPlugin providingPlugin) {
        if (plugin != null) {
            throw new IllegalStateException("SkullFactory has already been initialized.");
        }
        plugin = providingPlugin;
        asyncExecutor = runnable -> plugin.getServer().getAsyncScheduler().runNow(plugin, (final ScheduledTask task) -> runnable.run());
        mainThreadExecutor = runnable -> plugin.getServer().getGlobalRegionScheduler().run(plugin, (final ScheduledTask task) -> runnable.run());
        profileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(3, TimeUnit.HOURS)
                .build();
    }

    public static SkullRequestBuilder fromName(@NotNull String playerName) {
        String cacheKey = "name:" + playerName.toLowerCase();
        return new SkullRequestBuilder(getCachedProfile(cacheKey, () ->
                Bukkit.createProfile(playerName)
                        .update()
                        .thenApplyAsync(p -> checkProfile(p, "player: " + playerName), asyncExecutor))
        );
    }

    public static SkullRequestBuilder fromUuid(@NotNull UUID uuid) {
        String cacheKey = "uuid:" + uuid;
        return new SkullRequestBuilder(getCachedProfile(cacheKey, () ->
                Bukkit.createProfile(uuid)
                        .update()
                        .thenApplyAsync(p -> checkProfile(p, "UUID: " + uuid), asyncExecutor)
        ));
    }

    public static SkullRequestBuilder fromTextureValue(@NotNull String textureValue) {
        String cacheKey = "texture:" + textureValue;
        return new SkullRequestBuilder(getCachedProfile(cacheKey, () ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        byte[] decoded = Base64.getDecoder().decode(textureValue);
                        String jsonString = new String(decoded, StandardCharsets.UTF_8);
                        JsonObject json = GSON.fromJson(jsonString, JsonObject.class);
                        String url = json.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                        return createProfileFromUrl(new URL(url));
                    } catch (Exception e) {
                        throw new ProfileFetchException("Invalid Base64 texture value.", e);
                    }
                }, asyncExecutor)
        ));
    }

    public static SkullRequestBuilder fromUrl(@NotNull String url) {
        String cacheKey = "url:" + url;
        return new SkullRequestBuilder(getCachedProfile(cacheKey, () ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return createProfileFromUrl(new URL(url));
                    } catch (MalformedURLException e) {
                        throw new ProfileFetchException("Invalid texture URL.", e);
                    }
                }, asyncExecutor)
        ));
    }

    public static SkullRequestBuilder fromTextureHash(@NotNull String textureHash) {
        return fromUrl(TEXTURE_URL_PREFIX + textureHash);
    }

    private static PlayerProfile checkProfile(PlayerProfile profile, String identifier) {
        if (profile == null || !profile.hasTextures()) {
            throw new ProfileFetchException("Failed to complete profile for " + identifier);
        }
        return profile;
    }

    private static PlayerProfile createProfileFromUrl(@NotNull URL url) {
        PlayerProfile profile = Bukkit.createProfile(NIL_UUID);
        profile.getTextures().setSkin(url);
        return profile;
    }

    private static CompletableFuture<PlayerProfile> getCachedProfile(@NotNull String key, @NotNull java.util.function.Supplier<CompletableFuture<PlayerProfile>> mappingFunction) {
        if (profileCache == null) {
            throw new IllegalStateException("SkullFactory has not been initialized. Call SkullFactory.init() in your onEnable method.");
        }
        try {
            return profileCache.get(key, mappingFunction::get);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e.getCause() != null ? e.getCause() : e);
        }
    }

    public static final class SkullRequestBuilder {
        private final CompletableFuture<PlayerProfile> profileFuture;
        private Component displayName;

        private SkullRequestBuilder(CompletableFuture<PlayerProfile> profileFuture) {
            this.profileFuture = profileFuture;
        }

        public SkullRequestBuilder displayName(@Nullable Component displayName) {
            this.displayName = displayName;
            return this;
        }

        @NotNull
        public CompletableFuture<SkullMeta> buildMetaAsync() {
            return profileFuture.thenApplyAsync(profile -> {
                SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
                if (meta == null) throw new IllegalStateException("Failed to create SkullMeta.");
                meta.setPlayerProfile(profile);
                if (this.displayName != null) meta.displayName(this.displayName);
                return meta;
            }, asyncExecutor);
        }

        @NotNull
        public CompletableFuture<ItemStack> applyToAsync(@NotNull ItemStack baseItem) {
            return buildMetaAsync().thenApplyAsync(meta -> {
                baseItem.setItemMeta(meta);
                return baseItem;
            }, asyncExecutor);
        }

        @NotNull
        public CompletableFuture<ItemStack> applyTo(@NotNull ItemStack baseItem) {
            return buildMetaAsync().thenApplyAsync(meta -> {
                if (!(baseItem.getItemMeta() instanceof SkullMeta)) return baseItem;
                baseItem.setItemMeta(meta);
                return baseItem;
            }, mainThreadExecutor);
        }

        @NotNull
        public CompletableFuture<ItemStack> build() {
            return applyTo(new ItemStack(Material.PLAYER_HEAD));
        }

        public void buildAndAccept(@NotNull Consumer<ItemStack> action) {
            build().thenAccept(action);
        }
    }
    
    public static final class ProfileFetchException extends RuntimeException {
        public ProfileFetchException(String message) {
            super(message);
        }

        public ProfileFetchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
