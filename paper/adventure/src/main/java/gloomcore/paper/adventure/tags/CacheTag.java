package gloomcore.paper.adventure.tags;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CacheTag implements TagResolver {

    private final Object2ObjectOpenHashMap<String, Tag> cache = new Object2ObjectOpenHashMap<>();
    private final String tagName;
    private final String errorMessage;

    protected CacheTag(String tagName) {
        this.tagName = tagName;
        this.errorMessage = "No argument " + tagName + " key provided";
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!has(name)) {
            return null;
        }
        final String argument = arguments.popOr(errorMessage).value();
        return cache.computeIfAbsent(argument, _ -> createTag(argument, ctx));
    }

    @Override
    public boolean has(@NotNull String name) {
        return tagName.equals(name);
    }

    protected abstract Tag createTag(final String argument, Context ctx);
}
