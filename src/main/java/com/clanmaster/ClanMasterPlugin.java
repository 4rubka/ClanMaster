package com.clanmaster;

import com.clanmaster.api.ClanAPI;
import com.clanmaster.command.ClanCommand;
import com.clanmaster.data.ClanStorage;
import com.clanmaster.data.StorageType;
import com.clanmaster.data.json.JsonClanStorage;
import com.clanmaster.data.sql.SqlClanStorage;
import com.clanmaster.gui.ClanMenu;
import com.clanmaster.util.integration.IntegrationManager;
import com.clanmaster.placeholder.ClanPlaceholderExpansion;
import com.clanmaster.service.BonusService;
import com.clanmaster.service.ClanService;
import com.clanmaster.util.MessageResolver;
import com.clanmaster.util.Text;
import com.clanmaster.command.ClanChatCommand;
import com.clanmaster.command.ClanChatSpyCommand;
import com.clanmaster.command.ClanChestCommand;
import com.clanmaster.util.FeatureManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * Entry point for the ClanMaster plugin. Sets up services, storage, commands, GUI providers, and optional integrations.
 */
public final class ClanMasterPlugin extends JavaPlugin {

    private ClanService clanService;
    private BonusService bonusService;
    private MessageResolver messages;
    private FeatureManager featureManager;
    private Economy economy;
    private ClanMenu clanMenu;
    private IntegrationManager integrationManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("lang/en.yml", false);
        saveResource("lang/uk.yml", false);
        saveResource("lang/de.yml", false);
        saveResource("lang/fr.yml", false);

        this.messages = new MessageResolver(this);
        this.featureManager = new FeatureManager(this);
        this.economy = setupEconomy();

        ClanStorage storage = createStorage();
        this.clanService = new ClanService(this, storage, messages, economy);
        this.bonusService = new BonusService(getConfig());
        this.clanMenu = new ClanMenu(this, clanService, bonusService, messages, featureManager);
        this.integrationManager = new IntegrationManager(this, clanService, messages);

        ClanAPI.initialize(this, clanService, bonusService);

        registerCommands();
        registerListeners();
        registerPlaceholder();

        getLogger().info("ClanMaster enabled.");
    }

    @Override
    public void onDisable() {
        if (clanService != null) {
            clanService.shutdown();
        }
        getLogger().info("ClanMaster disabled.");
    }

    /**
     * Registers main command executor and tab completer.
     */
    private void registerCommands() {
        ClanCommand cmd = new ClanCommand(this, clanService, bonusService, messages, economy, clanMenu, featureManager);
        getCommand("clan").setExecutor(cmd);
        getCommand("clan").setTabCompleter(cmd);
        getCommand("clanchat").setExecutor(new ClanChatCommand(clanService));
        getCommand("clanchatspy").setExecutor(new ClanChatSpyCommand(clanService));
        getCommand("clanchest").setExecutor(new ClanChestCommand(clanService));
        var adminCmd = new com.clanmaster.command.ClanAdminCommand(this, clanService, messages);
        getCommand("clanadmin").setExecutor(adminCmd);
        getCommand("clanadmin").setTabCompleter(new com.clanmaster.command.ClanAdminTab());
    }

    /**
     * Registers Bukkit listeners for GUI handling.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(clanMenu, this);
        getServer().getPluginManager().registerEvents(integrationManager, this);
    }

    /**
     * Creates storage backend based on configuration choice.
     *
     * @return storage implementation
     */
    private ClanStorage createStorage() {
        FileConfiguration config = getConfig();
        StorageType type = StorageType.valueOf(config.getString("storage.type", "JSON").toUpperCase());
        File dataFolder = getDataFolder();
        switch (type) {
            case MYSQL:
                return new SqlClanStorage(config, getLogger());
            case SQLITE:
                return new SqlClanStorage(config, getLogger());
            case JSON:
            default:
                return new JsonClanStorage(new File(dataFolder, "clans.json"), getLogger());
        }
    }

    /**
     * Attempts to connect to Vault economy.
     *
     * @return economy instance or null if not available
     */
    private Economy setupEconomy() {
        String provider = getConfig().getString("economy.provider", "VAULT").toUpperCase();
        if (!provider.equals("VAULT")) {
            getLogger().info("Economy provider disabled via config.");
            return null;
        }
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found. Economy costs and rewards will be skipped.");
            return null;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("Vault economy provider missing.");
            return null;
        }
        return rsp.getProvider();
    }

    /**
     * Registers PlaceholderAPI expansion when the dependency is present.
     */
    private void registerPlaceholder() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholderExpansion(clanService, this).register();
            getLogger().info("PlaceholderAPI hook registered.");
        }
    }

    public ClanService getClanService() {
        return clanService;
    }

    public BonusService getBonusService() {
        return bonusService;
    }

    public MessageResolver getMessages() {
        return messages;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
