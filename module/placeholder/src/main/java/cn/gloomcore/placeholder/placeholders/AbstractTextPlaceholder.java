package cn.gloomcore.placeholder.placeholders;

import cn.gloomcore.placeholder.TextPlaceholder;
import cn.gloomcore.placeholder.util.FormatUtil;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public abstract class AbstractTextPlaceholder implements TextPlaceholder {

    protected final String original;
    protected final String identifier;
    protected final String params;
    protected PlaceholderExpansion expansion;

    protected AbstractTextPlaceholder(String original) {
        this.original = original;
        String[] identifierAndParams = FormatUtil.splitIdentifierAndParams(original);
        this.identifier = identifierAndParams[0];
        this.params = identifierAndParams[1];

        this.expansion = getExpansion(identifier);
    }

    protected String parseText(OfflinePlayer player) {
        if (expansion == null) {
            expansion = getExpansion(identifier);
        }
        String request =  expansion.onRequest(player, params);
        if(request == null){
            return original;
        }
        return request;
    }

    private PlaceholderExpansion getExpansion(String identifier) {
        return PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(identifier);
    }


}
