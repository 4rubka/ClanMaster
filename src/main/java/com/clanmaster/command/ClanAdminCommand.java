package com.clanmaster.command;

import com.clanmaster.ClanMasterPlugin;
import com.clanmaster.model.Clan;
import com.clanmaster.service.ClanService;
import com.clanmaster.util.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClanAdminCommand implements CommandExecutor {

    private final ClanMasterPlugin plugin;
    private final ClanService clanService;
    private final com.clanmaster.util.MessageResolver messages;

    public ClanAdminCommand(ClanMasterPlugin plugin, ClanService clanService, com.clanmaster.util.MessageResolver messages) {
        this.plugin = plugin;
        this.clanService = clanService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clan.admin")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Text.color("&7/clanadmin save|reload|disband <clan>|info <clan>|setcost|givexp|setwarpoints"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "save":
                clanService.save();
                sender.sendMessage(Text.color("&aSaved."));
                break;
            case "reload":
                plugin.reloadConfig();
                messages.reload();
                sender.sendMessage(Text.color("&aReloaded config/messages."));
                break;
            case "disband":
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&7Usage: /clanadmin disband <clan>"));
                    return true;
                }
                clanService.deleteClan(args[1]);
                sender.sendMessage(Text.color("&cDisbanded " + args[1]));
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&7Usage: /clanadmin info <clan>"));
                    return true;
                }
                clanService.getClanByName(args[1]).ifPresentOrElse(clan -> showInfo(sender, clan), () -> sender.sendMessage(Text.color("&cNot found")));
                break;
            case "setcost":
                if (args.length < 2) {
                    sender.sendMessage(Text.color("&7Usage: /clanadmin setcost <amount>"));
                    return true;
                }
                try {
                    double val = Double.parseDouble(args[1]);
                    plugin.getConfig().set("costs.create", val);
                    plugin.saveConfig();
                    sender.sendMessage(Text.color("&aCreate cost set to " + val));
                } catch (NumberFormatException ex) {
                    sender.sendMessage(Text.color("&cInvalid number"));
                }
                break;
            case "givexp":
                if (args.length < 3) {
                    sender.sendMessage(Text.color("&7Usage: /clanadmin givexp <clan> <amount>"));
                    return true;
                }
                try {
                    double xp = Double.parseDouble(args[2]);
                    clanService.addXp(args[1], xp);
                    sender.sendMessage(Text.color("&aGave " + xp + " XP to " + args[1]));
                } catch (NumberFormatException ex) {
                    sender.sendMessage(Text.color("&cInvalid number"));
                }
                break;
            case "setwarpoints":
                if (args.length < 3) {
                    sender.sendMessage(Text.color("&7Usage: /clanadmin setwarpoints <clan> <amount>"));
                    return true;
                }
                try {
                    int points = Integer.parseInt(args[2]);
                    var clanOpt = clanService.getClanByName(args[1]);
                    if (clanOpt.isPresent()) {
                        clanOpt.get().setWarPoints(points);
                        clanService.saveAsync();
                        sender.sendMessage(Text.color("&aSet war points to " + points + " for " + args[1]));
                    } else {
                        sender.sendMessage(Text.color("&cClan not found"));
                    }
                } catch (NumberFormatException ex) {
                    sender.sendMessage(Text.color("&cInvalid number"));
                }
                break;
            case "addachievement":
                if (args.length < 3) {
                    sender.sendMessage(Text.color("&7Usage: /clanadmin addachievement <clan> <achievement>"));
                    return true;
                }
                var clanOpt = clanService.getClanByName(args[1]);
                if (clanOpt.isPresent()) {
                    clanOpt.get().getAchievements().add(args[2]);
                    clanService.saveAsync();
                    sender.sendMessage(Text.color("&aAdded achievement " + args[2] + " to " + args[1]));
                } else {
                    sender.sendMessage(Text.color("&cClan not found"));
                }
                break;
            case "setlevel":
                if (args.length < 3) {
                    sender.sendMessage(Text.color("&7Usage: /clanadmin setlevel <clan> <level>"));
                    return true;
                }
                try {
                    int level = Integer.parseInt(args[2]);
                    var clanOpt2 = clanService.getClanByName(args[1]);
                    if (clanOpt2.isPresent()) {
                        clanOpt2.get().setLevel(level);
                        clanService.saveAsync();
                        sender.sendMessage(Text.color("&aSet level to " + level + " for " + args[1]));
                    } else {
                        sender.sendMessage(Text.color("&cClan not found"));
                    }
                } catch (NumberFormatException ex) {
                    sender.sendMessage(Text.color("&cInvalid number"));
                }
                break;
            case "list":
                sender.sendMessage(Text.color("&6&l=== All Clans ==="));
                clanService.listClans().forEach(name -> {
                    var clan = clanService.getClanByName(name);
                    clan.ifPresent(c -> sender.sendMessage(Text.color("&f" + c.getName() + " &7- LVL: &a" + c.getLevel() + " &7WP: &c" + c.getWarPoints())));
                });
                break;
            default:
                sender.sendMessage(Text.color("&7/clanadmin save|reload|disband <clan>|info <clan>|setcost|givexp|setwarpoints"));
        }
        return true;
    }

    private void showInfo(CommandSender sender, Clan clan) {
        sender.sendMessage(Text.color("&d&l=== Clan Info ==="));
        sender.sendMessage(Text.color("&dClan: &f" + clan.getName()));
        sender.sendMessage(Text.color("&7Level: &a" + clan.getLevel() + " &7XP: &b" + clan.getXp()));
        sender.sendMessage(Text.color("&7Members: &a" + clan.getMembers().size()));
        sender.sendMessage(Text.color("&7Allies: &a" + clan.getAllies().size() + " &7Enemies: &c" + clan.getEnemies().size()));
        sender.sendMessage(Text.color("&7Points: &e" + clan.getPoints()));
        sender.sendMessage(Text.color("&7War Points: &c" + clan.getWarPoints()));
        sender.sendMessage(Text.color("&7Wins: &a" + clan.getWins() + " &7Losses: &c" + clan.getLosses()));
        sender.sendMessage(Text.color("&7Title: &e" + clan.getTitle()));
        sender.sendMessage(Text.color("&7Achievements: &6" + clan.getAchievements().size()));
    }
}
