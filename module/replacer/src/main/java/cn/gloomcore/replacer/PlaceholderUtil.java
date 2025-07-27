package cn.gloomcore.replacer;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.function.Function;
import java.util.regex.Pattern;

public class PlaceholderUtil {
    private static final Pattern PAPI_PATTERN = Pattern.compile("%[^%]+?%");
    private static boolean PAPI_ENABLED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    public static boolean checkPapiEnabled() {
        PAPI_ENABLED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        return PAPI_ENABLED;
    }

    public static boolean checkPapi(String string) {
        return PAPI_ENABLED && PAPI_PATTERN.matcher(string).find();
    }

    public static String parsePapi(String string, Player player) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    public static String parsePapi(String string, OfflinePlayer player) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    public static String parsePapi(String string, StringReplacer replacer) {
        return replacer.apply(string);
    }

    public static Function<Player, String> parsePapiSupplier(String string) {
        if (checkPapi(string)) {
            return (player) -> PlaceholderAPI.setPlaceholders(player, string);
        }
        return (player) -> string;
    }

}
