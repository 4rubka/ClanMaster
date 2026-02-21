package com.clanmaster.gui;

import com.clanmaster.model.Clan;
import com.clanmaster.model.ClanMember;
import com.clanmaster.model.ClanRank;
import com.clanmaster.service.BonusService;
import com.clanmaster.service.ClanService;
import com.clanmaster.util.ItemBuilder;
import com.clanmaster.util.MessageResolver;
import com.clanmaster.util.Text;
import com.clanmaster.util.FeatureManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides GUI views for clan interactions.
 */
public class ClanMenu implements Listener {

    private final ClanService clanService;
    private final BonusService bonusService;
    private final MessageResolver messages;
    private final FeatureManager featureManager;

    public ClanMenu(Object plugin, ClanService clanService, BonusService bonusService, MessageResolver messages, FeatureManager featureManager) {
        this.clanService = clanService;
        this.bonusService = bonusService;
        this.messages = messages;
        this.featureManager = featureManager;
    }

    /**
     * Opens main menu for player.
     */
    public void openMain(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) {
            openNoClanMenu(player);
        } else {
            openWithClanMenu(player, clan);
        }
    }

    /**
     * Opens menu for players without a clan.
     */
    private void openNoClanMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, Text.color("&6&l✦ &fClan Browser"));
        
        // Decorate borders
        ItemStack pane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        for (int slot : border) {
            inv.setItem(slot, pane);
        }

        // Title/Info item
        inv.setItem(13, new ItemBuilder(Material.NETHER_STAR)
                .name("&6&lClanMaster")
                .lore(Arrays.asList(
                        "&7Welcome to the clan system!",
                        "&eFind a clan to join or view the top clans.",
                        "",
                        "&7Your stats:",
                        "&fKills: &a" + getPlayerKills(player),
                        "&fDeaths: &c" + getPlayerDeaths(player),
                        "&fK/D: &b" + String.format("%.2f", getKD(player))
                ))
                .build());

        // Clan Top Section
        inv.setItem(22, new ItemBuilder(Material.GOLD_BLOCK)
                .name("&6&lTop Clans")
                .lore(Arrays.asList(
                        "&7Click to view the top clans",
                        "&eon the server!"
                ))
                .build());

        // Clan List Section
        inv.setItem(31, new ItemBuilder(Material.BOOK)
                .name("&b&lAll Clans")
                .lore(Arrays.asList(
                        "&7Browse all clans",
                        "&eand find one to join!"
                ))
                .build());

        // Player Stats
        inv.setItem(40, new ItemBuilder(Material.DIAMOND_SWORD)
                .name("&c&lYour Stats")
                .lore(Arrays.asList(
                        "&7Kills: &a" + getPlayerKills(player),
                        "&7Deaths: &c" + getPlayerDeaths(player),
                        "&7K/D Ratio: &b" + String.format("%.2f", getKD(player)),
                        "",
                        "&eJoin a clan for bonuses!"
                ))
                .build());

        // Info/Help
        inv.setItem(48, new ItemBuilder(Material.PAPER)
                .name("&a&lHelp")
                .lore(Arrays.asList(
                        "&7Click to see clan commands",
                        "",
                        "&e/clan top &7- View top clans",
                        "&e/clan list &7- List all clans",
                        "&e/clan info <clan> &7- Clan details"
                ))
                .build());

        player.openInventory(inv);
    }

    /**
     * Opens menu for players with a clan.
     */
    private void openWithClanMenu(Player player, Clan clan) {
        String title = featureManager.getMenuTextColored("main-title", "&#ff4faf&l✦ &fClan Menu");
        Inventory inv = Bukkit.createInventory(player, 54, Text.color(title));
        decorate(inv, Material.CYAN_STAINED_GLASS_PANE);

        inv.setItem(4, titleItem(clan));
        inv.setItem(20, statsItem(clan));
        
        // Bonuses item (if enabled)
        if (featureManager.isBonusesEnabled()) {
            inv.setItem(22, bonusesItem(clan));
        }
        
        inv.setItem(24, managementItem());
        inv.setItem(30, settingsItem());
        inv.setItem(32, membersItem(clan));
        inv.setItem(40, infoItem(clan));
        
        // Wars item (if enabled)
        if (featureManager.isClanWarsEnabled()) {
            inv.setItem(10, warsItem(clan));
        }
        
        // Achievements item (if enabled)
        if (featureManager.isAchievementsEnabled()) {
            inv.setItem(16, achievementsItem(clan));
        }

        player.openInventory(inv);
    }

    private ItemStack titleItem(Clan clan) {
        String gradientName = Text.gradient("ff4faf", "5fd9ff", clan.getName());
        return new ItemBuilder(Material.NETHER_STAR)
                .name("&l" + gradientName)
                .lore(Arrays.asList(
                        "&7♥ &f" + messages.get("menu.level") + ": &d" + clan.getLevel(),
                        "&7✦ &fXP: &b" + clan.getXp(),
                        "&7✸ &f" + messages.get("menu.members") + ": &a" + clan.getMembers().size(),
                        "",
                        "&7Leader: &e" + getLeaderName(clan),
                        "&7Title: &6" + (clan.getTitle().isEmpty() ? "None" : clan.getTitle())
                ))
                .build();
    }

    private ItemStack statsItem(Clan clan) {
        String name = featureManager.getMenuTextColored("stats", "&#5fd9ffStatistics");
        String levelText = featureManager.getMenuTextColored("level", "Level");
        String membersText = featureManager.getMenuTextColored("members-count", "Members");
        
        return new ItemBuilder(Material.BOOK)
                .name(Text.color(name))
                .lore(Arrays.asList(
                        "&7" + levelText + ": &a" + clan.getLevel(),
                        "&7XP: &a" + clan.getXp(),
                        "&7" + membersText + ": &a" + clan.getMembers().size(),
                        "&7Coins: &6" + clan.getCoins(),
                        "&7War Points: &c" + clan.getWarPoints(),
                        "&7Wins: &a" + clan.getWins() + " &7Losses: &c" + clan.getLosses(),
                        "",
                        "&eClick for more details"
                ))
                .build();
    }

    private ItemStack bonusesItem(Clan clan) {
        String name = featureManager.getMenuTextColored("bonuses", "&#6cffc9Bonuses");
        String levelText = featureManager.getMenuTextColored("level", "Level");
        String privilegeText = featureManager.getMenuTextColored("privilege", "Privilege");
        
        BonusService.Bonus bonus = bonusService.getBonus(clan.getLevel());
        return new ItemBuilder(Material.EMERALD)
                .name(Text.color(name))
                .lore(Arrays.asList(
                        "&7Current " + levelText + ": &a" + clan.getLevel(),
                        "",
                        "&7Active Bonuses:",
                        "&f• Coins: &a" + bonus.getCoins(),
                        "&f• XP Boost: &a" + bonus.getXp(),
                        "&f• " + privilegeText + ": &a" + bonus.getPrivilege(),
                        "",
                        "&eLevel up for better bonuses!"
                ))
                .build();
    }

    private ItemStack managementItem() {
        String name = featureManager.getMenuTextColored("manage", "&#f3b4ffManagement");
        String desc = featureManager.getMenuTextColored("manage-desc", "&7Manage members");
        
        return new ItemBuilder(Material.ANVIL)
                .name(Text.color(name))
                .lore(Arrays.asList(
                        desc,
                        "",
                        "&7• Invite members",
                        "&7• Kick members",
                        "&7• Promote/Demote",
                        "&7• Transfer leadership",
                        "",
                        "&eClick to manage"
                ))
                .build();
    }

    private ItemStack membersItem(Clan clan) {
        String name = featureManager.getMenuTextColored("members", "&#9b8cffMembers");
        String desc = featureManager.getMenuTextColored("members-desc", "&7Manage/view members");
        String clickText = featureManager.getMenuTextColored("click-demote", "&7Click to change rank");
        
        return new ItemBuilder(Material.PLAYER_HEAD)
                .name(Text.color(name))
                .lore(Arrays.asList(
                        desc,
                        "",
                        "&7Online: &a" + getOnlineMembersCount(clan),
                        "&7Total: &e" + clan.getMembers().size(),
                        "",
                        "&eClick to view members"
                ))
                .build();
    }

    private ItemStack settingsItem() {
        String name = featureManager.getMenuTextColored("settings", "&#ffd166Settings");
        String desc = featureManager.getMenuTextColored("settings-desc", "&7Clan preferences");
        
        return new ItemBuilder(Material.COMPARATOR)
                .name(Text.color(name))
                .lore(Arrays.asList(
                        desc,
                        "",
                        "&7• Toggle friendly fire",
                        "&7• Set clan prefix",
                        "&7• Manage clan home",
                        "",
                        "&eClick for settings"
                ))
                .build();
    }

    private ItemStack infoItem(Clan clan) {
        String name = featureManager.getMenuTextColored("info", "&#5fd9ffInfo");
        
        return new ItemBuilder(Material.PAPER)
                .name(Text.color(name))
                .lore(Arrays.asList(
                        "&7Level: &a" + clan.getLevel(),
                        "&7XP: &b" + clan.getXp(),
                        "&7Coins: &6" + clan.getCoins(),
                        "",
                        "&7Members: &a" + clan.getMembers().size(),
                        "&7Allies: &e" + clan.getAllies().size(),
                        "&7Enemies: &c" + clan.getEnemies().size(),
                        "",
                        "&eClick for detailed info"
                ))
                .build();
    }

    private ItemStack warsItem(Clan clan) {
        String name = featureManager.getMenuTextColored("wars", "&c&lWars");
        String desc = featureManager.getMenuTextColored("wars-desc", "&7Manage clan wars");
        
        int activeWars = clan.getActiveWars().size();
        return new ItemBuilder(activeWars > 0 ? Material.REDSTONE_BLOCK : Material.IRON_SWORD)
                .name(Text.color(name))
                .lore(Arrays.asList(
                        desc,
                        "",
                        "&7Active Wars: &c" + activeWars,
                        "&7Wins: &a" + clan.getWins(),
                        "&7Losses: &c" + clan.getLosses(),
                        "",
                        activeWars > 0 ? "&eClick to view wars" : "&7No active wars"
                ))
                .build();
    }

    private ItemStack achievementsItem(Clan clan) {
        String name = featureManager.getMenuTextColored("achievements", "&#5fd9ffAchievements");
        String desc = featureManager.getMenuTextColored("achievements-desc", "&7Clan rewards");
        
        int achievements = clan.getAchievements().size();
        return new ItemBuilder(Material.BEACON)
                .name(Text.color(name))
                .lore(Arrays.asList(
                        desc,
                        "",
                        "&7Achievements: &6" + achievements,
                        "",
                        "&eClick to view"
                ))
                .build();
    }

    private void decorate(Inventory inv, Material material) {
        ItemStack pane = new ItemBuilder(material).name(ChatColor.RESET.toString()).build();
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        for (int slot : border) {
            inv.setItem(slot, pane);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        String strippedTitle = ChatColor.stripColor(title);
        
        // Clan Browser (no clan menu)
        if (strippedTitle.contains("Clan Browser")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            int slot = event.getSlot();

            if (slot == 22) {
                openTopClansMenu(player);
            } else if (slot == 31) {
                openAllClansMenu(player);
            } else if (slot == 40) {
                showPlayerStats(player);
            } else if (slot == 48) {
                showClanHelp(player);
            }
            return;
        }

        // Main clan menu
        if (strippedTitle.contains("Clan Menu")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            int slot = event.getSlot();

            if (slot == 24 || slot == 32) {
                openManage(player, 0);
            } else if (slot == 20) {
                openStats(player);
            } else if (slot == 22 && featureManager.isBonusesEnabled()) {
                openBonuses(player);
            } else if (slot == 30) {
                openSettings(player);
            } else if (slot == 40) {
                showDetailedInfo(player);
            } else if (slot == 10 && featureManager.isClanWarsEnabled()) {
                openWarsMenu(player);
            } else if (slot == 16 && featureManager.isAchievementsEnabled()) {
                openAchievementsMenu(player);
            }
            return;
        }

        // Top Clans menu
        if (strippedTitle.contains("Top Clans")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            handleTopClansClick(player, event.getSlot(), title);
            return;
        }

        // All Clans menu
        if (strippedTitle.contains("All Clans")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            handleAllClansClick(player, event.getSlot(), title);
            return;
        }

        // Management menu
        if (strippedTitle.contains("Management")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            handleManageClick(player, event);
            return;
        }

        // Settings menu
        if (strippedTitle.contains("Settings")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            handleSettingsClick(player, event.getSlot());
            return;
        }

        // Active Wars menu
        if (strippedTitle.contains("Active Wars")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            if (event.getSlot() == 48) {
                openMain(player);
            }
            return;
        }

        // Achievements menu
        if (strippedTitle.contains("Achievements")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            if (event.getSlot() == 48) {
                openMain(player);
            }
            return;
        }

        // Clan Stats menu
        if (strippedTitle.contains("Clan Stats")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            if (event.getSlot() == 48) {
                openMain(player);
            }
            return;
        }

        // Clan Bonuses menu
        if (strippedTitle.contains("Clan Bonuses")) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 54) return;
            if (event.getSlot() == 48) {
                openMain(player);
            }
            return;
        }
    }

    private void handleTopClansClick(Player player, int slot, String title) {
        if (slot == 48) {
            openMain(player);
            return;
        }
        int page = parsePage(title);
        if (slot == 45 && page > 0) {
            openTopClansMenu(player, page - 1);
        } else if (slot == 53) {
            openTopClansMenu(player, page + 1);
        }
    }

    private void handleAllClansClick(Player player, int slot, String title) {
        if (slot == 48) {
            openMain(player);
            return;
        }
        int page = parsePage(title);
        if (slot == 45 && page > 0) {
            openAllClansMenu(player, page - 1);
        } else if (slot == 53) {
            openAllClansMenu(player, page + 1);
        }
    }

    private void handleManageClick(Player player, InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        if (current == null || !current.hasItemMeta()) return;

        String title = event.getView().getTitle();
        int page = parsePage(title);

        // Back button
        if (event.getSlot() == 48) {
            openMain(player);
            return;
        }

        if (current.getType() == Material.ARROW) {
            if (event.getSlot() == 45 && page > 0) {
                openManage(player, page - 1);
            } else if (event.getSlot() == 53) {
                openManage(player, page + 1);
            }
            return;
        }

        String name = ChatColor.stripColor(current.getItemMeta().getDisplayName()).replace("§a", "");
        UUID targetId = Bukkit.getOfflinePlayer(name).getUniqueId();
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;
        if (player.getUniqueId().equals(targetId)) return;

        ClanRank actorRank = clan.getMembers().get(player.getUniqueId()).getRank();
        ClanRank targetRank = clan.getMembers().get(targetId).getRank();

        if (actorRank.canManage(targetRank)) {
            if (targetRank == ClanRank.MEMBER) {
                clanService.promote(player.getUniqueId(), targetId);
            } else {
                clanService.demote(player.getUniqueId(), targetId);
            }
            player.sendMessage(Text.color(messages.get("menu.changed")));
            openManage(player, page);
        }
    }

    private void handleSettingsClick(Player player, int slot) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;

        ClanMember member = clan.getMembers().get(player.getUniqueId());
        if (member == null || member.getRank() != ClanRank.LEADER) {
            player.sendMessage(Text.color(messages.get("error.not-leader")));
            player.closeInventory();
            return;
        }

        if (slot == 11) {
            boolean state = clanService.togglePvp(player.getUniqueId());
            player.sendMessage(Text.color(state ? messages.get("clan.pvp-on") : messages.get("clan.pvp-off")));
            openSettings(player);
        } else if (slot == 15 && featureManager.isClanHomeEnabled()) {
            boolean ok = clanService.setHome(player.getUniqueId(), com.clanmaster.util.LocationUtil.toString(player.getLocation()));
            player.sendMessage(Text.color(ok ? messages.get("clan.home-set") : messages.get("error.not-leader")));
            openSettings(player);
        } else if (slot == 29 && featureManager.isClanHomeEnabled()) {
            boolean ok = clanService.delHome(player.getUniqueId());
            player.sendMessage(Text.color(ok ? messages.get("clan.home-set") : messages.get("error.not-leader")));
            openSettings(player);
        } else if (slot == 33 && featureManager.isClanHomeEnabled()) {
            String locString = clanService.getHome(player.getUniqueId());
            if (locString == null) {
                player.sendMessage(Text.color(messages.get("clan.home-missing")));
            } else {
                var loc = com.clanmaster.util.LocationUtil.fromString(locString);
                if (loc != null) {
                    player.teleport(loc);
                    player.sendMessage(Text.color(messages.get("clan.home-teleport")));
                    player.closeInventory();
                } else {
                    player.sendMessage(Text.color(messages.get("clan.home-missing")));
                }
            }
        } else if (slot == 48) {
            openMain(player);
        }
    }

    private void openTopClansMenu(Player player) {
        openTopClansMenu(player, 0);
    }

    private void openTopClansMenu(Player player, int page) {
        Inventory inv = Bukkit.createInventory(player, 54, Text.color("&6&lTop Clans &7#" + (page + 1)));
        decorate(inv, Material.YELLOW_STAINED_GLASS_PANE);
        
        List<Clan> topClans = clanService.topByLevel().values().stream().toList();
        int start = page * 7;
        int index = 10;
        
        for (int i = start; i < Math.min(start + 7, topClans.size()); i++) {
            Clan clan = topClans.get(i);
            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .name("&6&l#" + (i + 1) + " &f" + clan.getName())
                    .lore(Arrays.asList(
                            "&7Level: &a" + clan.getLevel(),
                            "&7XP: &b" + clan.getXp(),
                            "&7Members: &e" + clan.getMembers().size(),
                            "&7War Points: &c" + clan.getWarPoints(),
                            "",
                            "&eClick to view info"
                    ))
                    .build();
            inv.setItem(index, head);
            index += 9;
        }
        
        inv.setItem(48, new ItemBuilder(Material.ARROW)
                .name("&c&lBack")
                .lore(Arrays.asList("&7Return to main menu"))
                .build());
        inv.setItem(45, navItem(Material.ARROW, messages.get("menu.prev"), page > 0));
        inv.setItem(53, navItem(Material.ARROW, messages.get("menu.next"), start + 7 < topClans.size()));
        
        player.openInventory(inv);
    }

    private void openAllClansMenu(Player player) {
        openAllClansMenu(player, 0);
    }

    private void openAllClansMenu(Player player, int page) {
        Inventory inv = Bukkit.createInventory(player, 54, Text.color("&b&lAll Clans &7#" + (page + 1)));
        decorate(inv, Material.BLUE_STAINED_GLASS_PANE);
        
        List<String> allClans = clanService.listClans();
        int start = page * 7;
        int index = 10;
        
        for (int i = start; i < Math.min(start + 7, allClans.size()); i++) {
            String clanName = allClans.get(i);
            Clan clan = clanService.getClanByName(clanName).orElse(null);
            if (clan == null) continue;
            
            ItemStack head = new ItemBuilder(Material.BOOK)
                    .name("&b&l" + clanName)
                    .lore(Arrays.asList(
                            "&7Level: &a" + clan.getLevel(),
                            "&7XP: &b" + clan.getXp(),
                            "&7Members: &e" + clan.getMembers().size(),
                            "&7Activity: " + clanService.getActivityStatus(clan),
                            "",
                            "&eClick to view info"
                    ))
                    .build();
            inv.setItem(index, head);
            index += 9;
        }
        
        inv.setItem(48, new ItemBuilder(Material.ARROW)
                .name("&c&lBack")
                .lore(Arrays.asList("&7Return to main menu"))
                .build());
        inv.setItem(45, navItem(Material.ARROW, messages.get("menu.prev"), page > 0));
        inv.setItem(53, navItem(Material.ARROW, messages.get("menu.next"), start + 7 < allClans.size()));
        
        player.openInventory(inv);
    }

    private void openSettings(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;

        String title = featureManager.getMenuTextColored("settings-title", "&#ffd166&lSettings");
        Inventory inv = Bukkit.createInventory(player, 54, Text.color(title));
        decorate(inv, Material.CYAN_STAINED_GLASS_PANE);

        boolean pvpState = clan.isFriendlyFire();
        boolean hasHome = featureManager.isClanHomeEnabled() && clan.getHome() != null && !clan.getHome().isEmpty();

        // Friendly Fire toggle
        String ffName = pvpState ? 
                featureManager.getMenuTextColored("friendly-fire-on", "&c&lFriendly Fire: ON") :
                featureManager.getMenuTextColored("friendly-fire-off", "&a&lFriendly Fire: OFF");
        inv.setItem(11, new ItemBuilder(pvpState ? Material.REDSTONE_BLOCK : Material.LIME_WOOL)
                .name(Text.color(ffName))
                .lore(Arrays.asList(
                        "&7Current: " + (pvpState ? "&cEnabled" : "&aDisabled"),
                        "",
                        "&eClick to toggle"
                ))
                .build());

        // Clan Home items (if enabled)
        if (featureManager.isClanHomeEnabled()) {
            inv.setItem(15, new ItemBuilder(Material.RED_BED)
                    .name(featureManager.getMenuTextColored("set-home", "&a&lSet Clan Home"))
                    .lore(Arrays.asList(
                            "&7Set the clan home to your",
                            "&7current location",
                            "",
                            "&eClick to set"
                    ))
                    .build());

            inv.setItem(29, new ItemBuilder(Material.SHEARS)
                    .name(featureManager.getMenuTextColored("delete-home", "&e&lDelete Clan Home"))
                    .lore(Arrays.asList(
                            "&7Remove the clan home",
                            hasHome ? "&7Current: &aSet" : "&7Current: &cNot set",
                            "",
                            "&eClick to delete"
                    ))
                    .build());

            inv.setItem(33, new ItemBuilder(Material.ENDER_PEARL)
                    .name(featureManager.getMenuTextColored("teleport-home", "&d&lTeleport to Clan Home"))
                    .lore(Arrays.asList(
                            "&7Teleport to your clan home",
                            hasHome ? "&7Status: &aAvailable" : "&7Status: &cNot set",
                            "",
                            "&eClick to teleport"
                    ))
                    .build());
        }

        inv.setItem(48, new ItemBuilder(Material.ARROW)
                .name(featureManager.getMenuTextColored("back", "&c&lBack"))
                .lore(Arrays.asList(featureManager.getMenuTextColored("back-desc", "&7Return to main menu")))
                .build());

        player.openInventory(inv);
    }

    private void openManage(Player player, int page) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;

        String manageTitle = featureManager.getMenuTextColored("manage-title", "&#f3b4ff&lManagement");
        Inventory inv = Bukkit.createInventory(player, 54, Text.color(manageTitle + " #" + (page + 1)));
        decorate(inv, Material.PURPLE_STAINED_GLASS_PANE);
        
        int start = page * 28;
        int index = 10;
        var members = clan.getMembers().values().stream().toList();
        
        for (int i = start; i < Math.min(start + 28, members.size()); i++) {
            ClanMember member = members.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
            boolean isOnline = offlinePlayer.isOnline();
            
            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .name((isOnline ? "&a" : "&7") + offlinePlayer.getName())
                    .lore(Arrays.asList(
                            "&7Rank: " + member.getRank().name(),
                            isOnline ? "&aOnline" : "&cOffline",
                            "",
                            messages.get("menu.click-demote")
                    ))
                    .build();
            inv.setItem(index++, head);
            if ((index + 1) % 9 == 0) {
                index += 2;
            }
        }
        
        inv.setItem(45, navItem(Material.ARROW, messages.get("menu.prev"), page > 0));
        inv.setItem(53, navItem(Material.ARROW, messages.get("menu.next"), start + 28 < members.size()));

        // Back button
        inv.setItem(48, new ItemBuilder(Material.ARROW)
                .name(featureManager.getMenuTextColored("back", "&c&lBack"))
                .lore(Arrays.asList(featureManager.getMenuTextColored("back-desc", "&7Return to main menu")))
                .build());

        player.openInventory(inv);
    }

    private void openWarsMenu(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;

        String title = featureManager.getMenuTextColored("wars-title", "&c&lActive Wars");
        String noWarsText = featureManager.getMenuTextColored("no-wars", "&7No Active Wars");
        Inventory inv = Bukkit.createInventory(player, 54, Text.color(title));
        decorate(inv, Material.RED_STAINED_GLASS_PANE);

        int index = 10;
        if (clan.getActiveWars().isEmpty()) {
            inv.setItem(22, new ItemBuilder(Material.IRON_SWORD)
                    .name(noWarsText)
                    .lore(Arrays.asList("&eDeclare a war with /clan war <clan>"))
                    .build());
        } else {
            for (Map.Entry<String, Clan.War> entry : clan.getActiveWars().entrySet()) {
                Clan.War war = entry.getValue();
                String vsTitle = featureManager.getMenuTextColored("war-vs", "&c&lvs {clan}")
                        .replace("{clan}", war.getEnemyClan());
                ItemStack item = new ItemBuilder(Material.REDSTONE_BLOCK)
                        .name(Text.color(vsTitle))
                        .lore(Arrays.asList(
                                "&7Kills: &f" + war.getKillsClan1() + " &7- &f" + war.getKillsClan2(),
                                "&7Started: &e" + new java.util.Date(war.getStartTime()).toLocaleString(),
                                "",
                                war.isActive() ? "&aActive" : "&cEnded"
                        ))
                        .build();
                inv.setItem(index, item);
                index += 9;
            }
        }

        // Back button
        inv.setItem(48, new ItemBuilder(Material.ARROW)
                .name(featureManager.getMenuTextColored("back", "&c&lBack"))
                .lore(Arrays.asList(featureManager.getMenuTextColored("back-desc", "&7Return to main menu")))
                .build());

        player.openInventory(inv);
    }

    private void openAchievementsMenu(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;

        String title = featureManager.getMenuTextColored("achievements-title", "&6&lAchievements");
        String noAchievementsText = featureManager.getMenuTextColored("no-achievements", "&7No Achievements");
        Inventory inv = Bukkit.createInventory(player, 54, Text.color(title));
        decorate(inv, Material.YELLOW_STAINED_GLASS_PANE);

        int index = 10;
        if (clan.getAchievements().isEmpty()) {
            inv.setItem(22, new ItemBuilder(Material.BOOK)
                    .name(noAchievementsText)
                    .lore(Arrays.asList("&eComplete tasks to earn achievements!"))
                    .build());
        } else {
            for (String achievement : clan.getAchievements()) {
                ItemStack item = new ItemBuilder(Material.BEACON)
                        .name("&6&l" + achievement)
                        .lore(Arrays.asList("&eClick to claim reward"))
                        .build();
                inv.setItem(index, item);
                index += 9;
            }
        }

        // Back button
        inv.setItem(48, new ItemBuilder(Material.ARROW)
                .name(featureManager.getMenuTextColored("back", "&c&lBack"))
                .lore(Arrays.asList(featureManager.getMenuTextColored("back-desc", "&7Return to main menu")))
                .build());

        player.openInventory(inv);
    }

    private ItemStack navItem(Material mat, String name, boolean enabled) {
        if (!enabled) return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        return new ItemBuilder(mat).name(name).build();
    }

    private int parsePage(String title) {
        int idx = title.lastIndexOf('#');
        if (idx == -1) return 0;
        try {
            return Integer.parseInt(ChatColor.stripColor(title.substring(idx + 1)).trim()) - 1;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void openStats(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;

        String title = featureManager.getMenuTextColored("stats-title", "&b&lClan Stats");
        Inventory inv = Bukkit.createInventory(player, 54, Text.color(title));
        decorate(inv, Material.BLUE_STAINED_GLASS_PANE);

        // Stats info item
        ItemStack info = new ItemBuilder(Material.BOOK)
                .name("&6&lClan Statistics")
                .lore(Arrays.asList(
                        "&7Name: &f" + clan.getName(),
                        "&7Level: &a" + clan.getLevel(),
                        "&7XP: &b" + clan.getXp(),
                        "&7Members: &a" + clan.getMembers().size(),
                        "&7Coins: &6" + clan.getCoins(),
                        "&7War Points: &c" + clan.getWarPoints(),
                        "&7Wins: &a" + clan.getWins(),
                        "&7Losses: &c" + clan.getLosses(),
                        "&7Allies: &e" + clan.getAllies().size(),
                        "&7Enemies: &c" + clan.getEnemies().size()
                ))
                .build();
        inv.setItem(22, info);

        // Back button
        inv.setItem(48, new ItemBuilder(Material.ARROW)
                .name(featureManager.getMenuTextColored("back", "&c&lBack"))
                .lore(Arrays.asList(featureManager.getMenuTextColored("back-desc", "&7Return to main menu")))
                .build());

        player.openInventory(inv);
    }

    private void openBonuses(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;

        String title = featureManager.getMenuTextColored("bonuses-title", "&a&lClan Bonuses");
        Inventory inv = Bukkit.createInventory(player, 54, Text.color(title));
        decorate(inv, Material.GREEN_STAINED_GLASS_PANE);

        BonusService.Bonus currentBonus = bonusService.getBonus(clan.getLevel());
        BonusService.Bonus nextBonus = bonusService.getBonus(clan.getLevel() + 1);

        // Current bonus info
        String currentTitle = featureManager.getMenuTextColored("current-bonus", "&a&lCurrent Bonus (Level {level})")
                .replace("{level}", String.valueOf(clan.getLevel()));
        ItemStack current = new ItemBuilder(Material.EMERALD_BLOCK)
                .name(Text.color(currentTitle))
                .lore(Arrays.asList(
                        "&7Coins: &6+" + currentBonus.getCoins(),
                        "&7XP Boost: &b+" + currentBonus.getXp(),
                        "&7Privilege: &e" + (currentBonus.getPrivilege().isEmpty() ? "None" : currentBonus.getPrivilege()),
                        "",
                        "&eKeep leveling up for better bonuses!"
                ))
                .build();
        inv.setItem(22, current);

        // Next bonus info
        if (nextBonus.getLevel() <= 10) {
            String nextTitle = featureManager.getMenuTextColored("next-bonus", "&e&lNext Bonus (Level {level})")
                    .replace("{level}", String.valueOf(nextBonus.getLevel()));
            ItemStack next = new ItemBuilder(Material.GOLD_BLOCK)
                    .name(Text.color(nextTitle))
                    .lore(Arrays.asList(
                            "&7Coins: &6+" + nextBonus.getCoins(),
                            "&7XP Boost: &b+" + nextBonus.getXp(),
                            "&7Privilege: &e" + (nextBonus.getPrivilege().isEmpty() ? "None" : nextBonus.getPrivilege()),
                            "",
                            "&eLevel up to unlock!"
                    ))
                    .build();
            inv.setItem(24, next);
        }

        // Back button
        inv.setItem(48, new ItemBuilder(Material.ARROW)
                .name(featureManager.getMenuTextColored("back", "&c&lBack"))
                .lore(Arrays.asList(featureManager.getMenuTextColored("back-desc", "&7Return to main menu")))
                .build());

        player.openInventory(inv);
    }

    private void showPlayerStats(Player player) {
        player.sendMessage(Text.color("&6&l=== Your Stats ==="));
        player.sendMessage(Text.color("&7Kills: &a" + getPlayerKills(player)));
        player.sendMessage(Text.color("&7Deaths: &c" + getPlayerDeaths(player)));
        player.sendMessage(Text.color("&7K/D Ratio: &b" + String.format("%.2f", getKD(player))));
        player.sendMessage(Text.color(""));
        player.sendMessage(Text.color("&eJoin a clan to get exclusive bonuses!"));
    }

    private void showClanHelp(Player player) {
        String[] lines = {
                "&8&l╔══ &x&f&f&9&b&f&fClanMaster &7/&f help",
                "&8&l║ &a/clan top &7- View top clans",
                "&8&l║ &a/clan list &7- List all clans",
                "&8&l║ &a/clan info <clan> &7- Clan details",
                "&8&l║ &a/clan menu &7- Open this menu",
                "&8&l╚══ &7Find a clan and ask to join!"
        };
        for (String line : lines) {
            player.sendMessage(Text.color(line));
        }
    }

    private void showDetailedInfo(Player player) {
        Clan clan = clanService.getClanOrNull(player.getUniqueId());
        if (clan == null) return;
        
        player.sendMessage(Text.color("&6&l=== Clan Info ==="));
        player.sendMessage(Text.color("&7Name: &f" + clan.getName()));
        player.sendMessage(Text.color("&7Level: &a" + clan.getLevel() + " &7XP: &b" + clan.getXp()));
        player.sendMessage(Text.color("&7Members: &a" + clan.getMembers().size()));
        player.sendMessage(Text.color("&7Coins: &6" + clan.getCoins()));
        player.sendMessage(Text.color("&7War Points: &c" + clan.getWarPoints()));
        player.sendMessage(Text.color("&7Wins: &a" + clan.getWins() + " &7Losses: &c" + clan.getLosses()));
        player.sendMessage(Text.color("&7Allies: &e" + clan.getAllies().size()));
        player.sendMessage(Text.color("&7Enemies: &c" + clan.getEnemies().size()));
        player.sendMessage(Text.color("&7Title: &6" + clan.getTitle()));
        player.sendMessage(Text.color("&7Achievements: &6" + clan.getAchievements().size()));
    }

    // Helper methods
    private int getPlayerKills(Player player) {
        return player.getStatistic(org.bukkit.Statistic.PLAYER_KILLS);
    }

    private int getPlayerDeaths(Player player) {
        return player.getStatistic(org.bukkit.Statistic.DEATHS);
    }

    private double getKD(Player player) {
        int deaths = getPlayerDeaths(player);
        if (deaths == 0) return getPlayerKills(player);
        return (double) getPlayerKills(player) / deaths;
    }

    private int getOnlineMembersCount(Clan clan) {
        AtomicInteger count = new AtomicInteger();
        clan.getMembers().keySet().forEach(uuid -> {
            if (Bukkit.getPlayer(uuid) != null) count.incrementAndGet();
        });
        return count.get();
    }

    private String getLeaderName(Clan clan) {
        for (Map.Entry<UUID, ClanMember> entry : clan.getMembers().entrySet()) {
            if (entry.getValue().getRank() == ClanRank.LEADER) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
                return p.getName() != null ? p.getName() : "Unknown";
            }
        }
        return "Unknown";
    }
}
