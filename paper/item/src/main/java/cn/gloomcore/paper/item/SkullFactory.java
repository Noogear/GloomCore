package cn.gloomcore.paper.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * SkullFactory类用于创建自定义的玩家头颅物品。
 * 该类提供了多种方式来创建带有不同皮肤的头颅物品，包括根据玩家名、UUID、纹理值或URL等。
 * 所有创建操作都是异步进行的，并且结果会被缓存以提高性能。
 */
public final class SkullFactory {
    private static final Gson GSON = new Gson();
    private static final UUID NIL_UUID = new UUID(0, 0);
    private static final String TEXTURE_URL_PREFIX = "http://textures.minecraft.net/texture/";
    private static final Pattern HEX_PATTERN = Pattern.compile("[0-9a-fA-F]{64}");
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.*");
    private static final int BASE64_HEURISTIC_LENGTH = 100;
    private static JavaPlugin plugin;
    private static Executor asyncExecutor;
    private static Executor mainThreadExecutor;
    private static Cache<String, CompletableFuture<PlayerProfile>> profileCache;

    private SkullFactory() {
    }

    /**
     * 初始化SkullFactory
     *
     * @param providingPlugin 提供该功能的插件实例
     * @throws IllegalStateException 如果SkullFactory已经被初始化
     */
    public static void init(@NotNull JavaPlugin providingPlugin) {
        if (plugin != null) {
            throw new IllegalStateException("SkullFactory has already been initialized.");
        }
        plugin = providingPlugin;
        final AsyncScheduler asyncScheduler = plugin.getServer().getAsyncScheduler();
        final GlobalRegionScheduler globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
        asyncExecutor = runnable -> asyncScheduler.runNow(plugin, (final ScheduledTask task) -> runnable.run());
        mainThreadExecutor = runnable -> globalRegionScheduler.run(plugin, (final ScheduledTask task) -> runnable.run());
        profileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(6, TimeUnit.HOURS)
                .build();
    }

    /**
     * 根据输入自动判断类型并创建SkullRequestBuilder
     * 支持的输入类型包括：URL、UUID（标准格式或无分隔符格式）、纹理哈希值、Base64编码的纹理值或玩家名
     *
     * @param input 输入字符串，可以是URL、UUID、纹理哈希、Base64纹理值或玩家名
     * @return SkullRequestBuilder实例，用于进一步构建头颅物品
     * @throws IllegalArgumentException 如果输入为空
     */
    @NotNull
    public static SkullRequestBuilder fromAuto(@NotNull String input) {
        return new SkullRequestBuilder(getCachedProfile(input, () -> {
            if (input.isEmpty()) {
                throw new IllegalArgumentException("Input value cannot be empty.");
            }
            if (URL_PATTERN.matcher(input).matches()) {
                return createProfileFromUrlString(input);
            }
            int length = input.length();
            switch (length) {
                case 36:
                    try {
                        return createProfileFromUuid(UUID.fromString(input));
                    } catch (IllegalArgumentException ignored) {
                    }
                case 32:
                    try {
                        String formattedUuid = new StringBuilder(input)
                                .insert(20, '-')
                                .insert(16, '-')
                                .insert(12, '-')
                                .insert(8, '-')
                                .toString();
                        return createProfileFromUuid(UUID.fromString(formattedUuid));
                    } catch (IllegalArgumentException ignored) {
                    }
                case 64:
                    if (HEX_PATTERN.matcher(input).matches()) {
                        return createProfileFromUrlString(TEXTURE_URL_PREFIX + input);
                    }
                default:
                    if (length > BASE64_HEURISTIC_LENGTH) {
                        return createProfileFromTextureValue(input);
                    }
            }
            return createProfileFromName(input);
        }));
    }

    /**
     * 根据玩家名创建SkullRequestBuilder
     *
     * @param playerName 玩家名
     * @return SkullRequestBuilder实例，用于进一步构建头颅物品
     */
    @NotNull
    public static SkullRequestBuilder fromName(@NotNull String playerName) {
        return new SkullRequestBuilder(getCachedProfile(playerName, () -> createProfileFromName(playerName)));
    }

    /**
     * 根据UUID创建SkullRequestBuilder
     *
     * @param uuid 玩家UUID
     * @return SkullRequestBuilder实例，用于进一步构建头颅物品
     */
    @NotNull
    public static SkullRequestBuilder fromUuid(@NotNull UUID uuid) {
        return new SkullRequestBuilder(getCachedProfile(uuid.toString(), () -> createProfileFromUuid(uuid)));
    }

    /**
     * 根据Base64编码的纹理值创建SkullRequestBuilder
     *
     * @param textureValue Base64编码的纹理值
     * @return SkullRequestBuilder实例，用于进一步构建头颅物品
     */
    @NotNull
    public static SkullRequestBuilder fromTextureValue(@NotNull String textureValue) {
        return new SkullRequestBuilder(getCachedProfile(textureValue, () -> createProfileFromTextureValue(textureValue)));
    }

    /**
     * 根据纹理URL创建SkullRequestBuilder
     *
     * @param url 纹理URL
     * @return SkullRequestBuilder实例，用于进一步构建头颅物品
     */
    @NotNull
    public static SkullRequestBuilder fromUrl(@NotNull String url) {
        return new SkullRequestBuilder(getCachedProfile(url, () -> createProfileFromUrlString(url)));
    }

    /**
     * 根据纹理哈希值创建SkullRequestBuilder
     *
     * @param textureHash 纹理哈希值（64位十六进制字符串）
     * @return SkullRequestBuilder实例，用于进一步构建头颅物品
     */
    @NotNull
    public static SkullRequestBuilder fromTextureHash(@NotNull String textureHash) {
        String textureUrl = TEXTURE_URL_PREFIX + textureHash;
        return new SkullRequestBuilder(getCachedProfile(textureUrl, () -> createProfileFromUrlString(textureUrl)));
    }


    /**
     * 根据玩家名创建PlayerProfile
     *
     * @param name 玩家名
     * @return CompletableFuture<PlayerProfile> 异步获取的PlayerProfile
     */
    private static CompletableFuture<PlayerProfile> createProfileFromName(@NotNull String name) {
        return Bukkit.createProfile(name)
                .update()
                .thenApplyAsync(p -> checkProfile(p, "player: " + name), asyncExecutor);
    }

    /**
     * 根据UUID创建PlayerProfile
     *
     * @param uuid 玩家UUID
     * @return CompletableFuture<PlayerProfile> 异步获取的PlayerProfile
     */
    private static CompletableFuture<PlayerProfile> createProfileFromUuid(@NotNull UUID uuid) {
        return Bukkit.createProfile(uuid)
                .update()
                .thenApplyAsync(p -> checkProfile(p, "UUID: " + uuid), asyncExecutor);
    }

    /**
     * 根据Base64编码的纹理值创建PlayerProfile
     *
     * @param textureValue Base64编码的纹理值
     * @return CompletableFuture<PlayerProfile> 异步创建的PlayerProfile
     */
    private static CompletableFuture<PlayerProfile> createProfileFromTextureValue(@NotNull String textureValue) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] decoded = Base64.getDecoder().decode(textureValue);
                String jsonString = new String(decoded, StandardCharsets.UTF_8);
                JsonObject json = GSON.fromJson(jsonString, JsonObject.class);
                String url = json.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                return createProfileFromUrl(URI.create(url).toURL());
            } catch (Exception e) {
                throw new ProfileFetchException("Invalid Base64 texture value.", e);
            }
        }, asyncExecutor);
    }

    /**
     * 根据URL字符串创建PlayerProfile
     *
     * @param url URL字符串
     * @return CompletableFuture<PlayerProfile> 异步创建的PlayerProfile
     */
    private static CompletableFuture<PlayerProfile> createProfileFromUrlString(@NotNull String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createProfileFromUrl(URI.create(url).toURL());
            } catch (Exception e) {
                throw new ProfileFetchException("Invalid texture URL.", e);
            }
        }, asyncExecutor);
    }

    /**
     * 根据URL创建PlayerProfile并设置皮肤
     *
     * @param url 纹理URL
     * @return PlayerProfile 带有指定皮肤的PlayerProfile
     */
    private static PlayerProfile createProfileFromUrl(@NotNull URL url) {
        PlayerProfile profile = Bukkit.createProfile(NIL_UUID);
        profile.getTextures().setSkin(url);
        return profile;
    }

    /**
     * 检查PlayerProfile是否有效
     *
     * @param profile    PlayerProfile实例
     * @param identifier 标识符，用于错误信息
     * @return PlayerProfile 有效的PlayerProfile
     * @throws ProfileFetchException 如果PlayerProfile无效或没有纹理
     */
    private static PlayerProfile checkProfile(PlayerProfile profile, String identifier) {
        if (profile == null || !profile.hasTextures()) {
            throw new ProfileFetchException("Failed to complete profile for " + identifier);
        }
        return profile;
    }

    /**
     * 从缓存中获取PlayerProfile，如果缓存中不存在则创建新的
     *
     * @param key             缓存键
     * @param mappingFunction 用于创建PlayerProfile的函数
     * @return CompletableFuture<PlayerProfile> 异步获取的PlayerProfile
     * @throws IllegalStateException 如果SkullFactory未初始化
     */
    private static CompletableFuture<PlayerProfile> getCachedProfile(@NotNull String key, @NotNull Supplier<CompletableFuture<PlayerProfile>> mappingFunction) {
        if (profileCache == null) {
            throw new IllegalStateException("SkullFactory has not been initialized. Call SkullFactory.init() in your onEnable method.");
        }
        try {
            return profileCache.get(key, mappingFunction::get);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e.getCause() != null ? e.getCause() : e);
        }
    }


    /**
     * SkullRequestBuilder类用于构建自定义头颅物品
     * 提供了设置显示名称和构建头颅物品的方法
     */
    public static final class SkullRequestBuilder {
        private final CompletableFuture<PlayerProfile> profileFuture;
        private Component displayName;

        private SkullRequestBuilder(CompletableFuture<PlayerProfile> profileFuture) {
            this.profileFuture = profileFuture;
        }

        /**
         * 设置头颅物品的显示名称
         *
         * @param displayName 显示名称
         * @return SkullRequestBuilder 返回自身以支持链式调用
         */
        public SkullRequestBuilder displayName(@Nullable Component displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * 异步构建SkullMeta
         *
         * @return CompletableFuture<SkullMeta> 异步构建的SkullMeta
         */
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

        /**
         * 异步将头颅元数据应用到指定物品上
         *
         * @param baseItem 基础物品
         * @return CompletableFuture<ItemStack> 异步应用后的物品
         */
        @NotNull
        public CompletableFuture<ItemStack> applyToAsync(@NotNull ItemStack baseItem) {
            return buildMetaAsync().thenApplyAsync(meta -> {
                baseItem.setItemMeta(meta);
                return baseItem;
            }, asyncExecutor);
        }

        /**
         * 将头颅元数据应用到指定物品上（在主线程执行）
         *
         * @param baseItem 基础物品
         * @return CompletableFuture<ItemStack> 异步应用后的物品
         */
        @NotNull
        public CompletableFuture<ItemStack> applyTo(@NotNull ItemStack baseItem) {
            return buildMetaAsync().thenApplyAsync(meta -> {
                if (!(baseItem.getItemMeta() instanceof SkullMeta)) return baseItem;
                baseItem.setItemMeta(meta);
                return baseItem;
            }, mainThreadExecutor);
        }

        /**
         * 构建新的头颅物品
         *
         * @return CompletableFuture<ItemStack> 异步构建的头颅物品
         */
        @NotNull
        public CompletableFuture<ItemStack> build() {
            return applyTo(new ItemStack(Material.PLAYER_HEAD));
        }

        /**
         * 构建头颅物品并执行指定操作
         *
         * @param action 构建完成后要执行的操作
         */
        public void buildAndAccept(@NotNull Consumer<ItemStack> action) {
            build().thenAccept(action);
        }
    }

    /**
     * ProfileFetchException类表示获取玩家档案时发生的异常
     */
    public static final class ProfileFetchException extends RuntimeException {
        /**
         * 构造一个新的ProfileFetchException
         *
         * @param message 异常信息
         */
        public ProfileFetchException(String message) {
            super(message);
        }

        /**
         * 构造一个新的ProfileFetchException
         *
         * @param message 异常信息
         * @param cause   异常原因
         */
        public ProfileFetchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
