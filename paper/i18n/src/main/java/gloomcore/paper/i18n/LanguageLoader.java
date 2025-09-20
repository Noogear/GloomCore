package gloomcore.paper.i18n;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * 语言文件加载器。
 * <p>
 * 该类的唯一职责是从插件的 data 文件夹和 JAR 包中读取语言文件，
 * 合并缺失的键值，并提供一个包含所有翻译的 Map。
 * 它通过一个预定义的语言代码列表进行优化，以实现快速加载。
 */
public final class LanguageLoader {

    private final JavaPlugin plugin;
    private final String langDirectory;

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
            "af_za", "ar_sa", "ast_es", "az_az", "ba_ru", "bar", "be_by", "be_latn",
            "bg_bg", "br_fr", "brb", "bs_ba", "ca_es", "cs_cz", "cy_gb", "da_dk",
            "de_at", "de_ch", "de_de", "el_gr", "en_au", "en_ca", "en_gb", "en_nz",
            "en_pt", "en_ud", "en_us", "enp", "enws", "eo_uy", "es_ar", "es_cl",
            "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "esan", "et_ee", "eu_es",
            "fa_ir", "fi_fi", "fil_ph", "fo_fo", "fr_ca", "fr_fr", "fra_de", "fur_it",
            "fy_nl", "ga_ie", "gd_gb", "gl_es", "haw_us", "he_il", "hi_in", "hn_no",
            "hr_hr", "hu_hu", "hy_am", "id_id", "ig_ng", "io_en", "is_is", "isv",
            "it_it", "ja_jp", "jbo_en", "ka_ge", "kk_kz", "kn_in", "ko_kr", "ksh",
            "kw_gb", "ky_kg", "la_la", "lb_lu", "li_li", "lmo", "lo_la", "lol_us", "lt_lt",
            "lv_lv", "lzh", "mk_mk", "mn_mn", "ms_my", "mt_mt", "nah", "nds_de",
            "nl_be", "nl_nl", "nn_no", "no_no", "oc_fr", "ovd", "pl_pl", "pls",
            "pt_br", "pt_pt", "qya_aa", "ro_ro", "rpr", "ru_ru", "ry_ua", "sah_sah",
            "se_no", "sk_sk", "sl_si", "so_so", "sq_al", "sr_cs", "sr_sp", "sv_se",
            "sxu", "szl", "ta_in", "th_th", "tl_ph", "tlh_aa", "tok", "tr_tr",
            "tt_ru", "tzo_mx", "uk_ua", "val_es", "vec_it", "vi_vn", "vp_vl", "yi_de",
            "yo_ng", "zh_cn", "zh_hk", "zh_tw", "zlm_arab"
    );

    /**
     * @param plugin        所属的插件实例。
     * @param langDirectory 语言文件所在的目录名 (例如 "lang")。
     */
    public LanguageLoader(JavaPlugin plugin, String langDirectory) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin instance cannot be null.");
        this.langDirectory = Objects.requireNonNull(langDirectory, "Language directory cannot be null.");
    }

    /**
     * 加载所有语言文件。
     * <p>
     * 此过程包括：
     * 1. 自动保存插件 JAR 包中包含的默认语言文件。
     * 2. 读取插件数据文件夹中的所有 .yml 语言文件。
     * 3. 使用 JAR 包内的默认文件补充用户文件中缺失的键。
     *
     * @return 一个以标准语言代码为键，翻译映射为值的 Map。
     */
    public Map<String, Map<String, String>> load() {
        saveDefaultFiles();

        Map<String, Map<String, String>> allTranslations = new ConcurrentHashMap<>();
        File folder = plugin.getDataFolder().toPath().resolve(langDirectory).toFile();

        if (!folder.isDirectory()) {
            return allTranslations;
        }

        File[] langFiles = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles == null) return allTranslations;

        for (File langFile : langFiles) {
            String langCode = normalize(langFile.getName().replace(".yml", ""));
            try {
                allTranslations.put(langCode, loadAndMerge(langFile));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load language file: " + langFile.getName(), e);
            }
        }
        return allTranslations;
    }

    /**
     * 将语言代码（如 "en-US", "zh_CN"）标准化为小写的、使用下划线的格式（如 "en_us", "zh_cn"）。
     */
    private static String normalize(String langCode) {
        return langCode.replace('-', '_').toLowerCase();
    }

    /**
     * 高效地保存插件 JAR 包中所有存在的默认语言文件。
     * <p>
     * 通过遍历预定义的 {@link #SUPPORTED_LANGUAGES} 集合来检查资源，
     * 避免了对所有可用 Java Locale 的低效扫描，显著提升了性能。
     */
    private void saveDefaultFiles() {
        File langFolder = plugin.getDataFolder().toPath().resolve(langDirectory).toFile();
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create language directory: " + langFolder.getPath());
            return;
        }

        for (String langCode : SUPPORTED_LANGUAGES) {
            String fileName = langCode + ".yml";
            String resourcePath = langDirectory + "/" + fileName;
            File destinationFile = new File(langFolder, fileName);

            if (plugin.getResource(resourcePath) != null && !destinationFile.exists()) {
                plugin.saveResource(resourcePath, false);
            }
        }
    }

    /**
     * 加载单个语言文件，并使用 JAR 包内的对应文件合并缺失的键。
     */
    private Map<String, String> loadAndMerge(File langFile) throws IOException {
        YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(langFile);
        String resourcePath = langDirectory + "/" + langFile.getName();

        try (InputStream defaultStream = plugin.getResource(resourcePath)) {
            if (defaultStream == null) {
                return flatten(userConfig);
            }

            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            boolean isUpdated = false;

            for (String key : defaultConfig.getKeys(true)) {
                if (!userConfig.contains(key)) {
                    userConfig.set(key, defaultConfig.get(key));
                    isUpdated = true;
                }
            }

            // 警告: Bukkit 的 save() 方法会移除所有注释。
            if (isUpdated) {
                userConfig.save(langFile);
            }

            return flatten(userConfig);
        }
    }

    /**
     * 将 YamlConfiguration 扁平化为 Map<String, String>。
     */
    private Map<String, String> flatten(YamlConfiguration config) {
        Map<String, String> map = new HashMap<>();
        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                map.put(key, config.getString(key));
            }
        }
        return map;
    }
}
