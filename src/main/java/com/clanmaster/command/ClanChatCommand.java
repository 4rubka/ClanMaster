package com.clanmaster.command;

import com.clanmaster.service.ClanService;
import com.clanmaster.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ClanChatCommand implements CommandExecutor, TabCompleter {
    private final ClanService clanService;

    public ClanChatCommand(ClanService clanService) {
        this.clanService = clanService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Text.color("&7Usage: /" + label + " <message>"));
            return true;
        }
        var clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color("&cYou are not in a clan."));
            return true;
        }
        String msg = String.join(" ", args);
        String formatted = Text.color("&d[Clan] &f" + player.getName() + ": &7" + msg);
        clan.getMembers().keySet().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(formatted);
            }
        });
        // spies
        clanService.getSpyViewers().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(Text.color("&8[Spy] " + formatted));
            }
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
