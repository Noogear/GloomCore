package cn.gloomcore.replacer;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class ReplacerUtil {
    private static final Pattern REPLACE_PATTERN = Pattern.compile("%[^%]+?%");
    private static boolean ENABLED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    public static void checkEnabled() {
        ENABLED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static boolean contains(String string) {
        return ENABLED && REPLACE_PATTERN.matcher(string).find();
    }

    public static String apply(String string, Player player) {
        return ENABLED ? PlaceholderAPI.setPlaceholders(player, string) : string;
    }

    public static String apply(String string, OfflinePlayer player) {
        return ENABLED ? PlaceholderAPI.setPlaceholders(player, string) : string;
    }

    public static String apply(String string, ReplacerCache cache) {
        return ENABLED ? cache.get(string) : string;
    }

}
