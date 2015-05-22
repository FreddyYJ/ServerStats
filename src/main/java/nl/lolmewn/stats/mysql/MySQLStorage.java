package nl.lolmewn.stats.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;
import nl.lolmewn.stats.api.storage.StorageEngine;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.mysql.api.MySQLAttribute;
import nl.lolmewn.stats.mysql.api.MySQLTable;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.user.StatsStatHolder;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author Lolmewn
 */
public class MySQLStorage implements StorageEngine {

    private final Main plugin;
    private final MySQLConfig config;
    private BasicDataSource source;
    private String prefix;
    private Map<String, MySQLTable> tables;

    public MySQLStorage(Main main, MySQLConfig config) throws StorageException {
        this.plugin = main;
        this.config = config;
    }

    @Override
    public StatsHolder load(UUID userUuid, StatManager statManager) throws StorageException {
        System.out.println("Loading data for " + userUuid + "...");
        StatsStatHolder holder = new StatsStatHolder(userUuid, plugin.getName(userUuid));
        try (Connection con = source.getConnection()) {
            for (Stat stat : statManager.getStats()) {
                System.out.println("Loading stat data for " + stat.getName() + "...");
                String table = prefix + formatStatName(stat.getName());
                PreparedStatement st = con.prepareStatement("SELECT * FROM " + table + " WHERE uuid=?");
                st.setString(1, userUuid.toString());
                ResultSet set = st.executeQuery();
                while (set.next()) {
                    // use the parameters too 
                    List<MetadataPair> params = new ArrayList<>();
                    for (Entry<String, DataType> param : stat.getDataTypes().entrySet()) {
                        Object value;
                        switch (param.getValue()) {
                            case BOOLEAN:
                                value = set.getBoolean(param.getKey());
                                break;
                            case BYTE_ARRAY:
                                value = set.getBytes(param.getKey());
                                break;
                            case DOUBLE:
                                value = set.getDouble(param.getKey());
                                break;
                            case FLOAT:
                                value = set.getFloat(param.getKey());
                                break;
                            case INTEGER:
                                value = set.getInt(param.getKey());
                                break;
                            case LONG:
                                value = set.getLong(param.getKey());
                                break;
                            case STRING:
                                value = set.getString(param.getKey());
                                break;
                            case TIMESTAMP:
                                value = set.getTimestamp(param.getKey()).getTime();
                                break;
                            default:
                                Logger.getLogger(MySQLStorage.class.getName()).log(Level.SEVERE, "Unknown data type " + param.getValue() + ", just trying something");
                                value = set.getObject(param.getKey());
                        }
                        params.add(new MetadataPair(param.getKey(), value));
                    }
                    StatEntry entry = new DefaultStatEntry(set.getDouble("value"), params);
                    System.out.println("Adding entry using params " + params + ", value=" + set.getDouble("value") + "...");
                    holder.addEntry(stat, entry);
                }
            }
        } catch (SQLException ex) {
            throw new StorageException("Something went wrong while loading the user!", ex);
        }
        holder.setTemp(false);
        return holder;
    }

    @Override
    public void save(StatsHolder user) throws StorageException {
        StatsStatHolder holder = (StatsStatHolder) user;
        if (holder.isTemp()) {
            return;
        }
        try (Connection con = source.getConnection()) {
            // First, make sure there's a column in the Stats_players table to maintain integrity of the table
            String playersQuery = "INSERT INTO " + prefix + "players (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
            PreparedStatement playersPS = con.prepareStatement(playersQuery);
            playersPS.setString(1, holder.getUuid().toString());
            playersPS.setString(2, holder.getName());
            playersPS.execute();

            for (Stat stat : holder.getStats()) {
                String table = prefix + formatStatName(stat.getName());
                // TODO improve saving method by updating the value
                for (StatEntry entry : holder.getStats(stat)) {
                    StringBuilder update = new StringBuilder("UPDATE ");
                    update.append(table);
                    update.append(" SET value=? WHERE uuid=? ");
                    for (String metadataName : stat.getDataTypes().keySet()) {
                        update.append("AND ").append(metadataName.replace(" ", ""));
                        update.append("=? ");
                    }
                    PreparedStatement updatePS = con.prepareStatement(update.toString());
                    updatePS.setDouble(1, entry.getValue());
                    updatePS.setString(2, holder.getUuid().toString());
                    int idx = 3;
                    for (String metadataName : stat.getDataTypes().keySet()) {
                        updatePS.setObject(idx++, entry.getMetadata().get(metadataName));
                    }
                    if (!updatePS.execute() && updatePS.getUpdateCount() == 0) {
                        //Need to insert
                        StringBuilder insert = new StringBuilder("INSERT INTO ");
                        insert.append(table);
                        insert.append(" (uuid, value");
                        for (String metadataName : entry.getMetadata().keySet()) {
                            insert.append(", ").append(metadataName.replace(" ", ""));
                        }
                        insert.append(") VALUES (?, ?");
                        for (String metadataName : stat.getDataTypes().keySet()) {
                            insert.append(",? ");
                        }
                        insert.append(")");
                        PreparedStatement insertPS = con.prepareStatement(insert.toString());
                        insertPS.setString(1, holder.getUuid().toString());
                        insertPS.setDouble(2, entry.getValue());
                        idx = 3;
                        for (String metadataName : stat.getDataTypes().keySet()) {
                            insertPS.setObject(idx++, entry.getMetadata().get(metadataName));
                        }
                        insertPS.execute();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new StorageException("Something went wrong while saving the user!", ex);
        }
    }

    public void generateTables() throws StorageException {
        MySQLTable playersTable = new MySQLTable(prefix + "players");
        playersTable.addColumn("uuid", DataType.STRING).addAttributes(MySQLAttribute.PRIMARY_KEY, MySQLAttribute.NOT_NULL, MySQLAttribute.UNIQUE);
        playersTable.addColumn("name", DataType.STRING).addAttribute(MySQLAttribute.NOT_NULL);
        try (Connection con = this.source.getConnection()) {
            con.createStatement().execute(playersTable.generateCreateQuery());
            for (Stat stat : plugin.getStatManager().getStats()) {
                String tableName = prefix + formatStatName(stat.getName());
                MySQLTable table = new MySQLTable(tableName);
                this.tables.put(tableName, table);
                table.addColumn("id", DataType.LONG).addAttributes(
                        MySQLAttribute.PRIMARY_KEY,
                        MySQLAttribute.AUTO_INCREMENT,
                        MySQLAttribute.NOT_NULL,
                        MySQLAttribute.UNIQUE
                );
                table.addColumn("uuid", DataType.STRING).addAttributes(
                        MySQLAttribute.NOT_NULL
                ).references(playersTable, playersTable.getColumn("uuid"));
                table.addColumn("value", DataType.DOUBLE).addAttribute(MySQLAttribute.NOT_NULL);
                for (Entry<String, DataType> entry : stat.getDataTypes().entrySet()) {
                    table.addColumn(entry.getKey(), entry.getValue());
                }
                String createQuery = table.generateCreateQuery();
                con.createStatement().execute(createQuery);
            }
        } catch (SQLException ex) {
            throw new StorageException("Failed to generate tables for stats", ex);
        }
    }

    public String formatStatName(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    @Override
    public void delete(StatsHolder user) throws StorageException {
        try {
            try (Connection con = this.source.getConnection()) {
                for (MySQLTable table : this.tables.values()) {
                    try (PreparedStatement st = con.prepareStatement("DELETE FROM " + prefix + table.getName() + " WHERE uuid=?")) {
                        st.setString(1, user.getUuid().toString());
                        st.execute();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new StorageException("Something went wrong while trying to delete user " + user.getUuid().toString(), ex);
        }
        // idea for improvement: iterate over the user's stats instead of over every table
    }

    @Override
    public void enable() throws StorageException {
        this.source = new BasicDataSource();
        this.source.setDriverClassName("com.mysql.jdbc.Driver");
        this.source.setUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + "?zeroDateTimeBehavior=convertToNull");
        this.source.setUsername(config.getUsername());
        this.source.setPassword(config.getPassword());
        this.prefix = config.getPrefix();
        this.tables = new HashMap<>();
        this.plugin.scheduleTask(new Runnable() {

            @Override
            public void run() {
                try {
                    generateTables();
                } catch (StorageException ex) {
                    Logger.getLogger(MySQLStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 1);
    }

    @Override
    public void disable() throws StorageException {
        try {
            this.source.close();
        } catch (SQLException ex) {
            throw new StorageException("Exception while disabling the StorageEngine", ex);
        }
    }

}
