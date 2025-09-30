package gloomcore.adventure.i18n;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageBundle {
    static final Object2ObjectMap<Locale, Locale> EXPLICIT_FALLBACK_MAP = new Object2ObjectOpenHashMap<>();
    static final Object2ObjectMap<String, Locale> PRIMARY_VARIANT_MAP = new Object2ObjectOpenHashMap<>();


    static {
        Map<String, String> map = new HashMap<>();

        // --- 类别 1: 中文变体回滚链 ---
        map.put("zh_hk", "zh_tw");       // 香港繁体 -> 台湾繁体 (写法更接近)
        map.put("zh_tw", "zh_cn");       // 台湾繁体 -> 简体中文 (作为最终中文回滚)
        map.put("lzh", "zh_cn");         // 文言文 -> 简体中文

        // --- 类别 2: 同语言不同区域变体的回滚 ---
        map.put("de_at", "de_de");       // 德语: 奥地利 -> 德国
        map.put("de_ch", "de_de");       // 德语: 瑞士 -> 德国
        map.put("fr_ca", "fr_fr");       // 法语: 加拿大 -> 法国
        map.put("en_au", "en_gb");       // 英语: 澳大利亚 -> 英国
        map.put("en_ca", "en_gb");       // 英语: 加拿大 -> 英国
        map.put("en_nz", "en_gb");       // 英语: 新西兰 -> 英国
        map.put("en_gb", "en_us");       // 英语: 英国 -> 美国 (链式回滚)
        map.put("es_mx", "es_es");       // 西班牙语: 墨西哥 -> 西班牙
        map.put("es_ar", "es_es");       // 西班牙语: 阿根廷 -> 西班牙
        map.put("pt_pt", "pt_br");       // 葡萄牙语: 葡萄牙 -> 巴西 (巴西用户多)
        map.put("be_latn", "be_by");     // 白俄罗斯语: 拉丁字母 -> 西里尔字母

        // --- 类别 3: 高度互通的独立语言 ---
        map.put("nn_no", "no_no");       // 新挪威语 -> 书面挪威语
        map.put("bs_ba", "hr_hr");       // 波斯尼亚语 -> 克罗地亚语
        map.put("hr_hr", "sr_sp");       // 克罗地亚语 -> 塞尔维亚语(拉丁)
        map.put("ms_my", "id_id");       // 马来语 -> 印度尼西亚语
        map.put("id_id", "ms_my");       // 印度尼西亚语 -> 马来语 (可双向，或单向)
        map.put("gl_es", "pt_pt");       // 加利西亚语 -> 葡萄牙语 (语言学上更接近)
        map.put("az_az", "tr_tr");       // 阿塞拜疆语 -> 土耳其语
        map.put("val_es", "ca_es");      // 瓦伦西亚语 -> 加泰罗尼亚语

        // --- 类别 4: 方言/地区语言回滚到国家标准语 ---
        // 日耳曼语族
        map.put("bar", "de_de");         // 巴伐利亚语 -> 德语
        map.put("ksh", "de_de");         // 科隆语 -> 德语
        map.put("nds_de", "de_de");      // 低地德语 -> 德语
        map.put("sxu", "de_de");         // 上萨克森德语 -> 德语
        map.put("brb", "nl_be");         // 布拉班特语 -> 荷兰语(比利时)
        // 罗曼语族
        map.put("ca_es", "es_es");       // 加泰罗尼亚语 -> 西班牙语 (地理位置)
        map.put("ast_es", "es_es");      // 阿斯图里亚斯语 -> 西班牙语
        map.put("vec_it", "it_it");      // 威尼斯语 -> 意大利语
        map.put("fur_it", "it_it");      // 弗留利语 -> 意大利语
        map.put("lmo", "it_it");         // 伦巴底语 -> 意大利语
        map.put("esan", "it_it");        // 艾米利亚语 -> 意大利语
        // 斯拉夫语族
        map.put("szl", "pl_pl");         // 西里西亚语 -> 波兰语
        // 凯尔特语族
        map.put("cy_gb", "en_gb");       // 威尔士语 -> 英语(英国)
        map.put("ga_ie", "en_gb");       // 爱尔兰语 -> 英语(英国)
        map.put("gd_gb", "en_gb");       // 苏格兰盖尔语 -> 英语(英国)
        map.put("kw_gb", "en_gb");       // 康沃尔语 -> 英语(英国)
        map.put("br_fr", "fr_fr");       // 布列塔尼语 -> 法语

        // --- 类别 5: 基于地理/历史影响的回滚 ---
        map.put("uk_ua", "ru_ru");       // 乌克兰语 -> 俄语
        map.put("be_by", "ru_ru");       // 白俄罗斯语 -> 俄语
        map.put("kk_kz", "ru_ru");       // 哈萨克语 -> 俄语
        map.put("ky_kg", "ru_ru");       // 吉尔吉斯语 -> 俄语
        map.put("tt_ru", "ru_ru");       // 鞑靼语 -> 俄语
        map.put("isv", "ru_ru");         // 教会斯拉夫语 -> 俄语
        map.put("nah", "es_mx");         // 纳瓦特尔语 -> 西班牙语(墨西哥)
        map.put("tzo_mx", "es_mx");      // 索西语 -> 西班牙语(墨西哥)

        // --- 类别 6: 人造/趣味语言 ---
        map.put("lol_us", "en_us");      // LOLCAT -> 英语(美国)
        map.put("tlh_aa", "en_us");      // 克林贡语 -> 英语(美国)
        map.put("qya_aa", "en_us");      // 昆雅语 -> 英语(美国)
        map.put("tok", "en_us");         // 道本语 -> 英语(美国)
        map.put("jbo_en", "en_us");      // 逻辑语 -> 英语(美国)
        map.put("eo_uy", "es_uy");       // 世界语(乌拉圭) -> 西班牙语(乌拉圭)

        for (Map.Entry<String, String> entry : map.entrySet()) {
            Locale from = i18nUtil.normalize(entry.getKey());
            Locale to = i18nUtil.normalize(entry.getValue());
            if (from != null && to != null) {
                EXPLICIT_FALLBACK_MAP.put(from, to);
            }
        }
    }

    static {
        Map<String, String> map = new HashMap<>();
        map.put("en", "en_us"); // 英语区首选美国英语
        map.put("zh", "zh_cn"); // 中文区首选简体中文
        map.put("de", "de_de"); // 德语区首选德国德语
        map.put("es", "es_es"); // 西语区首选西班牙西班牙语
        map.put("fr", "fr_fr"); // 法语区首选法国法语
        map.put("pt", "pt_br"); // 葡语区首选巴西葡萄牙语 (覆盖更广)
        map.put("nl", "nl_nl"); // 荷兰语区首选荷兰荷兰语
        map.put("it", "it_it"); // 意大利语
        map.put("ja", "ja_jp"); // 日语
        map.put("ko", "ko_kr"); // 韩语
        map.put("ru", "ru_ru"); // 俄语
        map.put("sv", "sv_se"); // 瑞典语
        map.put("no", "no_no"); // 挪威语
        map.put("pl", "pl_pl"); // 波兰语
        map.put("sr", "sr_sp"); // 塞尔维亚语首选拉丁字母
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Locale to = i18nUtil.normalize(entry.getValue());
            if (to != null) {
                PRIMARY_VARIANT_MAP.put(entry.getKey(), to);
            }
        }
    }

    private final Set<Locale> loadedLanguages;
    private final ConcurrentHashMap<Locale, Locale> resolveCache = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param loadedLanguages 应用当前实际加载的语言集合
     */
    public LanguageBundle(@NotNull Set<Locale> loadedLanguages) {
        if (loadedLanguages.isEmpty()) {
            throw new IllegalArgumentException("Loaded languages cannot be empty.");
        }
        this.loadedLanguages = loadedLanguages;
    }

    public void clearCache() {
        this.loadedLanguages.clear();
    }

    /**
     * 根据预定义的回滚策略，解析并获取最接近的可用语言。
     * 此方法会首先检查缓存，如果命中则直接返回结果，否则执行计算并将结果存入缓存。
     */
    public Locale resolve(@NotNull Locale requestedLanguage) {
        Objects.requireNonNull(requestedLanguage, "Requested language cannot be null");
        final Locale internedLang = i18nUtil.intern(requestedLanguage);
        return resolveCache.computeIfAbsent(internedLang, this::computeResolution);
    }


    /**
     * 根据预定义的回滚策略，解析并获取最接近的可用语言。
     * <p>
     * 回滚策略优先级:
     * 1. 直接匹配: 请求的语言已加载。
     * 2. 精准回滚关系图: 根据 EXPLICIT_FALLBACK_MAP 进行链式查找。
     * 3. 同语族主变体: 回滚到该语言最主流的变体 (例如 de-AT -> de-DE)。
     * 4. 全局默认语言: 回滚到指定的最终默认语言。
     *
     * @param requestedLanguage 用户请求的 Locale
     * @return 一个保证存在于 loadedLanguages 集合中的最匹配的 Locale
     */
    private Locale computeResolution(@NotNull Locale requestedLanguage) {
        // 策略 1: 直接匹配
        if (loadedLanguages.contains(requestedLanguage)) {
            return requestedLanguage;
        }

        // 策略 2: 精准回滚关系图
        Set<Locale> visited = new HashSet<>();
        Locale currentLang = requestedLanguage;

        while (visited.add(currentLang)) {
            Locale fallback = EXPLICIT_FALLBACK_MAP.get(currentLang);
            if (fallback == null) {
                break;
            }
            if (loadedLanguages.contains(fallback)) {
                return fallback;
            }
            currentLang = fallback;
        }

        // 策略 3: 同语族主变体匹配
        String baseLanguage = requestedLanguage.getLanguage();
        Locale primaryVariant = PRIMARY_VARIANT_MAP.get(baseLanguage);

        if (primaryVariant != null && !primaryVariant.equals(requestedLanguage) && loadedLanguages.contains(primaryVariant)) {
            return primaryVariant;
        }

        // 策略 4: 全局默认语言
        return null;
    }

}
