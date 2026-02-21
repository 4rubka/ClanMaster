package com.clanmaster.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Locale;

/**
 * Handles multilingual message lookup.
 */
public class MessageResolver {

    private final Plugin plugin;
    private FileConfiguration messages;
    private String activeLanguage;

    /**
     * Loads messages file with configured language fallback to English.
     */
    public MessageResolver(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reloads messages from disk.
     */
    public void reload() {
        this.activeLanguage = plugin.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();
        File file = new File(langDir, activeLanguage + ".yml");
        if (!file.exists()) {
            plugin.saveResource("lang/" + activeLanguage + ".yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Fetches message by key respecting language namespace.
     */
    public String get(String key) {
        String value = messages.getString(key);
        if (value != null) {
            return value;
        }
        // fallback to en
        File enFile = new File(plugin.getDataFolder(), "lang/en.yml");
        if (enFile.exists()) {
            FileConfiguration enCfg = YamlConfiguration.loadConfiguration(enFile);
            String fallback = enCfg.getString(key);
            if (fallback != null) {
                return fallback;
            }
        }
        // Return key itself if nothing found (for debugging)
        return key;
    }

    /**
     * Fetches message with placeholder replacement.
     */
    public String get(String key, String placeholder, String replacement) {
        return get(key).replace(placeholder, replacement);
    }

    /**
     * Reloads the active language file.
     */
    public void reloadLanguage() {
        reload();
    }
}
