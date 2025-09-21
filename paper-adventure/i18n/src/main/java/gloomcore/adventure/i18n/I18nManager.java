package gloomcore.adventure.i18n;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Locale;

/**
 * 国际化 (i18n) 管理器。
 * <p>
 * 这是一个纯粹的数据提供者，负责在内存中存储和提供翻译文本。
 * 它不执行任何文件 IO 操作，所有数据都通过 {@link LanguageLoader} 加载。
 */
public enum I18nManager {
    INSTANCE;
    private final Object2ObjectOpenHashMap<Locale, Object2ObjectMap<String, String>> translations = new Object2ObjectOpenHashMap<>();
    private Locale defaultLocale = i18nUtil.intern(Locale.ENGLISH);


    public Locale getDefaultLangCode() {
        return defaultLocale;
    }

    public void setDefaultLangCode(final Locale locale) {
        this.defaultLocale = i18nUtil.intern(locale);
    }

    /**
     * 从指定的加载器重新加载所有语言数据。
     *
     * @param loader 用于加载语言文件的 {@link LanguageLoader} 实例。
     */
    public void reload(final LanguageLoader loader) {
        this.translations.clear();
        this.translations.putAll(loader.load());
        if (!this.translations.containsKey(defaultLocale)) {
            throw new IllegalStateException("Default language file (" + getDefaultLangCode() + ") is missing or failed to load.");
        }
    }


    /**
     *
     * @param key    翻译键。
     * @param locale 目标语言环境。
     * @return 翻译后的字符串。
     */
    public String translate(final String key, final Locale locale) {
        // 1. 尝试从用户的语言中获取翻译
        final Object2ObjectMap<String, String> userTranslations = translations.get(locale);
        if (userTranslations != null) {
            final String message = userTranslations.get(key);
            if (message != null) {
                return message;
            }
        }
        // 2. 如果用户语言中没有，则回退到默认语言
        final Object2ObjectMap<String, String> defaultTranslations = translations.get(this.defaultLocale);
        if (defaultTranslations != null) {
            // 使用 getOrDefault 提供最终的回退值
            return defaultTranslations.getOrDefault(key, key);
        }
        // 3. 极端情况：连默认语言文件都加载失败
        return key;
    }

}
