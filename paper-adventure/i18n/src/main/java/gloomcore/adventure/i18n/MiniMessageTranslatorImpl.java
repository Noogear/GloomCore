package gloomcore.adventure.i18n;

import gloomcore.adventure.i18n.tags.IndexedArgumentTag;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.examination.Examinable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class MiniMessageTranslatorImpl implements Examinable, MinimessageTranslator {
    private final Object2ObjectMap<String, Object2ObjectMap<Locale, String>> translations = new Object2ObjectOpenHashMap<>();
    private final MiniMessage miniMessage;
    private final Key name;
    private Locale defaultLocale = i18nUtil.intern(Locale.US);

    public MiniMessageTranslatorImpl(Key name, MiniMessage miniMessage) {
        this.name = name;
        this.miniMessage = miniMessage;
    }

    @Override
    public @NotNull Key name() {
        return name;
    }

    @Override
    public void register(final @NotNull String key, final @NotNull Locale locale, final @NotNull String format) {
        Object2ObjectMap<Locale, String> localeMap = this.translations.computeIfAbsent(key, _ -> new Object2ObjectOpenHashMap<>());
        localeMap.put(i18nUtil.intern(locale), format);
    }

    @Override
    public void unregister(final @NotNull String key) {
        this.translations.remove(key);
    }

    @Override
    public boolean contains(final @NotNull String key) {
        return this.translations.containsKey(key);
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }

    @Override
    public @Nullable String getMiniMessageString(final @NotNull String key, final @NotNull Locale locale) {
        Object2ObjectMap<Locale, String> translation = translations.get(key);
        if (translation == null) {
            return null;
        }
        if (!translation.containsKey(locale)) {
            return translation.get(defaultLocale);
        } else {
            return translation.get(locale);
        }
    }

    @Override
    public void defaultLocale(@NotNull Locale defaultLocale) {
        this.defaultLocale = i18nUtil.intern(requireNonNull(defaultLocale, "defaultLocale"));
    }


    @Override
    public @Nullable Component translate(final @NotNull TranslatableComponent component, final @NotNull Locale locale) {
        String translated = getMiniMessageString(component.key(), locale);
        if (translated == null) {
            return null;
        }
        if (translated.isEmpty()) {
            return Component.empty();
        }
        final Component resultingComponent;
        if (component.arguments().isEmpty()) {
            resultingComponent = this.miniMessage.deserialize(translated);
        } else {
            resultingComponent = this.miniMessage.deserialize(translated, new IndexedArgumentTag(component.arguments()));
        }
        if (component.children().isEmpty()) {
            return resultingComponent;
        } else {
            return resultingComponent.children(component.children());
        }
    }
}
