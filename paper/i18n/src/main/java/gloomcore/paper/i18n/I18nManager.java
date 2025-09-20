package gloomcore.paper.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 国际化 (i18n) 管理器。
 * <p>
 * 这是一个纯粹的数据提供者，负责在内存中存储和提供翻译文本。
 * 它不执行任何文件 IO 操作，所有数据都通过 {@link LanguageLoader} 加载。
 */
public final class I18nManager {

    private final String defaultLangCode;
    private final Map<String, Map<String, String>> translations;


    /**
     * @param defaultLangCode 默认语言代码 (例如 "en_us")。它将被自动标准化。
     */
    public I18nManager(final String defaultLangCode) {
        this.defaultLangCode = normalize(defaultLangCode);
        this.translations = new ConcurrentHashMap<>();
    }

    private static String normalize(final Locale locale) {
        return normalize(locale.toLanguageTag());
    }

    private static String normalize(final String langCode) {
        return langCode.replace('-', '_').toLowerCase();
    }

    /**
     *
     * @param key    翻译键。
     * @param locale 目标语言环境。
     * @return 翻译后的字符串。
     */
    public String get(final String key, final Locale locale) {
        final String langCode = normalize(locale);
        // 1. 尝试从用户的语言中获取翻译
        final Map<String, String> userTranslations = translations.get(langCode);
        if (userTranslations != null) {
            final String message = userTranslations.get(key);
            if (message != null) {
                return message;
            }
        }
        // 2. 如果用户语言中没有，则回退到默认语言
        final Map<String, String> defaultTranslations = translations.get(this.defaultLangCode);
        if (defaultTranslations != null) {
            // 使用 getOrDefault 提供最终的回退值
            return defaultTranslations.getOrDefault(key, key);
        }
        // 3. 极端情况：连默认语言文件都加载失败
        return key;
    }
}
