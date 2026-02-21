package com.clanmaster.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses and exposes clan bonuses based on configuration.
 */
public class BonusService {

    private final Map<Integer, Bonus> bonuses = new HashMap<>();

    /**
     * Loads bonus entries from config.
     */
    public BonusService(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("bonuses");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int level = Integer.parseInt(key);
                ConfigurationSection lvl = section.getConfigurationSection(key);
                double coins = lvl.getDouble("coins", 0);
                double xp = lvl.getDouble("xp", 0);
                String privilege = lvl.getString("privilege", "");
                bonuses.put(level, new Bonus(level, coins, xp, privilege));
            }
        }
    }

    /**
     * Gets bonus for specific level or empty bonus.
     */
    public Bonus getBonus(int level) {
        return bonuses.getOrDefault(level, new Bonus(level, 0, 0, ""));
    }

    /**
     * Simple immutable bonus record.
     */
    public static class Bonus {
        private final int level;
        private final double coins;
        private final double xp;
        private final String privilege;

        public Bonus(int level, double coins, double xp, String privilege) {
            this.level = level;
            this.coins = coins;
            this.xp = xp;
            this.privilege = privilege;
        }

        public int getLevel() {
            return level;
        }

        public double getCoins() {
            return coins;
        }

        public double getXp() {
            return xp;
        }

        public String getPrivilege() {
            return privilege;
        }
    }
}
