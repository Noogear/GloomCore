package gloomcore.paper.adventure.tags.impl;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderTag implements TagResolver {
    private static final LocalExpansionManager EXPANSION_MANAGER = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
    private final gloomcore.contract.Context<Player> playerContext;

    public PlaceholderTag(gloomcore.contract.Context<Player> playerContext) {
        this.playerContext = playerContext;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!has(name)) {
            return null;
        }
        final String placeholder = arguments.popOr("No argument papi key provided").value();
        final int index = placeholder.indexOf('_');
        if (index != -1) {
            final PlaceholderExpansion expansion = EXPANSION_MANAGER.getExpansion(placeholder.substring(0, index));
            if (expansion != null) {
                final String result = expansion.onRequest(playerContext.user(), placeholder.substring(index + 1));
                return Tag.inserting(Component.text(result == null ? placeholder : result));
            }
        }
        return Tag.inserting(Component.text(placeholder));
    }

    @Override
    public boolean has(@NotNull String name) {
        return "papi".equals(name);
    }
}
