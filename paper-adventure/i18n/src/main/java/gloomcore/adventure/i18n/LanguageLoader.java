package gloomcore.adventure.i18n;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
     */
    public Map<Locale, Object2ObjectMap<String, String>> load() {
        saveDefaultFiles();

        Map<Locale, Object2ObjectMap<String, String>> allTranslations = new Object2ObjectOpenHashMap<>();
        File folder = plugin.getDataFolder().toPath().resolve(langDirectory).toFile();

        if (!folder.isDirectory()) {
            return allTranslations;
        }

        File[] langFiles = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles == null) return allTranslations;

        for (File langFile : langFiles) {
            String langCode = langFile.getName().replace(".yml", "");
            try {
                allTranslations.put(i18nUtil.normalize(langCode), loadAndMerge(langFile));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load language file: " + langFile.getName(), e);
            }
        }
        return allTranslations;
    }

    /**
     * 高效地保存插件 JAR 包中所有存在的默认语言文件。
     */
    private void saveDefaultFiles() {
        File langFolder = plugin.getDataFolder().toPath().resolve(langDirectory).toFile();
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create language directory: " + langFolder.getPath());
            return;
        }

        for (String langCode : i18nUtil.getAvailableLangCodes()) {
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
    private Object2ObjectMap<String, String> loadAndMerge(File langFile) throws IOException {
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
    private Object2ObjectMap<String, String> flatten(YamlConfiguration config) {
        Object2ObjectOpenHashMap<String, String> map = new Object2ObjectOpenHashMap<>();
        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                map.put(key, config.getString(key));
            }
        }
        return Object2ObjectMaps.unmodifiable(map);
    }
}
