package cn.gloomcore.replacer;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;
import java.util.function.Function;

public class PlayerReplacer implements StringReplacer {
    protected final WeakHashMap<String, String> cache = new WeakHashMap<>();
    protected final Function<String, String> applier;
    private final OfflinePlayer player;

    public PlayerReplacer(Player player, Function<String, String> applier) {
        this.player = player;
        this.applier = applier;
    }

    public PlayerReplacer(Player player) {
        this(player, (k) -> PlaceholderUtil.parsePapi(k, player));
    }

    @Override
    public @Nullable String apply(@NotNull String original) {
        return cache.computeIfAbsent(original, applier);
    }

    public void clear() {
        cache.clear();
    }

}
