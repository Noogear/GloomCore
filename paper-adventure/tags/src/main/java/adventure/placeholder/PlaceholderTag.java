package adventure.placeholder;

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
    private final Player player;

    public PlaceholderTag(Player player) {
        this.player = player;
    }

    Tag createTag(final String argument) {
        final int index = argument.indexOf('_');
        if (index != -1) {
            final PlaceholderExpansion expansion = EXPANSION_MANAGER.getExpansion(argument.substring(0, index));
            if (expansion != null) {
                final String result = expansion.onRequest(player, argument.substring(index + 1));
                if (result != null) {
                    return Tag.inserting(Component.text(result));
                }
            }
        }
        return Tag.inserting(Component.text(argument));
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!has(name)) {
            return null;
        }
        final String placeholder = arguments.popOr("No argument papi key provided").value();
        return createTag(placeholder);
    }

    @Override
    public boolean has(@NotNull String name) {
        return "papi".equals(name);
    }
}
