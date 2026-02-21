package com.clanmaster.util.integration;

import com.clanmaster.service.ClanService;
import com.clanmaster.util.MessageResolver;
import com.clanmaster.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Lightweight integrations for common plugins without hard dependencies.
 */
public class IntegrationManager implements Listener {

    private final Plugin plugin;
    private final ClanService clanService;
    private final MessageResolver messages;

    public IntegrationManager(Plugin plugin, ClanService clanService, MessageResolver messages) {
        this.plugin = plugin;
        this.clanService = clanService;
        this.messages = messages;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String clan = clanService.getClanName(player.getUniqueId());
        String tagFormat = plugin.getConfig().getString("display.tag-format", "&7[&#ff4faf{clan}&7] ");
        String tag = clan == null ? "" : Text.color(tagFormat.replace("{clan}", clan));
        if (!tag.isEmpty()) {
            player.setPlayerListName(tag + player.getName());
            player.setDisplayName(tag + player.getName());
        }
        // Tab header/footer that coexists with TAB/TabList; they can override if loaded later.
        if (plugin.getConfig().getBoolean("display.tab-header.enabled", true)) {
            String header = Text.color(messages.get("tab.header").replace("{clan}", clan == null ? "No Clan" : clan));
            String footer = Text.color(messages.get("tab.footer").replace("{player}", player.getName()));
            player.setPlayerListHeaderFooter(header, footer);
        }
    }
}
