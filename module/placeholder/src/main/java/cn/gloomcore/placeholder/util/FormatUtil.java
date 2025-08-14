package cn.gloomcore.placeholder.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class FormatUtil {
    private static final Pattern PAPI_PATTERN = Pattern.compile("%[^%]+?%");
    @Nullable
    public  static String parseExpansion(@NotNull PlaceholderExpansion expansion, @NotNull String params, OfflinePlayer player){
        return expansion.onRequest(player, params);

    }

    public static String[] splitIdentifierAndParams(String input) {
        int firstUnderscore = input.indexOf('_');
        if (firstUnderscore == -1) {
            return new String[]{input, ""};
        }
        String identifier = input.substring(0,  firstUnderscore);
        String params = input.substring(firstUnderscore  + 1);
        return new String[]{identifier, params};
    }
}
