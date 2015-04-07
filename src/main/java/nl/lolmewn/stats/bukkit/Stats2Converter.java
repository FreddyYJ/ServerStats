package nl.lolmewn.stats.bukkit;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.mysql.MySQLConfig;
import nl.lolmewn.stats.mysql.MySQLStorage;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.user.StatsStatHolder;
import nl.lolmewn.stats.util.UUIDFetcher;
import nl.lolmewn.stats.util.Util;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn
 */
public class Stats2Converter {

    private final BukkitMain plugin;
    private String prefix;
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
        playerStatsLookup.put("eggsthrown", Util.findStat(plugin.getStatManager(), "Eggs thrown"));
        playerStatsLookup.put("fishcatched", Util.findStat(plugin.getStatManager(), "Fish caught"));
        playerStatsLookup.put("itemscrafted", Util.findStat(plugin.getStatManager(), "Items crafted"));
        playerStatsLookup.put("itemdrops", Util.findStat(plugin.getStatManager(), "Items dropped"));
        playerStatsLookup.put("itempickups", Util.findStat(plugin.getStatManager(), "Items picked up"));
        playerStatsLookup.put("joins", Util.findStat(plugin.getStatManager(), "Joins"));
        playerStatsLookup.put("lastjoin", Util.findStat(plugin.getStatManager(), "Last join"));
        playerStatsLookup.put("lastleave", Util.findStat(plugin.getStatManager(), "Last seen"));
        playerStatsLookup.put("money", Util.findStat(plugin.getStatManager(), "Money"));
        playerStatsLookup.put("omnomnom", Util.findStat(plugin.getStatManager(), "Omnomnom"));
        playerStatsLookup.put("pvpstreak", Util.findStat(plugin.getStatManager(), "PVP streak"));
        playerStatsLookup.put("pvptopstreak", Util.findStat(plugin.getStatManager(), "PVP top streak"));
        playerStatsLookup.put("playtime", Util.findStat(plugin.getStatManager(), "Playtime"));
        playerStatsLookup.put("shear", Util.findStat(plugin.getStatManager(), "Shears"));
        playerStatsLookup.put("teleports", Util.findStat(plugin.getStatManager(), "Teleports"));
        playerStatsLookup.put("timeskicked", Util.findStat(plugin.getStatManager(), "Times kicked"));
        playerStatsLookup.put("toolsbroken", Util.findStat(plugin.getStatManager(), "Tools broken"));
        playerStatsLookup.put("trades", Util.findStat(plugin.getStatManager(), "Trades"));
        playerStatsLookup.put("votes", Util.findStat(plugin.getStatManager(), "Votes"));
        playerStatsLookup.put("wordssaid", Util.findStat(plugin.getStatManager(), "Words said"));
        playerStatsLookup.put("worldchange", Util.findStat(plugin.getStatManager(), "Times changed world"));
        playerStatsLookup.put("xpgained", Util.findStat(plugin.getStatManager(), "XP gained"));

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
        prefix = mysqlConf.getString("prefix");
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
            plugin.getLogger().info("Launching new MySQL Storage Engine with converted DB config data...");
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
            plugin.getLogger().info("Fetching the UUID of " + needsLookup.size() + " players...");
            UUIDFetcher fetcher = new UUIDFetcher(new ArrayList<>(needsLookup.values()));
            Map<String, UUID> uuids = fetcher.call();
            for (Entry<Integer, String> lookedUp : needsLookup.entrySet()) {
                users.put(lookedUp.getKey(), new StatsStatHolder(uuids.get(lookedUp.getValue()), lookedUp.getValue()));
            }

            plugin.getLogger().info("Converting " + users.size() + " players to new database format...");
            int done = 0;
            for (Entry<Integer, StatsStatHolder> entry : users.entrySet()) {
                convertUser(entry.getValue(), entry.getKey(), con);
                if (done++ % 100 == 0) {
                    plugin.getLogger().info("Converted " + done + "/" + users.size() + " users...");
                }
            }

            // rename all old tables to prefix_old_name
            List<String> oldTables = new ArrayList<String>() {
                {
                    this.add(prefix + "player");
                    this.add(prefix + "kill");
                    this.add(prefix + "death");
                    this.add(prefix + "move");
                    this.add(prefix + "players");
                }
            };
            Statement st = con.createStatement();
            StringBuilder builder = new StringBuilder();
            builder.append("RENAME TABLE ");
            for (Iterator<String> it = oldTables.iterator(); it.hasNext();) {
                String table = it.next();
                builder.append(table).append(" TO ").append("old_").append(table);
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            st.execute(builder.toString());
            
            
            storage.generateTables();
            for (StatsStatHolder holder : users.values()) {
                storage.save(holder);
            }
        } catch (StorageException | SQLException ex) {
            Logger.getLogger(Stats2Converter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Stats2Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void convertUser(StatsStatHolder holder, int id, Connection con) throws SQLException {

        /**
         * Stats_player data
         */
        try (PreparedStatement st = con.prepareStatement("SELECT * FROM " + prefix + "player WHERE player_id=?")) {
            st.setInt(1, id);
            ResultSet set = st.executeQuery();
            ResultSetMetaData meta = set.getMetaData();
            while (set.next()) {
                for (int i = 0; i < meta.getColumnCount(); i++) {
                    String colName = meta.getColumnName(i);
                    if (playerStatsLookup.containsKey(colName)) {
                        Stat stat = playerStatsLookup.get(colName);
                        if (stat == null) {
                            System.out.println("Wups, something went wrong while loading stat data: " + colName);
                            break;
                        }
                        holder.addEntry(stat,
                                new DefaultStatEntry(
                                        set.getDouble(colName),
                                        new MetadataPair( // if stat doesn't have world, it'll be ignored
                                                "world",
                                                set.getString("world")
                                        )
                                )
                        );
                    }
                }
            }
        }

        try (PreparedStatement st = con.prepareStatement("SELECT * FROM " + prefix + "block WHERE player_id=?")) {
            Stat bbreak = plugin.getStatManager().getStat("Blocks broken");
            Stat bplace = plugin.getStatManager().getStat("Blocks placed");
            st.setInt(1, id);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                StatEntry entry = new DefaultStatEntry(
                        set.getInt("amount"),
                        new MetadataPair("name", Material.getMaterial(set.getInt("blockID")).toString()),
                        new MetadataPair("data", set.getByte("blockData")),
                        new MetadataPair("world", set.getString("world"))
                );
                holder.addEntry(set.getBoolean("break") ? bbreak : bplace, entry);
            }
        }

        try (PreparedStatement st = con.prepareStatement("SELECT * FROM " + prefix + "death WHERE player_id=?")) {
            Stat death = plugin.getStatManager().getStat("Death");
            st.setInt(1, id);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                holder.addEntry(death, new DefaultStatEntry(
                        set.getInt("amount"),
                        new MetadataPair("world", set.getString("world")),
                        new MetadataPair("cause", set.getString("cause"))
                ));
            }
        }

        try (PreparedStatement st = con.prepareStatement("SELECT * FROM " + prefix + "move WHERE player_id=?")) {
            Stat move = plugin.getStatManager().getStat("Move");
            st.setInt(1, id);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                holder.addEntry(move, new DefaultStatEntry(
                        set.getDouble("distance"),
                        new MetadataPair("world", set.getString("world")),
                        new MetadataPair("type", set.getString("type"))
                ));
            }
        }

        try (PreparedStatement st = con.prepareStatement("SELECT * FROM " + prefix + "kill WHERE player_id=?")) {
            Stat kill = plugin.getStatManager().getStat("Kill");
            st.setInt(1, id);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                holder.addEntry(kill, new DefaultStatEntry(
                        set.getInt("amount"),
                        new MetadataPair("world", set.getString("world")),
                        new MetadataPair("entityType", set.getString("cause")),
                        new MetadataPair("weapon", "Unknown")
                ));
            }
        }
    }

}
