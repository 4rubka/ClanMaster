package com.clanmaster.api;

import com.clanmaster.service.BonusService;
import com.clanmaster.service.ClanService;
import org.bukkit.OfflinePlayer;

/**
 * Public API facade other plugins can use to interact with ClanMaster without touching internals directly.
 */
public final class ClanAPI {

    private static ClanService clanService;
    private static BonusService bonusService;

    private ClanAPI() {
    }

    /**
     * Internal bootstrap called by the plugin during enable.
     *
     * @param service clan service instance
     * @param bonus   bonus service instance
     */
    public static void initialize(Object plugin, ClanService service, BonusService bonus) {
        clanService = service;
        bonusService = bonus;
    }

    /**
     * Gets the clan name for a player if present.
     */
    public static String getClanName(OfflinePlayer player) {
        return clanService.getClanName(player.getUniqueId());
    }

    /**
     * Creates a clan with the provided leader.
     */
    public static boolean createClan(String name, OfflinePlayer leader) {
        return clanService.createClan(name, leader.getUniqueId());
    }

    /**
     * Adds a player to the clan if allowed.
     */
    public static boolean addPlayer(String clan, OfflinePlayer player) {
        return clanService.addMember(clan, player.getUniqueId());
    }

    /**
     * Removes a player from their clan.
     */
    public static boolean removePlayer(OfflinePlayer player) {
        return clanService.leaveClan(player.getUniqueId());
    }

    /**
     * Retrieves the current bonus snapshot for a clan level.
     */
    public static BonusService.Bonus getBonusForLevel(int level) {
        return bonusService.getBonus(level);
    }
}
