package adventure.placeholder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderCacheTag extends PlaceholderTag {
    private final Object2ObjectOpenHashMap<String, Tag> cache = new Object2ObjectOpenHashMap<>();

    public PlaceholderCacheTag(Player player) {
        super(player);
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!has(name)) {
            return null;
        }
        final String placeholder = arguments.popOr("No argument papi key provided").value();
        return cache.computeIfAbsent(placeholder, _ -> createTag(placeholder));
    }

}
