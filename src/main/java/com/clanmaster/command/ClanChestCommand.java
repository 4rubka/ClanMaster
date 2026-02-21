package com.clanmaster.command;

import com.clanmaster.service.ClanService;
import com.clanmaster.util.LocationUtil;
import com.clanmaster.util.Text;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanChestCommand implements CommandExecutor {
    private final ClanService clanService;

    public ClanChestCommand(ClanService clanService) {
        this.clanService = clanService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Text.color("&7Usage: /" + label + " <lock|unlock|accesslist>"));
            return true;
        }
        Block target = player.getTargetBlockExact(5);
        if (target == null || (target.getType() != Material.CHEST && target.getType() != Material.TRAPPED_CHEST)) {
            player.sendMessage(Text.color("&cLook at a chest."));
            return true;
        }
        String key = LocationUtil.toString(target.getLocation());
        switch (args[0].toLowerCase()) {
            case "lock":
                if (clanService.lockChest(player.getUniqueId(), key)) {
                    player.sendMessage(Text.color("&aChest locked for clan."));
                } else {
                    player.sendMessage(Text.color("&eAlready locked or no clan."));
                }
                break;
            case "unlock":
                if (clanService.unlockChest(player.getUniqueId(), key)) {
                    player.sendMessage(Text.color("&aChest unlocked."));
                } else {
                    player.sendMessage(Text.color("&cNot locked or no permission."));
                }
                break;
            case "accesslist":
                var clan = clanService.getClanOrNull(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage(Text.color("&cNo clan."));
                    return true;
                }
                boolean locked = clan.getLockedChests().contains(key);
                player.sendMessage(Text.color("&7Chest locked: " + locked + " &7Clan members: " + clan.getMembers().size()));
                break;
            default:
                player.sendMessage(Text.color("&7Usage: /" + label + " <lock|unlock|accesslist>"));
        }
        return true;
    }
}
