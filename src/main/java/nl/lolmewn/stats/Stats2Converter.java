package nl.lolmewn.stats;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.mysql.MySQLConfig;
import nl.lolmewn.stats.mysql.MySQLStorage;
import nl.lolmewn.stats.user.StatsStatHolder;
import nl.lolmewn.stats.util.UUIDFetcher;
import nl.lolmewn.stats.util.Util;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn
 */
public class Stats2Converter {

    private final BukkitMain plugin;
    private final HashMap<String, Stat> playerStatsLookup = new HashMap<>();

    public Stats2Converter(BukkitMain plugin) {
        this.plugin = plugin;
        start();
    }

    private void start() {
        playerStatsLookup.put("arrows", Util.findStat(plugin.getStatManager(), "Arrows"));
        playerStatsLookup.put("bedenter", Util.findStat(plugin.getStatManager(), "Beds entered"));
        playerStatsLookup.put("bucketempty", Util.findStat(plugin.getStatManager(), "Buckets emptied"));
        playerStatsLookup.put("bucketfill", Util.findStat(plugin.getStatManager(), "Buckets filled"));
        playerStatsLookup.put("commandsdone", Util.findStat(plugin.getStatManager(), "Commands done"));
        playerStatsLookup.put("damagetaken", Util.findStat(plugin.getStatManager(), "Damage taken"));
        playerStatsLookup.put("arrows", Util.findStat(plugin.getStatManager(), "Arrows"));
        playerStatsLookup.put("arrows", Util.findStat(plugin.getStatManager(), "Arrows"));
        playerStatsLookup.put("arrows", Util.findStat(plugin.getStatManager(), "Arrows"));
        playerStatsLookup.put("arrows", Util.findStat(plugin.getStatManager(), "Arrows"));
        playerStatsLookup.put("arrows", Util.findStat(plugin.getStatManager(), "Arrows"));
        playerStatsLookup.put("arrows", Util.findStat(plugin.getStatManager(), "Arrows"));
        
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
        plugin.getLogger().info("Moved old config to config_old.yml...");
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
        plugin.getLogger().info("Moving MySQL info over to the new config file (mysql.yml)...");
        try {
            mysqlConf.save(new File(plugin.getDataFolder(), "mysql.yml"));
        } catch (IOException ex) {
            Logger.getLogger(Stats2Converter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        plugin.getLogger().info("Config options moving complete.");
        plugin.getLogger().info("Do check out the new options in config.yml!");
        return true;
    }

    private void convertDatabase() {
        plugin.getLogger().info("Converting all user data...");
        try {
            plugin.getLogger().info("Launching new MySQL Storage Engine with converted DB data...");
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
            

            Connection con = storage.getConnection();
            ResultSet set = con.createStatement().executeQuery("SELECT * FROM " + conf.getString("prefix") + "players");
            
            HashMap<Integer, String> needsLookup = new HashMap<>();
            HashMap<Integer, StatsStatHolder> users = new HashMap<>();
            
            while (set.next()) {
                if (set.getString("uuid") == null) {
                    needsLookup.put(set.getInt("player_id"), set.getString(set.getString("name")));
                } else {
                    users.put(
                            set.getInt("player_id"),
                            new StatsStatHolder(
                                    UUID.fromString(set.getString("uuid")),
                                    set.getString("name")
                            )
                    );
                }
            }
            UUIDFetcher fetcher = new UUIDFetcher(new ArrayList<>(needsLookup.values()));
            Map<String, UUID> uuids = fetcher.call();
            for (Entry<Integer, String> lookedUp : needsLookup.entrySet()) {
                users.put(lookedUp.getKey(), new StatsStatHolder(uuids.get(lookedUp.getValue()), lookedUp.getValue()));
            }
            
            for(StatsStatHolder holder : users.values()){
                convertUser(holder, con);
            }
           
            // rename all old tables to prefix_old_name
            
            storage.generateTables();
            for(StatsStatHolder holder : users.values()){
                storage.save(holder);
            }
        } catch (StorageException | SQLException ex) {
            Logger.getLogger(Stats2Converter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Stats2Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void convertUser(StatsStatHolder holder, Connection con) throws SQLException {
        
    }

}
