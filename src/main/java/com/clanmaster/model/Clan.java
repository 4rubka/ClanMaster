package com.clanmaster.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a clan with metadata and roster.
 */
public class Clan {

    private final String name;
    private int level;
    private double xp;
    private final Map<UUID, ClanMember> members = new HashMap<>();
    private String description;
    private double coins;
    private String prefix = "";
    private boolean friendlyFire = false;
    private String home = "";
    private final Set<String> allies = new HashSet<>();
    private final Set<String> enemies = new HashSet<>();
    private double points = 0.0;
    private final Map<UUID, Double> playerPoints = new HashMap<>();
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private final Map<UUID, Long> joinAt = new HashMap<>();
    private final Set<String> lockedChests = new HashSet<>();
    
    // New features
    private String motd = "";
    private final Map<String, War> activeWars = new HashMap<>();
    private final Set<String> achievements = new HashSet<>();
    private String title = "";
    private int warPoints = 0;
    private int wins = 0;
    private int losses = 0;
    private long lastActivity = System.currentTimeMillis();
    private final Map<UUID, Integer> dailyKills = new HashMap<>();
    private long lastDailyReset = System.currentTimeMillis();

    /**
     * Creates a new clan with default level and xp.
     *
     * @param name clan name
     */
    public Clan(String name) {
        this.name = name;
        this.level = 1;
        this.xp = 0.0;
        this.description = "";
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getXp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = xp;
    }

    public Map<UUID, ClanMember> getMembers() {
        return members;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCoins() {
        return coins;
    }

    public void setCoins(double coins) {
        this.coins = coins;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public Set<String> getAllies() {
        return allies;
    }

    public Set<String> getEnemies() {
        return enemies;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public Map<UUID, Double> getPlayerPoints() {
        return playerPoints;
    }

    public Map<UUID, Integer> getKills() {
        return kills;
    }

    public Map<UUID, Integer> getDeaths() {
        return deaths;
    }

    public Map<UUID, Long> getJoinAt() {
        return joinAt;
    }

    public Set<String> getLockedChests() {
        return lockedChests;
    }

    // New feature methods
    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public Map<String, War> getActiveWars() {
        return activeWars;
    }

    public Set<String> getAchievements() {
        return achievements;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWarPoints() {
        return warPoints;
    }

    public void setWarPoints(int warPoints) {
        this.warPoints = warPoints;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Map<UUID, Integer> getDailyKills() {
        return dailyKills;
    }

    public long getLastDailyReset() {
        return lastDailyReset;
    }

    public void setLastDailyReset(long lastDailyReset) {
        this.lastDailyReset = lastDailyReset;
    }

    /**
     * Represents an active war between clans.
     */
    public static class War {
        private final String enemyClan;
        private final long startTime;
        private int killsClan1;
        private int killsClan2;
        private boolean active;

        public War(String enemyClan) {
            this.enemyClan = enemyClan;
            this.startTime = System.currentTimeMillis();
            this.killsClan1 = 0;
            this.killsClan2 = 0;
            this.active = true;
        }

        public String getEnemyClan() {
            return enemyClan;
        }

        public long getStartTime() {
            return startTime;
        }

        public int getKillsClan1() {
            return killsClan1;
        }

        public void setKillsClan1(int killsClan1) {
            this.killsClan1 = killsClan1;
        }

        public int getKillsClan2() {
            return killsClan2;
        }

        public void setKillsClan2(int killsClan2) {
            this.killsClan2 = killsClan2;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
