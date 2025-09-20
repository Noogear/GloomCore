package gloomcore.paper.adventure.tags.impl;

import gloomcore.paper.adventure.tags.CacheTag;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.entity.Player;

public class PlaceholderCacheTag extends CacheTag {

    private static final LocalExpansionManager EXPANSION_MANAGER = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
    private final Player player;

    public PlaceholderCacheTag(Player player) {
        super("papi");
        this.player = player;
    }

    @Override
    protected Tag createTag(final String argument, Context ctx) {
        final int index = argument.indexOf('_');
        if (index != -1) {
            final PlaceholderExpansion expansion = EXPANSION_MANAGER.getExpansion(argument.substring(0, index));
            if (expansion != null) {
                final String result = expansion.onRequest(player, argument.substring(index + 1));
                return Tag.inserting(Component.text(result == null ? argument : result));
            }
        }
        return Tag.inserting(Component.text(argument));
    }
}
