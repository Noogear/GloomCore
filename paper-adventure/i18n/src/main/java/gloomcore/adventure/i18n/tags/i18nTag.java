package gloomcore.adventure.i18n.tags;

import gloomcore.adventure.i18n.I18nManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class i18nTag implements TagResolver {
    private final Locale locale;

    public i18nTag(Locale locale) {
        this.locale =  I18nManager.INSTANCE.getOrDefaultLocale(locale);
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!this.has(name)) {
            return null;
        }
        String i18nKey = arguments.popOr("No argument i18n key provided").value();
        String translation = I18nManager.INSTANCE.translate(i18nKey, locale);
        if (translation == null) {
            return Tag.inserting(Component.text(i18nKey));
        }
        return Tag.selfClosingInserting(ctx.deserialize(translation));
    }

    @Override
    public boolean has(@NotNull String name) {
        return "i18n".equals(name);
    }
}
