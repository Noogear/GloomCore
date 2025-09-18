package gloomcore.paper.adventure;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum PapiTags {
    INSTANCE;

    private final PlaceholderAPIPlugin papiPlugin = PlaceholderAPIPlugin.getInstance();
    private final Map<String, PlaceholderExpansion> expansionCache = new ConcurrentHashMap<>();

    public @NotNull TagResolver papiTag(final @NotNull Player player) {
        return TagResolver.resolver("papi", (argumentQueue, context) -> {
            final String placeholder = argumentQueue.popOr("papi tag requires an argument").value();
            final int index = placeholder.indexOf('_');
            if (index == -1) {
                return Tag.inserting(Component.text(placeholder));
            }
            final PlaceholderExpansion expansion = expansionCache.computeIfAbsent(placeholder.substring(0, index), name ->
                    papiPlugin.getLocalExpansionManager().getExpansion(name)
            );
            if (expansion == null) {
                return Tag.inserting(Component.text(placeholder));
            }
            final String result = expansion.onRequest(player, placeholder.substring(index + 1));
            return Tag.inserting(Component.text(result == null ? placeholder : result));
        });
    }
}