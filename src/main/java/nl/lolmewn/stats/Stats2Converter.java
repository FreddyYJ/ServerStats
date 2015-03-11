package nl.lolmewn.stats;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.api.user.UserManager;
import nl.lolmewn.stats.mysql.MySQLConfig;
import nl.lolmewn.stats.mysql.MySQLStorage;
import nl.lolmewn.stats.user.StatsUserManager;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn
 */
public class Stats2Converter {

    private final BukkitMain plugin;

    public Stats2Converter(BukkitMain plugin) {
        this.plugin = plugin;
        start();
    }

    private void start() {
        plugin.getLogger().info("Old version of Stats detected - converting config & data...");
        // First things first - we need to fix the MySQL connection. 
        if (!convertConfig()) {
            plugin.getLogger().severe("Conversion of config failed - Please report the exception above to Lolmewn and see if it can be fixed.");
            return;
        }
        convertDatabase();
    }

    private boolean convertConfig() {
        // First things first - let's move the old config
        File configOld = new File(plugin.getDataFolder(), "config.yml");
        File configMoved = new File(plugin.getDataFolder(), "config_old.yml");
        try {
            Files.move(configOld, configMoved);
        } catch (IOException ex) {
            Logger.getLogger(Stats2Converter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(configMoved);
        plugin.saveDefaultConfig();
        plugin.saveResource("mysql.yml", true); // old version only worked with mysql
        YamlConfiguration mysqlConf = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "mysql.yml"));
        mysqlConf.set("host", oldConfig.getString("MySQL-Host"));
        mysqlConf.set("user", oldConfig.getString("MySQL-User"));
        mysqlConf.set("pass", oldConfig.getString("MySQL-Pass"));
        mysqlConf.set("port", oldConfig.getString("MySQL-Port"));
        mysqlConf.set("database", oldConfig.getString("MySQL-Database"));
        mysqlConf.set("prefix", oldConfig.getString("MySQL-Prefix"));
        try {
            mysqlConf.save(new File(plugin.getDataFolder(), "mysql.yml"));
        } catch (IOException ex) {
            Logger.getLogger(Stats2Converter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private void convertDatabase() {
        try {
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "mysql.yml"));
            final MySQLStorage storage = new MySQLStorage(
                    plugin,
                    new MySQLConfig()
                    .setDatabase(conf.getString("database"))
                    .setHost(conf.getString("host"))
                    .setPassword(conf.getString("pass"))
                    .setPort(conf.getInt("port", 3306))
                    .setPrefix(conf.getString("prefix"))
                    .setUsername(conf.getString("user"))
            );
            UserManager userManager = new StatsUserManager(plugin, storage);
        } catch (StorageException ex) {
            Logger.getLogger(Stats2Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
