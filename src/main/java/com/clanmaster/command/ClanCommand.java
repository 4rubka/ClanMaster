package com.clanmaster.command;

import com.clanmaster.gui.ClanMenu;
import com.clanmaster.model.Clan;
import com.clanmaster.service.BonusService;
import com.clanmaster.service.ClanService;
import com.clanmaster.util.MessageResolver;
import com.clanmaster.util.Text;
import com.clanmaster.util.FeatureManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles /clan commands and simple subcommands.
 */
public class ClanCommand implements CommandExecutor, TabCompleter {

    private final ClanService clanService;
    private final BonusService bonusService;
    private final MessageResolver messages;
    private final FeatureManager featureManager;
    private final Economy economy;
    private final ClanMenu clanMenu;

    public ClanCommand(Object plugin, ClanService clanService, BonusService bonusService, MessageResolver messages, Economy economy, ClanMenu clanMenu, FeatureManager featureManager) {
        this.clanService = clanService;
        this.bonusService = bonusService;
        this.messages = messages;
        this.featureManager = featureManager;
        this.economy = economy;
        this.clanMenu = clanMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }
        if (args.length == 0) {
            clanMenu.openMain(player);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create":
                handleCreate(player, args);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "promote":
                handlePromote(player, args);
                break;
            case "join":
                handleJoin(player);
                break;
            case "demote":
                handleDemote(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "disband":
                handleDisband(player);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "top":
                handleTop(player);
                break;
            case "list":
                handleList(player);
                break;
            case "prefix":
                handlePrefix(player, args);
                break;
            case "pvp":
                if (featureManager.isFriendlyFireEnabled()) {
                    handlePvp(player);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "sethome":
            case "delhome":
            case "home":
                if (featureManager.isClanHomeEnabled()) {
                    handleHomeCommand(player, args, sub);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "transfer":
                handleTransfer(player, args);
                break;
            case "points":
            case "deposit":
            case "withdraw":
            case "bank":
                if (featureManager.isClanBankEnabled()) {
                    handleBankCommand(player, args, sub);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "ally":
                if (featureManager.isAlliancesEnabled()) {
                    handleAlly(player, args);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "enemy":
            case "war":
            case "peace":
            case "wars":
            case "wartop":
                if (featureManager.isClanWarsEnabled()) {
                    handleWarCommand(player, args, sub);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "rename":
                handleRename(player, args);
                break;
            case "desc":
            case "description":
                handleDescription(player, args);
                break;
            case "menu":
                clanMenu.openMain(player);
                break;
            case "help":
                handleHelp(player);
                break;
            // New commands
            case "motd":
                if (featureManager.isMotdEnabled()) {
                    handleMotd(player, args);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "activity":
                if (featureManager.isActivityTrackingEnabled()) {
                    handleActivity(player);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "title":
                if (featureManager.isTitlesEnabled()) {
                    handleTitle(player, args);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "achievement":
            case "achievements":
                if (featureManager.isAchievementsEnabled()) {
                    handleAchievements(player, args);
                } else {
                    player.sendMessage(Text.color("&cThis feature is disabled."));
                }
                break;
            case "stats":
                handleClanStats(player);
                break;
            case "member":
                handleMember(player, args);
                break;
            default:
                player.sendMessage(Text.color(messages.get("error.unknown")));
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color(messages.get("usage.create")));
            return;
        }
        String name = args[1];
        double price = player.getServer().getPluginManager().getPlugin("ClanMaster").getConfig().getDouble("costs.create", 0);
        if (economy != null && !player.hasPermission("clan.create.free")) {
            if (!economy.has(player, price)) {
                player.sendMessage(Text.color(messages.get("error.no-money")));
                return;
            }
            economy.withdrawPlayer(player, price);
        }
        boolean ok = clanService.createClan(name, player.getUniqueId());
        if (ok) {
            player.sendMessage(Text.color(messages.get("clan.created").replace("{clan}", name)));
        } else {
            player.sendMessage(Text.color(messages.get("error.create")));
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color(messages.get("usage.invite")));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        boolean ok = clanService.invite(clan.getName(), target.getUniqueId());
        if (ok) {
            player.sendMessage(Text.color(messages.get("clan.invited").replace("{player}", target.getName())));
            if (target.isOnline()) {
                ((Player) target).sendMessage(Text.color(messages.get("clan.invite-received").replace("{clan}", clan.getName())));
            }
        } else {
            player.sendMessage(Text.color(messages.get("error.invite")));
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color(messages.get("usage.kick")));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        boolean ok = clanService.kickMember(clan.getName(), target.getUniqueId());
        if (ok) {
            player.sendMessage(Text.color(messages.get("clan.kicked").replace("{player}", target.getName())));
        } else {
            player.sendMessage(Text.color(messages.get("error.kick")));
        }
    }

    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color(messages.get("usage.promote")));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        boolean ok = clanService.promote(player.getUniqueId(), target.getUniqueId());
        if (ok) {
            player.sendMessage(Text.color(messages.get("clan.promoted").replace("{player}", target.getName())));
        } else {
            player.sendMessage(Text.color(messages.get("error.promote")));
        }
    }

    private void handleDemote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color(messages.get("usage.demote")));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        boolean ok = clanService.demote(player.getUniqueId(), target.getUniqueId());
        if (ok) {
            player.sendMessage(Text.color(messages.get("clan.demoted").replace("{player}", target.getName())));
        } else {
            player.sendMessage(Text.color(messages.get("error.demote")));
        }
    }

    private void handleJoin(Player player) {
        boolean ok = clanService.joinFromInvite(player.getUniqueId());
        if (ok) {
            player.sendMessage(Text.color(messages.get("clan.joined")));
        } else {
            player.sendMessage(Text.color(messages.get("error.join")));
        }
    }

    private void handleLeave(Player player) {
        boolean ok = clanService.leaveClan(player.getUniqueId());
        if (ok) {
            player.sendMessage(Text.color(messages.get("clan.left")));
        } else {
            player.sendMessage(Text.color(messages.get("error.leave")));
        }
    }

    private void handleDisband(Player player) {
        var clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        var member = clan.getMembers().get(player.getUniqueId());
        if (member == null || member.getRank() != com.clanmaster.model.ClanRank.LEADER) {
            player.sendMessage(Text.color(messages.get("error.not-leader")));
            return;
        }
        clanService.deleteClan(clan.getName());
        player.sendMessage(Text.color(messages.get("clan.disbanded").replace("{clan}", clan.getName())));
    }

    private void handleInfo(Player player, String[] args) {
        String name;
        if (args.length >= 2) {
            name = args[1].toLowerCase(java.util.Locale.ROOT);
        } else {
            var c = clanService.getClanOrNull(player.getUniqueId());
            if (c == null) {
                player.sendMessage(Text.color(messages.get("error.no-clan")));
                return;
            }
            name = c.getName();
        }
        var opt = clanService.getClanByName(name);
        if (opt.isEmpty()) {
            player.sendMessage(Text.color(messages.get("error.no-clan-name").replace("{clan}", name)));
            return;
        }
        var clan = opt.get();
        player.sendMessage(Text.color("&8&l╔══ &#5fd9ff" + clan.getName()));
        player.sendMessage(Text.color("&8&l║ &7Level: &a" + clan.getLevel() + " &7XP: &b" + clan.getXp()));
        player.sendMessage(Text.color("&8&l║ &7Members: &a" + clan.getMembers().size()));
        player.sendMessage(Text.color("&8&l║ &7War Points: &c" + clan.getWarPoints()));
        player.sendMessage(Text.color("&8&l║ &7Wins: &a" + clan.getWins() + " &7Losses: &c" + clan.getLosses()));
        player.sendMessage(Text.color("&8&l╚══"));
    }

    private void handleTop(Player player) {
        player.sendMessage(Text.color("&6&l=== Top Clans by Level ==="));
        clanService.topByLevel().forEach((rank, entry) -> {
            player.sendMessage(Text.color("&e#" + rank + " &d" + entry.getName() + " &7LVL: &a" + entry.getLevel()));
        });
    }

    private void handleWarTop(Player player) {
        player.sendMessage(Text.color("&c&l=== Top Clans by War Points ==="));
        clanService.topByWarPoints().forEach((rank, entry) -> {
            player.sendMessage(Text.color("&e#" + rank + " &d" + entry.getName() + " &7WP: &c" + entry.getWarPoints()));
        });
    }

    private void handlePrefix(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan prefix <text>"));
            return;
        }
        String prefix = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        boolean ok = clanService.setPrefix(player.getUniqueId(), prefix);
        player.sendMessage(Text.color(ok ? messages.get("clan.prefix-set").replace("{prefix}", prefix) : messages.get("error.not-leader")));
    }

    private void handlePvp(Player player) {
        boolean state = clanService.togglePvp(player.getUniqueId());
        player.sendMessage(Text.color(state ? messages.get("clan.pvp-on") : messages.get("clan.pvp-off")));
    }

    private void handleSetHome(Player player) {
        boolean ok = clanService.setHome(player.getUniqueId(), com.clanmaster.util.LocationUtil.toString(player.getLocation()));
        player.sendMessage(Text.color(ok ? messages.get("clan.home-set") : messages.get("error.not-leader")));
    }

    private void handleDelHome(Player player) {
        boolean ok = clanService.delHome(player.getUniqueId());
        player.sendMessage(Text.color(ok ? messages.get("clan.home-set") : messages.get("error.not-leader")));
    }

    private void handleHome(Player player) {
        String locString = clanService.getHome(player.getUniqueId());
        if (locString == null) {
            player.sendMessage(Text.color(messages.get("clan.home-missing")));
            return;
        }
        var loc = com.clanmaster.util.LocationUtil.fromString(locString);
        if (loc == null) {
            player.sendMessage(Text.color(messages.get("clan.home-missing")));
            return;
        }
        player.teleport(loc);
        player.sendMessage(Text.color(messages.get("clan.home-teleport")));
    }

    private void handleHomeCommand(Player player, String[] args, String sub) {
        switch (sub) {
            case "sethome":
                handleSetHome(player);
                break;
            case "delhome":
                handleDelHome(player);
                break;
            case "home":
                handleHome(player);
                break;
        }
    }

    private void handleBankCommand(Player player, String[] args, String sub) {
        switch (sub) {
            case "bank":
                handleBank(player);
                break;
            case "deposit":
                handleDeposit(player, args);
                break;
            case "withdraw":
                handleWithdraw(player, args);
                break;
            case "points":
                handlePoints(player, args);
                break;
        }
    }

    private void handleWarCommand(Player player, String[] args, String sub) {
        switch (sub) {
            case "war":
                handleWar(player, args);
                break;
            case "peace":
                handlePeace(player, args);
                break;
            case "wars":
                handleWars(player);
                break;
            case "wartop":
                handleWarTop(player);
                break;
            case "enemy":
                handleEnemy(player, args);
                break;
        }
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan transfer <player>"));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        boolean ok = clanService.transfer(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Text.color(ok ? messages.get("clan.transfer").replace("{player}", target.getName()) : messages.get("error.not-leader")));
    }

    private void handlePoints(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.color("&7Usage: /clan points <deposit|withdraw> <amount>"));
            return;
        }
        double amount;
        try { amount = Double.parseDouble(args[2]); } catch (NumberFormatException ex) { player.sendMessage("§cAmount?"); return; }
        boolean ok;
        if (args[1].equalsIgnoreCase("deposit")) {
            ok = clanService.pointsDeposit(player.getUniqueId(), amount, economy);
            player.sendMessage(Text.color(ok ? messages.get("clan.points-deposit").replace("{amount}", String.valueOf(amount)) : messages.get("clan.points-fail")));
        } else if (args[1].equalsIgnoreCase("withdraw")) {
            ok = clanService.pointsWithdraw(player.getUniqueId(), amount, economy);
            player.sendMessage(Text.color(ok ? messages.get("clan.points-withdraw").replace("{amount}", String.valueOf(amount)) : messages.get("clan.points-fail")));
        }
    }

    private void handleAlly(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.color("&7Usage: /clan ally <add|remove> <clan>"));
            return;
        }
        boolean ok = args[1].equalsIgnoreCase("add")
                ? clanService.allyAdd(player.getUniqueId(), args[2])
                : clanService.allyRemove(player.getUniqueId(), args[2]);
        player.sendMessage(Text.color(ok ? (args[1].equalsIgnoreCase("add") ? messages.get("clan.ally-add") : messages.get("clan.ally-remove")).replace("{clan}", args[2]) : messages.get("error.not-leader")));
    }

    private void handleEnemy(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.color("&7Usage: /clan enemy <add|remove> <clan>"));
            return;
        }
        boolean ok = args[1].equalsIgnoreCase("add")
                ? clanService.enemyAdd(player.getUniqueId(), args[2])
                : clanService.enemyRemove(player.getUniqueId(), args[2]);
        player.sendMessage(Text.color(ok ? (args[1].equalsIgnoreCase("add") ? messages.get("clan.enemy-add") : messages.get("clan.enemy-remove")).replace("{clan}", args[2]) : messages.get("error.not-leader")));
    }

    private void handleRename(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan rename <newName>"));
            return;
        }
        boolean ok = clanService.rename(player.getUniqueId(), args[1]);
        player.sendMessage(Text.color(ok ? "&aClan renamed to " + args[1] : messages.get("error.not-leader")));
    }

    private void handleDescription(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan desc <text>"));
            return;
        }
        String desc = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        boolean ok = clanService.setDescription(player.getUniqueId(), desc);
        player.sendMessage(Text.color(ok ? "&aDescription updated." : messages.get("error.not-leader")));
    }

    private void handleList(Player player) {
        String list = clanService.listClans().stream().sorted().collect(Collectors.joining(", "));
        player.sendMessage(Text.color(messages.get("clan.list").replace("{clans}", list)));
    }

    private void handleHelp(Player player) {
        String[] lines = {
                "&8&l╔══ &x&f&f&9&b&f&fClanMaster &7/&f help",
                "&8&l║ &a/clan create <name> &7- &fCreate a clan",
                "&8&l║ &a/clan invite <player> &7- &fInvite a player",
                "&8&l║ &a/clan kick <player> &7- &fKick a player",
                "&8&l║ &a/clan promote <player> &7- &fPromote a player",
                "&8&l║ &a/clan demote <player> &7- &fDemote a player",
                "&8&l║ &a/clan join &7- &fJoin a clan",
                "&8&l║ &a/clan leave &7- &fLeave a clan",
                "&8&l║ &a/clan disband &7- &fDisband clan (leader only)",
                "&8&l║ &a/clan list &7- &fList all clans",
                "&8&l║ &a/clan top &7- &fTop clans by level",
                "&8&l║ &a/clan info <clan> &7- &fView clan info",
                "&8&l║ &a/clan menu &7- &fOpen clan menu",
                "&8&l║ &a/clan war <clan> &7- &fDeclare war",
                "&8&l║ &a/clan peace <clan> &7- &fEnd war",
                "&8&l║ &a/clan wars &7- &fView active wars",
                "&8&l║ &a/clan wartop &7- &fTop clans by war points",
                "&8&l║ &a/clan ally <add|remove> <clan> &7- &fManage allies",
                "&8&l║ &a/clan enemy <add|remove> <clan> &7- &fManage enemies",
                "&8&l║ &a/clan prefix <text> &7- &fSet clan prefix",
                "&8&l║ &a/clan pvp &7- &fToggle friendly fire",
                "&8&l║ &a/clan sethome &7- &fSet clan home",
                "&8&l║ &a/clan home &7- &fTeleport to clan home",
                "&8&l║ &a/clan transfer <player> &7- &fTransfer leadership",
                "&8&l║ &a/clan rename <name> &7- &fRename clan",
                "&8&l║ &a/clan desc <text> &7- &fSet clan description",
                "&8&l║ &a/clan motd <text> &7- &fSet clan MOTD",
                "&8&l║ &a/clan activity &7- &fView clan activity",
                "&8&l║ &a/clan title <name> &7- &fSet clan title",
                "&8&l║ &a/clan achievements &7- &fView clan achievements",
                "&8&l║ &a/clan stats &7- &fView clan statistics",
                "&8&l║ &a/clan member &7- &fList clan members",
                "&8&l║ &a/clan bank &7- &fView clan bank",
                "&8&l║ &a/clan deposit <amount> &7- &fDeposit points",
                "&8&l║ &a/clan withdraw <amount> &7- &fWithdraw points",
                "&8&l╚══ &7PlaceholderAPI: %clanmaster_name%, %clanmaster_level%"
        };
        for (String line : lines) {
            player.sendMessage(Text.color(line));
        }
    }

    // New command handlers
    private void handleMotd(Player player, String[] args) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        if (args.length < 2) {
            String motd = clan.getMotd();
            player.sendMessage(Text.color("&7Clan MOTD: &e" + (motd.isEmpty() ? "Not set" : motd)));
            return;
        }
        String motd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        boolean ok = clanService.setMotd(player.getUniqueId(), motd);
        player.sendMessage(Text.color(ok ? "&aClan MOTD set to: &e" + motd : messages.get("error.not-leader")));
    }

    private void handleActivity(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        String status = clanService.getActivityStatus(clan);
        player.sendMessage(Text.color("&6&l=== Clan Activity ==="));
        player.sendMessage(Text.color("&7Last Activity: " + status));
        player.sendMessage(Text.color("&7Wins: &a" + clan.getWins() + " &7Losses: &c" + clan.getLosses()));
        player.sendMessage(Text.color("&7War Points: &c" + clan.getWarPoints()));
    }

    private void handleWar(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan war <clan>"));
            return;
        }
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        
        String enemyClanName = args[1];
        
        // Check if declaring war on self
        if (clan.getName().equalsIgnoreCase(enemyClanName)) {
            player.sendMessage(Text.color(messages.get("error.cannot-declare-war-self")));
            return;
        }
        
        // Check if already at war
        if (clan.getActiveWars().containsKey(enemyClanName.toLowerCase(Locale.ROOT))) {
            player.sendMessage(Text.color(messages.get("error.already-at-war")));
            return;
        }
        
        boolean ok = clanService.declareWar(player.getUniqueId(), enemyClanName);
        if (ok) {
            player.sendMessage(Text.color(messages.get("clan.war-declared").replace("{clan}", enemyClanName)));
        } else {
            player.sendMessage(Text.color(messages.get("error.not-leader")));
        }
    }

    private void handlePeace(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan peace <clan>"));
            return;
        }
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        boolean ok = clanService.makePeace(player.getUniqueId(), args[1]);
        player.sendMessage(Text.color(ok ? "&a☮ Peace made with &2" + args[1] + "&a!" : messages.get("error.not-leader")));
    }

    private void handleWars(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        player.sendMessage(Text.color("&c&l=== Active Wars ==="));
        if (clan.getActiveWars().isEmpty()) {
            player.sendMessage(Text.color("&7No active wars."));
            return;
        }
        clan.getActiveWars().forEach((enemy, war) -> {
            player.sendMessage(Text.color("&evs &c" + war.getEnemyClan() + " &7- &f" + war.getKillsClan1() + ":" + war.getKillsClan2()));
        });
    }

    private void handleTitle(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan title <name>"));
            return;
        }
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        String title = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        boolean ok = clanService.setTitle(player.getUniqueId(), title);
        player.sendMessage(Text.color(ok ? "&aClan title set to: &e" + title : messages.get("error.not-leader")));
    }

    private void handleAchievements(Player player, String[] args) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        player.sendMessage(Text.color("&6&l=== Clan Achievements ==="));
        if (clan.getAchievements().isEmpty()) {
            player.sendMessage(Text.color("&7No achievements yet."));
            return;
        }
        clan.getAchievements().forEach(achievement -> {
            player.sendMessage(Text.color("&e✦ " + achievement));
        });
    }

    private void handleClanStats(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        player.sendMessage(Text.color("&6&l=== Clan Statistics ==="));
        player.sendMessage(Text.color("&7Name: &f" + clan.getName()));
        player.sendMessage(Text.color("&7Level: &a" + clan.getLevel() + " &7XP: &b" + clan.getXp()));
        player.sendMessage(Text.color("&7Members: &a" + clan.getMembers().size()));
        player.sendMessage(Text.color("&7Coins: &6" + clan.getCoins()));
        player.sendMessage(Text.color("&7Points: &e" + clan.getPoints()));
        player.sendMessage(Text.color("&7War Points: &c" + clan.getWarPoints()));
        player.sendMessage(Text.color("&7Wins: &a" + clan.getWins() + " &7Losses: &c" + clan.getLosses()));
        player.sendMessage(Text.color("&7Allies: &e" + clan.getAllies().size() + " &7Enemies: &c" + clan.getEnemies().size()));
    }

    private void handleMember(Player player, String[] args) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        player.sendMessage(Text.color("&6&l=== Clan Members ==="));
        clan.getMembers().forEach((uuid, member) -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            String status = p.isOnline() ? "&a[ONLINE]" : "&c[OFFLINE]";
            player.sendMessage(Text.color("&f" + p.getName() + " &7- &e" + member.getRank().name() + " " + status));
        });
    }

    private void handleBank(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(Text.color(messages.get("error.no-clan")));
            return;
        }
        player.sendMessage(Text.color("&6&l=== Clan Bank ==="));
        player.sendMessage(Text.color("&7Points: &e" + clan.getPoints()));
        player.sendMessage(Text.color("&7Your contribution: &e" + clan.getPlayerPoints().getOrDefault(player.getUniqueId(), 0.0)));
    }

    private void handleDeposit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan deposit <amount>"));
            return;
        }
        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (NumberFormatException ex) { player.sendMessage(Text.color("&cInvalid amount")); return; }
        boolean ok = clanService.pointsDeposit(player.getUniqueId(), amount, economy);
        player.sendMessage(Text.color(ok ? "&aDeposited &e" + amount + " &apoints." : messages.get("clan.points-fail")));
    }

    private void handleWithdraw(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.color("&7Usage: /clan withdraw <amount>"));
            return;
        }
        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (NumberFormatException ex) { player.sendMessage(Text.color("&cInvalid amount")); return; }
        boolean ok = clanService.pointsWithdraw(player.getUniqueId(), amount, economy);
        player.sendMessage(Text.color(ok ? "&aWithdrew &e" + amount + " &apoints." : messages.get("clan.points-fail")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "join", "kick", "promote", "demote", "leave", "disband", 
                    "info", "top", "wartop", "list", "prefix", "pvp", "sethome", "delhome", "home", 
                    "transfer", "points", "ally", "enemy", "rename", "desc", "menu", "help",
                    "motd", "activity", "war", "peace", "wars", "title", "achievements", "stats", 
                    "member", "bank", "deposit", "withdraw");
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "invite":
                case "kick":
                case "promote":
                case "demote":
                case "transfer":
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
                case "ally":
                case "enemy":
                case "war":
                case "peace":
                    return clanService.listClans();
                case "deposit":
                case "withdraw":
                case "points":
                    return List.of("<amount>");
                case "rename":
                case "desc":
                case "prefix":
                case "title":
                case "motd":
                    return List.of("<text>");
                case "create":
                    return List.of("<name>");
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("ally")) {
            return clanService.listClans();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("enemy")) {
            return clanService.listClans();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("points")) {
            return List.of("<amount>");
        }
        return List.of();
    }
}
