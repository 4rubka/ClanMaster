package com.clanmaster.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages feature toggles and custom menu text from configuration.
 */
public class FeatureManager {

    private final Map<String, Boolean> enabledFeatures = new HashMap<>();
    private final Map<String, String> menuText = new HashMap<>();
    private final FileConfiguration config;

    public FeatureManager(Plugin plugin) {
        this.config = plugin.getConfig();
        loadFeatures();
        loadMenuText();
    }

    /**
     * Loads enabled features from config.
     */
    private void loadFeatures() {
        ConfigurationSection section = config.getConfigurationSection("enabled-features");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                enabledFeatures.put(key, section.getBoolean(key, true));
            }
        }
    }

    /**
     * Loads custom menu text from config.
     */
    private void loadMenuText() {
        ConfigurationSection section = config.getConfigurationSection("menu-text");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                menuText.put(key, section.getString(key, ""));
            }
        }
    }

    /**
     * Checks if a feature is enabled.
     */
    public boolean isEnabled(String feature) {
        return enabledFeatures.getOrDefault(feature, true);
    }

    /**
     * Gets custom menu text or default value.
     */
    public String getMenuText(String key, String defaultValue) {
        return menuText.getOrDefault(key, defaultValue);
    }

    /**
     * Gets menu text with color codes parsed.
     */
    public String getMenuTextColored(String key, String defaultValue) {
        String value = getMenuText(key, defaultValue);
        return Text.color(value);
    }

    /**
     * Reloads configuration.
     */
    public void reload() {
        enabledFeatures.clear();
        menuText.clear();
        loadFeatures();
        loadMenuText();
    }

    /**
     * Checks if clan home feature is enabled.
     */
    public boolean isClanHomeEnabled() {
        return isEnabled("clan-home");
    }

    /**
     * Checks if clan wars feature is enabled.
     */
    public boolean isClanWarsEnabled() {
        return isEnabled("clan-wars");
    }

    /**
     * Checks if clan bank feature is enabled.
     */
    public boolean isClanBankEnabled() {
        return isEnabled("clan-bank");
    }

    /**
     * Checks if achievements feature is enabled.
     */
    public boolean isAchievementsEnabled() {
        return isEnabled("achievements");
    }

    /**
     * Checks if bonuses feature is enabled.
     */
    public boolean isBonusesEnabled() {
        return isEnabled("bonuses");
    }

    /**
     * Checks if alliances feature is enabled.
     */
    public boolean isAlliancesEnabled() {
        return isEnabled("alliances");
    }

    /**
     * Checks if MOTD feature is enabled.
     */
    public boolean isMotdEnabled() {
        return isEnabled("motd");
    }

    /**
     * Checks if titles feature is enabled.
     */
    public boolean isTitlesEnabled() {
        return isEnabled("titles");
    }

    /**
     * Checks if activity tracking is enabled.
     */
    public boolean isActivityTrackingEnabled() {
        return isEnabled("activity-tracking");
    }

    /**
     * Checks if clan chat is enabled.
     */
    public boolean isClanChatEnabled() {
        return isEnabled("clan-chat");
    }

    /**
     * Checks if friendly fire toggle is enabled.
     */
    public boolean isFriendlyFireEnabled() {
        return isEnabled("friendly-fire");
    }
}
