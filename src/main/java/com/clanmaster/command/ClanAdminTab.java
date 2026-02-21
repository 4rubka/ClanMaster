package com.clanmaster.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class ClanAdminTab implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("save", "reload", "disband", "info", "setcost");
        if (args.length == 2 && (args[0].equalsIgnoreCase("disband") || args[0].equalsIgnoreCase("info"))) {
            return List.of("<clan>");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("setcost")) {
            return List.of("<amount>");
        }
        return List.of();
    }
}
