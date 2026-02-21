package com.clanmaster.placeholder;

import com.clanmaster.service.ClanService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI integration providing clan placeholders.
 */
public class ClanPlaceholderExpansion extends PlaceholderExpansion {

    private final ClanService clanService;
    private final Object plugin;

    public ClanPlaceholderExpansion(ClanService clanService, Object plugin) {
        this.clanService = clanService;
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clanmaster";
    }

    @Override
    public @NotNull String getAuthor() {
        return "_4rubka_";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";
        var clan = clanService.getClanOrNull(player.getUniqueId());
        switch (params.toLowerCase()) {
            case "name":
                return clan == null ? "" : clan.getName();
            case "level":
                return clan == null ? "0" : String.valueOf(clan.getLevel());
            case "xp":
                return clan == null ? "0" : String.valueOf(clan.getXp());
            case "xp_needed":
                return clan == null ? "0" : String.valueOf(clanService.getXpToNext(clan));
            case "members":
                return clan == null ? "0" : String.valueOf(clan.getMembers().size());
            case "leader":
                return clan == null ? "" : clanService.getLeaderName(clan);
            case "coins":
                return clan == null ? "0" : String.valueOf(clan.getCoins());
            case "points":
                return clan == null ? "0" : String.valueOf(clan.getPoints());
            case "friendlyfire":
                return clan == null ? "false" : String.valueOf(clan.isFriendlyFire());
            case "home_set":
                return clan == null ? "false" : String.valueOf(!clan.getHome().isEmpty());
            case "allies":
                return clan == null ? "0" : String.valueOf(clan.getAllies().size());
            case "enemies":
                return clan == null ? "0" : String.valueOf(clan.getEnemies().size());
            case "rank":
                return clan == null ? "" : clanService.getRank(player.getUniqueId()).name();
            case "prefix":
                return clan == null ? "" : clan.getPrefix();
            case "top_name":
                var top = clanService.topFirst();
                return top == null ? "" : top.getName();
            case "top_level":
                var topLevelClan = clanService.topFirst();
                return topLevelClan == null ? "0" : String.valueOf(topLevelClan.getLevel());
            case "player_points":
                return clan == null ? "0" : String.valueOf(clan.getPlayerPoints().getOrDefault(player.getUniqueId(), 0.0));
            case "ally_list":
                return clan == null ? "" : String.join(", ", clan.getAllies());
            case "enemy_list":
                return clan == null ? "" : String.join(", ", clan.getEnemies());
            case "bonus_privilege":
                var bonus = clanService.getPlugin().getBonusService().getBonus(clan != null ? clan.getLevel() : 0);
                return bonus.getPrivilege();
            case "bonus_coins":
                var bonusCoins = clanService.getPlugin().getBonusService().getBonus(clan != null ? clan.getLevel() : 0);
                return String.valueOf(bonusCoins.getCoins());
            case "bonus_xp":
                var bonusXp = clanService.getPlugin().getBonusService().getBonus(clan != null ? clan.getLevel() : 0);
                return String.valueOf(bonusXp.getXp());
            case "kills":
                return clan == null ? "0" : String.valueOf(clan.getKills().getOrDefault(player.getUniqueId(), 0));
            case "deaths":
                return clan == null ? "0" : String.valueOf(clan.getDeaths().getOrDefault(player.getUniqueId(), 0));
            case "kd":
                int k = clan == null ? 0 : clan.getKills().getOrDefault(player.getUniqueId(), 0);
                int d = clan == null ? 0 : clan.getDeaths().getOrDefault(player.getUniqueId(), 0);
                return d == 0 ? String.valueOf(k) : String.format("%.2f", (double) k / d);
            case "joindays":
                long joinAt = clan == null ? 0L : clan.getJoinAt().getOrDefault(player.getUniqueId(), 0L);
                if (joinAt == 0) return "0";
                long days = (System.currentTimeMillis() - joinAt) / (1000 * 60 * 60 * 24);
                return String.valueOf(days);
            // War placeholders
            case "war_points":
                return clan == null ? "0" : String.valueOf(clan.getWarPoints());
            case "wins":
                return clan == null ? "0" : String.valueOf(clan.getWins());
            case "losses":
                return clan == null ? "0" : String.valueOf(clan.getLosses());
            case "title":
                return clan == null ? "" : clan.getTitle();
            case "motd":
                return clan == null ? "" : clan.getMotd();
            case "active_wars":
                return clan == null ? "0" : String.valueOf(clan.getActiveWars().size());
            case "achievements":
                return clan == null ? "0" : String.valueOf(clan.getAchievements().size());
            case "daily_kills":
                return clan == null ? "0" : String.valueOf(clan.getDailyKills().getOrDefault(player.getUniqueId(), 0));
            case "description":
                return clan == null ? "" : clan.getDescription();
            case "war_kills":
                if (clan == null || clan.getActiveWars().isEmpty()) return "0";
                return clan.getActiveWars().values().stream()
                        .mapToInt(w -> w.isActive() ? w.getKillsClan1() : 0)
                        .sum() + "";
            case "activity":
                return clan == null ? "Unknown" : clanService.getActivityStatus(clan);
            // Tab placeholders
            case "tab_header":
                return clan == null ? "" : clanService.getPlugin().getConfig().getString("tab.header", "&#ff4faf✦ &fClan: &d{clan}").replace("{clan}", clan.getName());
            case "tab_footer":
                return clan == null ? "" : clanService.getPlugin().getConfig().getString("tab.footer", "&#5fd9ff✦ &fPlayer: &b{player}").replace("{player}", player.getName());
            case "online_members":
                if (clan == null) return "0";
                long online = clan.getMembers().keySet().stream()
                        .filter(uuid -> player.getServer().getPlayer(uuid) != null)
                        .count();
                return String.valueOf(online);
            case "max_members":
                return String.valueOf(clanService.getPlugin().getConfig().getInt("limits.max-members", 20));
            case "next_level":
                return clan == null ? "1" : String.valueOf(clan.getLevel() + 1);
            case "xp_progress":
                if (clan == null) return "0";
                double xpNeeded = clanService.getXpToNext(clan);
                double currentXp = clan.getXp() % clanService.getXpForLevel(clan.getLevel());
                return String.valueOf((int)currentXp);
            case "xp_needed_next":
                if (clan == null) return "0";
                return String.valueOf((int)clanService.getXpForLevel(clan.getLevel() + 1));
            case "war_status":
                if (clan == null) return "none";
                if (!clan.getActiveWars().isEmpty()) return "war";
                return "peace";
            case "clan_tag":
                if (clan == null) return "";
                String tagFormat = clanService.getPlugin().getConfig().getString("display.tag-format", "&7[&#ff4faf{clan}&7] ");
                return tagFormat.replace("{clan}", clan.getName());
            default:
                return "";
        }
    }
}
