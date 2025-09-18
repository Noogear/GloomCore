package gloomcore.paper.adventure.impl;

import gloomcore.paper.adventure.MiniTag;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum PapiTag implements MiniTag<Player> {
    INSTANCE;

    private static final PlaceholderAPIPlugin PAPI_PLUGIN = PlaceholderAPIPlugin.getInstance();
    private static final Map<String, PlaceholderExpansion> EXPANSION_CACHE = new ConcurrentHashMap<>();

    @Override
    public @NotNull TagResolver get(final @NotNull Player player) {
        return TagResolver.resolver("papi", (argumentQueue, _) -> {
            final String placeholder = argumentQueue.popOr("papi tag requires an argument").value();
            final int index = placeholder.indexOf('_');
            if (index == -1) {
                return Tag.inserting(Component.text(placeholder));
            }
            final PlaceholderExpansion expansion = EXPANSION_CACHE.computeIfAbsent(placeholder.substring(0, index), name ->
                    PAPI_PLUGIN.getLocalExpansionManager().getExpansion(name)
            );
            if (expansion == null) {
                return Tag.inserting(Component.text(placeholder));
            }
            final String result = expansion.onRequest(player, placeholder.substring(index + 1));
            return Tag.inserting(Component.text(result == null ? placeholder : result));
        });
    }
}