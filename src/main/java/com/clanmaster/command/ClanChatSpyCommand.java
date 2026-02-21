package com.clanmaster.command;

import com.clanmaster.service.ClanService;
import com.clanmaster.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanChatSpyCommand implements CommandExecutor {
    private final ClanService clanService;

    public ClanChatSpyCommand(ClanService clanService) {
        this.clanService = clanService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only");
            return true;
        }
        boolean enabled = clanService.toggleSpy(player.getUniqueId());
        player.sendMessage(Text.color(enabled ? "&aSpy enabled" : "&cSpy disabled"));
        return true;
    }
}
