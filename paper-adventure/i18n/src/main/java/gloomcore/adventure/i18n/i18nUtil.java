package gloomcore.adventure.i18n;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class i18nUtil {
    private static final Interner<Locale> INTERNER = Interners.newWeakInterner();
    private static final Object2ObjectMap<String, Locale> AVAILABLE_LOCALES;

    static {
        Object2ObjectOpenHashMap<String, Locale> tempMap = new Object2ObjectOpenHashMap<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            String langCode = locale.toLanguageTag().replace("-", "_").toLowerCase();
            tempMap.put(langCode, i18nUtil.intern(locale));
        }
        AVAILABLE_LOCALES = tempMap;
    }

    public static Locale normalize(final String langCode) {
        return AVAILABLE_LOCALES.get(langCode);
    }

    public static Set<String> getAvailableLangCodes() {
        return AVAILABLE_LOCALES.keySet();
    }

    public static Collection<Locale> getAvailableLocales() {
        return AVAILABLE_LOCALES.values();
    }

    public static Locale intern(Locale locale) {
        if (locale == null) {
            return null;
        }
        return INTERNER.intern(locale);
    }
}