package com.clanmaster.service;

import com.clanmaster.ClanMasterPlugin;
import com.clanmaster.data.ClanStorage;
import com.clanmaster.model.Clan;
import com.clanmaster.model.ClanMember;
import com.clanmaster.model.ClanRank;
import com.clanmaster.util.MessageResolver;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.clanmaster.util.Text;

/**
 * Central service keeping clan state and enforcing rules.
 */
public class ClanService {

    private final ClanMasterPlugin plugin;
    private final ClanStorage storage;
    private final MessageResolver messages;
    private final Economy economy;
    private final Map<String, Clan> clans = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingInvites = new ConcurrentHashMap<>();
    private final Set<UUID> clanChatToggle = ConcurrentHashMap.newKeySet();
    private final Set<UUID> spyToggle = ConcurrentHashMap.newKeySet();

    /**
     * Constructs the service and loads data.
     */
    public ClanService(ClanMasterPlugin plugin, ClanStorage storage, MessageResolver messages, Economy economy) {
        this.plugin = plugin;
        this.storage = storage;
        this.messages = messages;
        this.economy = economy;
        this.clans.putAll(storage.loadAll());
        startAutoSave();
        startDailyReset();
    }

    /**
     * Creates a new clan if name free and leader not already in clan.
     */
    public boolean createClan(String name, UUID leaderId) {
        if (clans.containsKey(name.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (getClanByPlayer(leaderId).isPresent()) {
            return false;
        }
        Clan clan = new Clan(name);
        clan.getMembers().put(leaderId, new ClanMember(leaderId, ClanRank.LEADER));
        clan.getJoinAt().put(leaderId, System.currentTimeMillis());
        clans.put(name.toLowerCase(Locale.ROOT), clan);
        saveAsync();
        return true;
    }

    /**
     * Deletes clan by name.
     */
    public boolean deleteClan(String name) {
        if (!clans.containsKey(name.toLowerCase(Locale.ROOT))) {
            return false;
        }
        clans.remove(name.toLowerCase(Locale.ROOT));
        saveAsync();
        return true;
    }

    /**
     * Adds member to clan if capacity allows.
     */
    public boolean addMember(String clanName, UUID playerId) {
        Clan clan = clans.get(clanName.toLowerCase(Locale.ROOT));
        if (clan == null || clan.getMembers().containsKey(playerId)) {
            return false;
        }
        int maxMembers = plugin.getConfig().getInt("limits.max-members", 20);
        if (clan.getMembers().size() >= maxMembers) {
            return false;
        }
        clan.getMembers().put(playerId, new ClanMember(playerId, ClanRank.MEMBER));
        clan.getJoinAt().put(playerId, System.currentTimeMillis());
        clan.setLastActivity(System.currentTimeMillis());
        pendingInvites.remove(playerId);
        saveAsync();
        return true;
    }

    public boolean invite(String clanName, UUID target) {
        Clan clan = clans.get(clanName.toLowerCase(Locale.ROOT));
        if (clan == null) return false;
        pendingInvites.put(target, clanName.toLowerCase(Locale.ROOT));
        return true;
    }

    public boolean acceptInvite(UUID playerId) {
        if (!pendingInvites.containsKey(playerId)) return false;
        String clan = pendingInvites.get(playerId);
        return addMember(clan, playerId);
    }

    /**
     * Removes member from clan.
     */
    public boolean kickMember(String clanName, UUID playerId) {
        Clan clan = clans.get(clanName.toLowerCase(Locale.ROOT));
        if (clan == null || !clan.getMembers().containsKey(playerId)) {
            return false;
        }
        clan.getMembers().remove(playerId);
        clan.getJoinAt().remove(playerId);
        clan.setLastActivity(System.currentTimeMillis());
        saveAsync();
        return true;
    }

    /**
     * Leaves current clan.
     */
    public boolean leaveClan(UUID playerId) {
        Optional<Clan> optional = getClanByPlayer(playerId);
        if (optional.isEmpty()) {
            return false;
        }
        Clan clan = optional.get();
        ClanMember member = clan.getMembers().get(playerId);
        if (member.getRank() == ClanRank.LEADER && clan.getMembers().size() > 1) {
            return false; // leader must transfer first
        }
        clan.getMembers().remove(playerId);
        clan.getJoinAt().remove(playerId);
        if (clan.getMembers().isEmpty()) {
            deleteClan(clan.getName());
        } else {
            clan.setLastActivity(System.currentTimeMillis());
            saveAsync();
        }
        return true;
    }

    public boolean joinFromInvite(UUID playerId) {
        return acceptInvite(playerId);
    }

    public boolean toggleClanChat(UUID player) {
        if (clanChatToggle.contains(player)) {
            clanChatToggle.remove(player);
            return false;
        }
        clanChatToggle.add(player);
        return true;
    }

    public boolean isClanChat(UUID player) {
        return clanChatToggle.contains(player);
    }

    public boolean toggleSpy(UUID player) {
        if (spyToggle.contains(player)) {
            spyToggle.remove(player);
            return false;
        }
        spyToggle.add(player);
        return true;
    }

    public Set<UUID> getSpyViewers() {
        return spyToggle;
    }

    /**
     * Promotes member if issuer has rights.
     */
    public boolean promote(UUID actorId, UUID targetId) {
        Optional<Clan> optional = getClanByPlayer(actorId);
        if (optional.isEmpty()) {
            return false;
        }
        Clan clan = optional.get();
        ClanMember actor = clan.getMembers().get(actorId);
        ClanMember target = clan.getMembers().get(targetId);
        if (target == null || actor == null) {
            return false;
        }
        if (!actor.getRank().canManage(target.getRank())) {
            return false;
        }
        if (target.getRank() == ClanRank.MEMBER) {
            target.setRank(ClanRank.OFFICER);
        } else if (target.getRank() == ClanRank.OFFICER && actor.getRank() == ClanRank.LEADER) {
            target.setRank(ClanRank.LEADER);
            actor.setRank(ClanRank.OFFICER);
        }
        saveAsync();
        return true;
    }

    /**
     * Demotes member respecting permissions.
     */
    public boolean demote(UUID actorId, UUID targetId) {
        Optional<Clan> optional = getClanByPlayer(actorId);
        if (optional.isEmpty()) {
            return false;
        }
        Clan clan = optional.get();
        ClanMember actor = clan.getMembers().get(actorId);
        ClanMember target = clan.getMembers().get(targetId);
        if (target == null || actor == null || actor == target) {
            return false;
        }
        if (!actor.getRank().canManage(target.getRank())) {
            return false;
        }
        if (target.getRank() == ClanRank.LEADER) {
            return false;
        }
        if (target.getRank() == ClanRank.OFFICER) {
            target.setRank(ClanRank.MEMBER);
        }
        saveAsync();
        return true;
    }

    /**
     * Returns clan containing player.
     */
    public Optional<Clan> getClanByPlayer(UUID playerId) {
        return storage.findByPlayer(playerId, clans);
    }

    /**
     * Returns clan name by player.
     */
    public String getClanName(UUID playerId) {
        return getClanByPlayer(playerId).map(Clan::getName).orElse(null);
    }

    /**
     * Lists all clan names.
     */
    public List<String> listClans() {
        return new ArrayList<>(clans.keySet());
    }

    /**
     * Finds clan by name (case-insensitive).
     */
    public Optional<Clan> getClanByName(String name) {
        return Optional.ofNullable(clans.get(name.toLowerCase(Locale.ROOT)));
    }

    /**
     * Returns top clans sorted by level then xp.
     */
    public Map<Integer, Clan> topByLevel() {
        List<Clan> sorted = clans.values().stream()
                .sorted(Comparator.comparingInt(Clan::getLevel).reversed()
                        .thenComparingDouble(Clan::getXp).reversed())
                .collect(Collectors.toList());
        
        Map<Integer, Clan> result = new LinkedHashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            result.put(i + 1, sorted.get(i));
        }
        return result;
    }

    /**
     * Returns top clans by war points.
     */
    public Map<Integer, Clan> topByWarPoints() {
        List<Clan> sorted = clans.values().stream()
                .sorted(Comparator.comparingInt(Clan::getWarPoints).reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        Map<Integer, Clan> result = new LinkedHashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            result.put(i + 1, sorted.get(i));
        }
        return result;
    }

    public Clan topFirst() {
        return clans.values().stream()
                .sorted(Comparator.comparingInt(Clan::getLevel).reversed()
                        .thenComparingDouble(Clan::getXp).reversed())
                .findFirst().orElse(null);
    }

    public double getXpToNext(Clan clan) {
        double needed = plugin.getConfig().getDouble("progress.xp-per-level", 1000.0) * clan.getLevel();
        return Math.max(0, needed - clan.getXp());
    }

    public double getXpForLevel(int level) {
        return plugin.getConfig().getDouble("progress.xp-per-level", 1000.0) * level;
    }

    public ClanMasterPlugin getPlugin() {
        return plugin;
    }

    public String getLeaderName(Clan clan) {
        UUID id = getLeader(clan);
        return id == null ? "" : Bukkit.getOfflinePlayer(id).getName();
    }

    public UUID getLeader(Clan clan) {
        return clan.getMembers().entrySet().stream()
                .filter(e -> e.getValue().getRank() == ClanRank.LEADER)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public ClanRank getRank(UUID player) {
        Clan clan = getClanOrNull(player);
        if (clan == null) return ClanRank.MEMBER;
        ClanMember m = clan.getMembers().get(player);
        return m == null ? ClanRank.MEMBER : m.getRank();
    }

    public boolean setPrefix(UUID actor, String prefix) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        if (!isLeader(actor, clan)) return false;
        clan.setPrefix(prefix);
        saveAsync();
        return true;
    }

    public boolean togglePvp(UUID actor) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        if (!isLeader(actor, clan)) return false;
        clan.setFriendlyFire(!clan.isFriendlyFire());
        saveAsync();
        return clan.isFriendlyFire();
    }

    public boolean setHome(UUID actor, String loc) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        if (!isLeader(actor, clan)) return false;
        clan.setHome(loc);
        saveAsync();
        return true;
    }

    public boolean delHome(UUID actor) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        if (!isLeader(actor, clan)) return false;
        clan.setHome("");
        saveAsync();
        return true;
    }

    public String getHome(UUID actor) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return null;
        return clan.getHome().isEmpty() ? null : clan.getHome();
    }

    public boolean transfer(UUID actor, UUID target) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        ClanMember actorMember = clan.getMembers().get(actor);
        ClanMember targetMember = clan.getMembers().get(target);
        if (actorMember == null || actorMember.getRank() != ClanRank.LEADER) return false;
        if (targetMember == null) return false;
        actorMember.setRank(ClanRank.OFFICER);
        targetMember.setRank(ClanRank.LEADER);
        saveAsync();
        return true;
    }

    public boolean pointsDeposit(UUID actor, double amount, Economy eco) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || amount <= 0) return false;
        if (eco != null && !eco.has(Bukkit.getOfflinePlayer(actor), amount)) return false;
        if (eco != null) eco.withdrawPlayer(Bukkit.getOfflinePlayer(actor), amount);
        clan.setPoints(clan.getPoints() + amount);
        clan.getPlayerPoints().merge(actor, amount, Double::sum);
        saveAsync();
        return true;
    }

    public boolean pointsWithdraw(UUID actor, double amount, Economy eco) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || amount <= 0 || clan.getPoints() < amount) return false;
        clan.setPoints(clan.getPoints() - amount);
        if (eco != null) eco.depositPlayer(Bukkit.getOfflinePlayer(actor), amount);
        saveAsync();
        return true;
    }

    public boolean allyAdd(UUID actor, String ally) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        clan.getAllies().add(ally.toLowerCase(Locale.ROOT));
        clan.getEnemies().remove(ally.toLowerCase(Locale.ROOT));
        saveAsync();
        return true;
    }

    public boolean allyRemove(UUID actor, String ally) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        clan.getAllies().remove(ally.toLowerCase(Locale.ROOT));
        saveAsync();
        return true;
    }

    public boolean enemyAdd(UUID actor, String target) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        clan.getEnemies().add(target.toLowerCase(Locale.ROOT));
        clan.getAllies().remove(target.toLowerCase(Locale.ROOT));
        saveAsync();
        return true;
    }

    public boolean enemyRemove(UUID actor, String target) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        clan.getEnemies().remove(target.toLowerCase(Locale.ROOT));
        saveAsync();
        return true;
    }

    public boolean rename(UUID actor, String newName) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        if (clans.containsKey(newName.toLowerCase(Locale.ROOT))) return false;
        
        // Remove old name and add with new name
        String oldName = clan.getName().toLowerCase(Locale.ROOT);
        clans.remove(oldName);
        clans.put(newName.toLowerCase(Locale.ROOT), clan);
        
        // Update prefix if it matches old name
        if (clan.getPrefix().equals(clan.getName())) {
            clan.setPrefix(newName);
        }
        
        saveAsync();
        return true;
    }

    public boolean setDescription(UUID actor, String desc) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        clan.setDescription(desc);
        saveAsync();
        return true;
    }

    public boolean setMotd(UUID actor, String motd) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        ClanMember member = clan.getMembers().get(actor);
        if (member == null || (member.getRank() != ClanRank.LEADER && member.getRank() != ClanRank.OFFICER)) return false;
        clan.setMotd(motd);
        saveAsync();
        return true;
    }

    public boolean setTitle(UUID actor, String title) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        clan.setTitle(title);
        saveAsync();
        return true;
    }

    // War system methods
    public boolean declareWar(UUID actor, String enemyClanName) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;

        Clan enemyClan = clans.get(enemyClanName.toLowerCase(Locale.ROOT));
        if (enemyClan == null) return false;
        
        // Cannot declare war on yourself
        if (clan.getName().equalsIgnoreCase(enemyClanName)) return false;

        // Check if already at war
        if (clan.getActiveWars().containsKey(enemyClanName.toLowerCase(Locale.ROOT))) return false;

        // Check max wars
        int maxWars = plugin.getConfig().getInt("limits.max-active-wars", 3);
        if (clan.getActiveWars().size() >= maxWars) return false;

        // Create war for both clans
        Clan.War war = new Clan.War(enemyClanName);
        clan.getActiveWars().put(enemyClanName.toLowerCase(Locale.ROOT), war);

        Clan.War enemyWar = new Clan.War(clan.getName());
        enemyClan.getActiveWars().put(clan.getName().toLowerCase(Locale.ROOT), enemyWar);

        saveAsync();
        return true;
    }

    public boolean endWar(UUID actor, String enemyClanName) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        
        Clan.War war = clan.getActiveWars().remove(enemyClanName.toLowerCase(Locale.ROOT));
        if (war == null) return false;
        
        Clan enemyClan = clans.get(enemyClanName.toLowerCase(Locale.ROOT));
        if (enemyClan != null) {
            enemyClan.getActiveWars().remove(clan.getName().toLowerCase(Locale.ROOT));
        }
        
        // Determine winner
        if (war.getKillsClan1() > war.getKillsClan2()) {
            clan.setWins(clan.getWins() + 1);
            clan.setWarPoints(clan.getWarPoints() + 100);
            enemyClan.setLosses(enemyClan.getLosses() + 1);
        } else if (war.getKillsClan2() > war.getKillsClan1()) {
            enemyClan.setWins(enemyClan.getWins() + 1);
            enemyClan.setWarPoints(enemyClan.getWarPoints() + 100);
            clan.setLosses(clan.getLosses() + 1);
        }
        
        saveAsync();
        return true;
    }

    public boolean makePeace(UUID actor, String enemyClanName) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        
        clan.getEnemies().remove(enemyClanName.toLowerCase(Locale.ROOT));
        clan.getActiveWars().remove(enemyClanName.toLowerCase(Locale.ROOT));
        
        Clan enemyClan = clans.get(enemyClanName.toLowerCase(Locale.ROOT));
        if (enemyClan != null) {
            enemyClan.getEnemies().remove(clan.getName().toLowerCase(Locale.ROOT));
            enemyClan.getActiveWars().remove(clan.getName().toLowerCase(Locale.ROOT));
        }
        
        saveAsync();
        return true;
    }

    public void recordKill(UUID killer, UUID victim) {
        Clan killerClan = getClanOrNull(killer);
        Clan victimClan = getClanOrNull(victim);
        
        if (killerClan != null) {
            killerClan.getKills().merge(killer, 1, Integer::sum);
            killerClan.setLastActivity(System.currentTimeMillis());
            
            // Daily kills tracking
            checkDailyReset(killerClan);
            killerClan.getDailyKills().merge(killer, 1, Integer::sum);
            
            // War kills
            if (victimClan != null && killerClan.getActiveWars().containsKey(victimClan.getName().toLowerCase())) {
                Clan.War war = killerClan.getActiveWars().get(victimClan.getName().toLowerCase());
                if (war != null && war.isActive()) {
                    war.setKillsClan1(war.getKillsClan1() + 1);
                }
            }
            
            // XP reward
            double xpReward = plugin.getConfig().getDouble("progress.xp-per-kill", 10.0);
            addXp(killerClan.getName(), xpReward);
        }
        
        if (victimClan != null) {
            victimClan.getDeaths().merge(victim, 1, Integer::sum);
        }
    }

    public void recordDeath(UUID victim) {
        Clan clan = getClanOrNull(victim);
        if (clan != null) {
            clan.getDeaths().merge(victim, 1, Integer::sum);
        }
    }

    private void checkDailyReset(Clan clan) {
        long now = System.currentTimeMillis();
        long dayMillis = 24 * 60 * 60 * 1000L;
        if (now - clan.getLastDailyReset() > dayMillis) {
            clan.getDailyKills().clear();
            clan.setLastDailyReset(now);
        }
    }

    public boolean lockChest(UUID actor, String locKey) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        return clan.getLockedChests().add(locKey);
    }

    public boolean unlockChest(UUID actor, String locKey) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        return clan.getLockedChests().remove(locKey);
    }

    public boolean canAccessChest(UUID actor, String locKey) {
        Clan clan = getClanOrNull(actor);
        if (clan == null) return false;
        return clan.getLockedChests().contains(locKey);
    }

    private boolean isLeader(UUID actor, Clan clan) {
        ClanMember m = clan.getMembers().get(actor);
        return m != null && m.getRank() == ClanRank.LEADER;
    }

    /**
     * Adds XP and checks level up.
     */
    public void addXp(String clanName, double amount) {
        Clan clan = clans.get(clanName.toLowerCase(Locale.ROOT));
        if (clan == null) {
            return;
        }
        clan.setXp(clan.getXp() + amount);
        double needed = plugin.getConfig().getDouble("progress.xp-per-level", 1000.0) * clan.getLevel();
        if (clan.getXp() >= needed) {
            clan.setLevel(clan.getLevel() + 1);
            clan.setXp(0);
            bonusBroadcast(clan);
        }
        saveAsync();
    }

    /**
     * Schedules periodic autosave.
     */
    private void startAutoSave() {
        long interval = plugin.getConfig().getLong("storage.autosave-seconds", 300L) * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::save, interval, interval);
    }

    /**
     * Schedules daily reset for kill tracking.
     */
    private void startDailyReset() {
        long dayMillis = 24 * 60 * 60 * 20L; // 24 hours in ticks
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::resetDailyKills, dayMillis, dayMillis);
    }

    private void resetDailyKills() {
        long now = System.currentTimeMillis();
        long dayMillis = 24 * 60 * 60 * 1000L;
        for (Clan clan : clans.values()) {
            if (now - clan.getLastDailyReset() > dayMillis) {
                clan.getDailyKills().clear();
                clan.setLastDailyReset(now);
            }
        }
        saveAsync();
    }

    /**
     * Saves data asynchronously.
     */
    public void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save);
    }

    /**
     * Flushes clans to storage.
     */
    public void save() {
        storage.saveAll(new HashMap<>(clans));
    }

    /**
     * Closes service cleanly.
     */
    public void shutdown() {
        save();
    }

    private void bonusBroadcast(Clan clan) {
        String msg = messages.get("clan.level-up")
                .replace("{clan}", clan.getName())
                .replace("{level}", String.valueOf(clan.getLevel()));
        Bukkit.broadcastMessage(Text.color(msg));
        
        // Play sound for clan members
        clan.getMembers().keySet().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        });
    }

    /**
     * Returns immutable view of clan data for GUI.
     */
    public Map<String, Clan> snapshot() {
        return Collections.unmodifiableMap(clans);
    }

    /**
     * Finds clan of player or null.
     */
    public Clan getClanOrNull(UUID playerId) {
        return getClanByPlayer(playerId).orElse(null);
    }

    /**
     * Get clan activity status.
     */
    public String getActivityStatus(Clan clan) {
        long now = System.currentTimeMillis();
        long lastActivity = clan.getLastActivity();
        long diff = now - lastActivity;
        
        long hours = diff / (1000 * 60 * 60);
        long days = hours / 24;
        
        if (hours < 1) return "&aActive now";
        if (hours < 24) return "&e" + hours + "h ago";
        if (days < 7) return "&6" + days + "d ago";
        if (days < 30) return "&c" + days + "d ago";
        return "&8Inactive";
    }

    /**
     * Check if clan has achievement.
     */
    public boolean hasAchievement(Clan clan, String achievement) {
        return clan.getAchievements().contains(achievement);
    }

    /**
     * Add achievement to clan.
     */
    public boolean addAchievement(UUID actor, String achievement) {
        Clan clan = getClanOrNull(actor);
        if (clan == null || !isLeader(actor, clan)) return false;
        if (clan.getAchievements().contains(achievement)) return false;
        clan.getAchievements().add(achievement);
        saveAsync();
        return true;
    }
}
