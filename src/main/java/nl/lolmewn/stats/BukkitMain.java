package nl.lolmewn.stats;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.api.user.UserManager;
import nl.lolmewn.stats.mysql.MySQLConfig;
import nl.lolmewn.stats.mysql.MySQLStorage;
import nl.lolmewn.stats.stats.bukkit.BukkitPVP;
import nl.lolmewn.stats.stats.bukkit.BukkitPlaytime;
import nl.lolmewn.stats.storage.FlatfileStorageEngine;
import nl.lolmewn.stats.user.StatsUserManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Lolmewn
 */
public class BukkitMain extends JavaPlugin implements Main{

    private StatManager statManager;
    private UserManager userManager;

    @Override
    public void onLoad() {
        this.checkFiles();
        this.statManager = new DefaultStatManager();
        try {
            this.loadUserManager();
        } catch (StorageException ex) {
            Logger.getLogger(BukkitMain.class.getName()).log(Level.SEVERE, null, ex);
            this.getLogger().severe("The above error is preventing Stats from booting. Please fix the error and restart the server.");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        this.loadStats();
        this.getServer().getPluginManager().registerEvents(new Events(this), this);
    }

    @Override
    public void onDisable() {
        if(this.userManager != null){
            for(StatsHolder holder : this.userManager.getUsers()){
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
    public UserManager getUserManager() {
        return userManager;
    }

    private void checkFiles() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        File mysql = new File(this.getDataFolder(), "mysql.yml");
        if(!mysql.exists()){
            this.saveResource("mysql.yml", true);
        }
    }

    private void loadStats() {
        this.statManager.addStat(new BukkitPlaytime(this));
        this.statManager.addStat(new BukkitPVP(this));
    }

    private void loadUserManager() throws StorageException {
        switch (this.getConfig().getString("storage", "mysql").toLowerCase()) {
            case "flatfile":
            case "flat":
            case "file":
                this.userManager = new StatsUserManager(
                        this,
                        new FlatfileStorageEngine(
                                new File(
                                        this.getDataFolder(),
                                        "users/"
                                )
                        )
                );
                break;
            default:
                this.getLogger().warning("Warning: No known storage type was selected in the config - defaulting to mysql.");
            // falling through
            case "mysql":
                YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "mysql.yml"));
                this.userManager = new StatsUserManager(
                        this,
                        new MySQLStorage(
                                this,
                                new MySQLConfig()
                                .setDatabase(conf.getString("database"))
                                .setHost(conf.getString("host"))
                                .setPassword(conf.getString("pass"))
                                .setPort(conf.getInt("port", 3306))
                                .setPrefix(conf.getString("prefix"))
                                .setUsername(conf.getString("user"))
                        )
                );
        }
    }
}
