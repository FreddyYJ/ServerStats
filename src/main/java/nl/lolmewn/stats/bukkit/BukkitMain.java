package nl.lolmewn.stats.bukkit;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.DefaultStatManager;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.StatsAPI;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.storage.StorageEngine;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.command.StatsCommand;
import nl.lolmewn.stats.mysql.MySQLConfig;
import nl.lolmewn.stats.mysql.MySQLStorage;
import nl.lolmewn.stats.stats.bukkit.BukkitArrows;
import nl.lolmewn.stats.stats.bukkit.BukkitBedEnter;
import nl.lolmewn.stats.stats.bukkit.BukkitBlockBreak;
import nl.lolmewn.stats.stats.bukkit.BukkitBlockPlace;
import nl.lolmewn.stats.stats.bukkit.BukkitBucketEmpty;
import nl.lolmewn.stats.stats.bukkit.BukkitBucketFill;
import nl.lolmewn.stats.stats.bukkit.BukkitCommandsDone;
import nl.lolmewn.stats.stats.bukkit.BukkitDamageTaken;
import nl.lolmewn.stats.stats.bukkit.BukkitEggsThrown;
import nl.lolmewn.stats.stats.bukkit.BukkitFishCaught;
import nl.lolmewn.stats.stats.bukkit.BukkitItemsCrafted;
import nl.lolmewn.stats.stats.bukkit.BukkitItemsDropped;
import nl.lolmewn.stats.stats.bukkit.BukkitItemsPickedUp;
import nl.lolmewn.stats.stats.bukkit.BukkitKill;
import nl.lolmewn.stats.stats.bukkit.BukkitLastJoin;
import nl.lolmewn.stats.stats.bukkit.BukkitLastLeave;
import nl.lolmewn.stats.stats.bukkit.BukkitMove;
import nl.lolmewn.stats.stats.bukkit.BukkitOmnomnom;
import nl.lolmewn.stats.stats.bukkit.BukkitPVP;
import nl.lolmewn.stats.stats.bukkit.BukkitPlaytime;
import nl.lolmewn.stats.stats.bukkit.BukkitShear;
import nl.lolmewn.stats.stats.bukkit.BukkitTeleports;
import nl.lolmewn.stats.stats.bukkit.BukkitTimesKicked;
import nl.lolmewn.stats.stats.bukkit.BukkitToolsBroken;
import nl.lolmewn.stats.stats.bukkit.BukkitTrades;
import nl.lolmewn.stats.stats.bukkit.BukkitWordsSaid;
import nl.lolmewn.stats.stats.bukkit.BukkitWorldChange;
import nl.lolmewn.stats.stats.bukkit.BukkitXpGained;
import nl.lolmewn.stats.storage.FlatfileStorageEngine;
import nl.lolmewn.stats.storage.StorageEngineManager;
import nl.lolmewn.stats.user.StatsUserManager;
import nl.lolmewn.stats.util.Timings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Lolmewn
 */
public class BukkitMain extends JavaPlugin implements Main {

    private StatsAPI api;
    private StatManager statManager;
    private StatsUserManager userManager;
    private final StorageEngineManager storageManager = new StorageEngineManager();

    @Override
    public void onLoad() {
        this.checkFiles();
        this.statManager = new DefaultStatManager();
        this.loadStats();
        try {
            new Messages(new BukkitMessages(this), new BukkitPainter());
        } catch (IOException ex) {
            Logger.getLogger(BukkitMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        Timings.setEnabled(this.getConfig().getBoolean("debug", false));
    }

    @Override
    public void onEnable() {
        this.checkConversionNeeded();
        try {
            this.loadStorageEngines();
            this.scheduleUserManagerLoading();
            this.scheduleDataSaver();
        } catch (StorageException ex) {
            Logger.getLogger(BukkitMain.class.getName()).log(Level.SEVERE, null, ex);
            this.getLogger().severe("The above error is preventing Stats from booting. Please fix the error and restart the server.");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        this.getServer().getPluginManager().registerEvents(new Events(this), this);
        this.getCommand("stats").setExecutor(new StatsCommand(this));
        this.startStats();
        this.registerListeners();
        this.registerAPI();
    }

    @Override
    public void onDisable() {
        if (this.userManager != null) {
            for (StatsHolder holder : this.userManager.getUsers()) {
                try {
                    this.userManager.saveUser(holder.getUuid());
                } catch (Exception ex) {
                    Logger.getLogger(BukkitMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public StatManager getStatManager() {
        return statManager;
    }

    @Override
    public StatsUserManager getUserManager() {
        return userManager;
    }

    @Override
    public StorageEngineManager getStorageEngineManager() {
        return storageManager;
    }

    @Override
    public String getName(UUID player) {
        return this.getServer().getOfflinePlayer(player).getName();
    }

    private void checkFiles() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        File mysql = new File(this.getDataFolder(), "mysql.yml");
        if (!mysql.exists()) {
            this.saveResource("mysql.yml", true);
        }
    }

    private void loadStats() {
        this.statManager.addStat(new BukkitArrows(this));
        this.statManager.addStat(new BukkitBedEnter(this));
        this.statManager.addStat(new BukkitBlockBreak(this));
        this.statManager.addStat(new BukkitBlockPlace(this));
        this.statManager.addStat(new BukkitBucketEmpty(this));
        this.statManager.addStat(new BukkitBucketFill(this));
        this.statManager.addStat(new BukkitCommandsDone(this));
        this.statManager.addStat(new BukkitDamageTaken(this));
        this.statManager.addStat(new BukkitEggsThrown(this));
        this.statManager.addStat(new BukkitFishCaught(this));
        this.statManager.addStat(new BukkitItemsCrafted(this));
        this.statManager.addStat(new BukkitItemsDropped(this));
        this.statManager.addStat(new BukkitItemsPickedUp(this));
        this.statManager.addStat(new BukkitKill(this));
        this.statManager.addStat(new BukkitLastJoin(this));
        this.statManager.addStat(new BukkitLastLeave(this));
        this.statManager.addStat(new BukkitMove(this));
        this.statManager.addStat(new BukkitOmnomnom(this));
        this.statManager.addStat(new BukkitPVP(this));
        this.statManager.addStat(new BukkitPlaytime(this));
        this.statManager.addStat(new BukkitShear(this));
        this.statManager.addStat(new BukkitTeleports(this));
        this.statManager.addStat(new BukkitTimesKicked(this));
        this.statManager.addStat(new BukkitToolsBroken(this));
        this.statManager.addStat(new BukkitTrades(this));
        this.statManager.addStat(new BukkitWordsSaid(this));
        this.statManager.addStat(new BukkitWorldChange(this));
        this.statManager.addStat(new BukkitXpGained(this));
    }

    private void registerListeners() {
        for (Stat stat : this.statManager.getStats()) {
            if (stat instanceof Listener) {
                this.getServer().getPluginManager().registerEvents((Listener) stat, this);
            }
        }
    }

    private void loadUserManager() throws StorageException {
        if (userManager != null) {
            getLogger().info("User manager already started, not starting another");
            return;
        }
        StorageEngine engine;
        switch (this.getConfig().getString("storage", "mysql").toLowerCase()) {
            case "flatfile":
            case "flat":
            case "file":
                engine = this.getStorageEngineManager().getStorageEngine("flatfile");
                break;
            default:
                if (this.getStorageEngineManager().hasStorageEngine(this.getConfig().getString("storage").toLowerCase())) {
                    engine = this.getStorageEngineManager().getStorageEngine(this.getConfig().getString("storage").toLowerCase());
                    break;
                }
                this.getLogger().warning("Warning: No known storage type was selected in the config - defaulting to mysql.");
            // falling through
            case "mysql":
                engine = this.getStorageEngineManager().getStorageEngine("mysql");
        }
        this.userManager = new StatsUserManager(
                this,
                engine
        );
        engine.enable();
    }

    private void scheduleDataSaver() {
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {

            @Override
            public void run() {
                Timings.startTiming("user-saving", System.nanoTime());
                synchronized (userManager) {
                    for (StatsHolder holder : userManager.getUsers()) {
                        userManager.merge(holder.getUuid());
                        try {
                            userManager.saveUser(holder.getUuid());
                        } catch (StorageException ex) {
                            Logger.getLogger(BukkitMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                debug("Saving users took " + Timings.finishTimings("user-saving", System.nanoTime()) + "ns");
            }
        }, 200L, 200L);
    }

    public void debug(String message) {
        if (this.getConfig().getBoolean("debug", false)) {
            this.getServer().getConsoleSender().sendMessage("[Debug] " + message);
        }
    }

    private void registerAPI() {
        this.api = new StatsAPI(this);
        this.getServer().getServicesManager().register(StatsAPI.class, api, this, ServicePriority.Normal);
    }

    private void checkConversionNeeded() {
        File file = new File(this.getDataFolder(), "config.yml");
        if (!file.exists()) {
            // nop
            return;
        }
        if (getConfig().contains("version")) {
            new Stats2Converter(this);
        }
    }

    private void startStats() {
        for (Stat stat : this.getStatManager().getStats()) {
            stat.setEnabled(true); // TODO check in stat config if it should be enabled
        }
    }

    private void loadStorageEngines() throws StorageException {
        /**
         * Flatfile
         */
        getStorageEngineManager().addStorageEngine("flatfile", new FlatfileStorageEngine(new File(
                this.getDataFolder(),
                "users/"
        ), statManager));

        /**
         * MySQL
         */
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "mysql.yml"));
        final MySQLStorage storage = new MySQLStorage(
                this,
                new MySQLConfig()
                .setDatabase(conf.getString("database"))
                .setHost(conf.getString("host"))
                .setPassword(conf.getString("pass"))
                .setPort(conf.getInt("port", 3306))
                .setPrefix(conf.getString("prefix"))
                .setUsername(conf.getString("user"))
        );
        getStorageEngineManager().addStorageEngine("mysql", storage);
    }

    @Override
    public void scheduleTask(Runnable runnable, int ticks) {
        this.getServer().getScheduler().runTaskLater(this, runnable, ticks);
    }

    private void scheduleUserManagerLoading() {
        this.getServer().getScheduler().runTask(this, new Runnable() {

            @Override
            public void run() {
                try {
                    loadUserManager();
                } catch (StorageException ex) {
                    Logger.getLogger(BukkitMain.class.getName()).log(Level.SEVERE, null, ex);
                    getLogger().severe("The error above means the user manager failed to start");
                    getLogger().severe("The plugin cannot function without it, so it'll disable now");
                    getLogger().severe("Please fix the issue before starting the plugin again");
                    getServer().getPluginManager().disablePlugin(BukkitMain.this);
                }
            }
        });
    }
}
