package nl.lolmewn.stats.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
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
import nl.lolmewn.stats.mysql.api.MySQLColumn;
import nl.lolmewn.stats.mysql.api.MySQLTable;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.DefaultStat;
import nl.lolmewn.stats.user.MySQLStatHolder;
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

    public void addTable(MySQLTable table) {
        this.tables.put(table.getName(), table);
    }

    @Override
    public MySQLStatHolder load(UUID userUuid, StatManager statManager) throws StorageException {
        plugin.debug("Loading data for " + userUuid + "...");
        MySQLStatHolder holder = new MySQLStatHolder(userUuid, plugin.getName(userUuid));
        String table = null;
        try (Connection con = source.getConnection()) {
            int i = 0;
            while (isLocked(con, userUuid) && i < 50) {
                try {
                    Thread.sleep(100);
                    if (i++ % 10 == 0) {
                        plugin.debug("User still locked, waiting...");
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(MySQLStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (i == 50) {
                plugin.debug("User was still locked, disregarding lock as it has been over 5s and I don't like waiting.");
            }
            long start = System.currentTimeMillis();
            for (Stat stat : statManager.getStats()) {
                plugin.debug("Loading stat data for " + stat.getName() + "...");
                table = prefix + formatStatName(stat.getName());
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
                    plugin.debug("Adding entry using params " + params + ", value=" + set.getDouble("value") + "...");
                    holder.addEntryLoaded(stat, entry);
                }
            }
            plugin.debug("Took " + (System.currentTimeMillis() - start) + "ms");
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Unknown column")) {
                System.out.println("Please note: Stats encountered an error while trying to load user " + userUuid.toString());
                System.out.println("It seems a column could not be found in the database; this is likely caused by the faulty conversion of the database from Stats 2 to Stats 3.");
                System.out.println("For now, you can either go back to Stats 2 (how to on the DBO page), wait until this error gets fixed by the developer or manually delete the table.");
                System.out.println("Full error below!");
            }
            System.out.println("The table causing the error: " + table);
            throw new StorageException("Something went wrong while loading the user!", ex);
        }
        holder.setTemp(false);
        return holder;
    }

    public boolean isLocked(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("SELECT * FROM " + prefix + "locks WHERE uuid=?")) {
            st.setString(1, uuid.toString());
            return st.executeQuery().next();
        }
    }

    public void lock(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("INSERT INTO " + prefix + "locks (uuid) VALUES (?)")) {
            st.setString(1, uuid.toString());
            st.execute();
        }
    }

    public void unlock(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("DELETE FROM " + prefix + "locks WHERE uuid=?")) {
            st.setString(1, uuid.toString());
            st.execute();
        }
    }

    @Override
    public void save(StatsHolder user) throws StorageException {
        if (!(user instanceof MySQLStatHolder)) {
            // temp user for StatsUserManager
            return;
        }
        MySQLStatHolder holder = (MySQLStatHolder) user;
        if (holder.isTemp()) {
            return;
        }
        String table = null;
        try (Connection con = source.getConnection()) {
            // First, make sure there's a column in the Stats_players table to maintain integrity of the table
            String playersQuery = "INSERT INTO " + prefix + "players (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
            PreparedStatement playersPS = con.prepareStatement(playersQuery);
            playersPS.setString(1, holder.getUuid().toString());
            playersPS.setString(2, holder.getName());
            playersPS.execute();

            for (Iterator<Stat> statIterator = holder.getStats().iterator(); statIterator.hasNext();) {
                Stat stat = statIterator.next();
                plugin.debug("Saving stat data for " + stat.getName() + "...");
                table = prefix + formatStatName(stat.getName());
                // first, delete the values that were locally deleted. If new values were added, they will be INSERTed anyway
                for (StatEntry deleted : holder.getRemovedEntries()) {
                    StringBuilder sb = new StringBuilder("DELETE FROM ");
                    sb.append(table).append(" WHERE uuid=? ");
                    stat.getDataTypes().keySet().stream().map((metadataName) -> {
                        sb.append("AND ").append(metadataName.replace(" ", ""));
                        return metadataName;
                    }).forEach((_item) -> {
                        sb.append("=? ");
                    });
                    PreparedStatement deletePS = con.prepareStatement(sb.toString());
                    deletePS.setString(1, holder.getUuid().toString());
                    int idx = 2;
                    for (String metadataName : stat.getDataTypes().keySet()) {
                        if (stat.getDataTypes().get(metadataName) == DataType.TIMESTAMP) {
                            deletePS.setObject(idx++, new Timestamp((long) deleted.getMetadata().get(metadataName)));
                        } else {
                            deletePS.setObject(idx++, deleted.getMetadata().get(metadataName));
                        }
                    }
                    deletePS.execute();
                }
                holder.getRemovedEntries().clear();
                Queue<StatEntry> save = holder.getAdditions().get(stat);
                if (save == null || save.isEmpty()) {
                    continue;
                }
                StatEntry entry;
                while ((entry = save.poll()) != null) {
                    plugin.debug("Saving entry using params " + entry.getMetadata() + ", value=" + entry.getValue() + "...");
                    StringBuilder update = new StringBuilder("UPDATE ");
                    update.append(table);
                    update.append(" SET value=");
                    if (stat instanceof DefaultStat && ((DefaultStat) stat).isSummable()) {
                        update.append("value+");
                    }
                    update.append("? WHERE uuid=? ");
                    stat.getDataTypes().keySet().stream().map((metadataName) -> {
                        update.append("AND ").append(metadataName.replace(" ", ""));
                        return metadataName;
                    }).forEach((_item) -> {
                        update.append("=? ");
                    });
                    PreparedStatement updatePS = con.prepareStatement(update.toString());
                    updatePS.setDouble(1, entry.getValue());
                    updatePS.setString(2, holder.getUuid().toString());
                    int idx = 3;
                    for (String metadataName : stat.getDataTypes().keySet()) {
                        if (stat.getDataTypes().get(metadataName) == DataType.TIMESTAMP) {
                            updatePS.setObject(idx++, new Timestamp((long) entry.getMetadata().get(metadataName)));
                        } else {
                            updatePS.setObject(idx++, entry.getMetadata().get(metadataName));
                        }
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
                        stat.getDataTypes().keySet().stream().forEach((ignored) -> {
                            insert.append(",? ");
                        });
                        insert.append(")");
                        PreparedStatement insertPS = con.prepareStatement(insert.toString());
                        insertPS.setString(1, holder.getUuid().toString());
                        insertPS.setDouble(2, entry.getValue());
                        idx = 3;
                        for (String metadataName : stat.getDataTypes().keySet()) {
                            if (stat.getDataTypes().get(metadataName) == DataType.TIMESTAMP) {
                                insertPS.setObject(idx++, new Timestamp((long) entry.getMetadata().get(metadataName)));
                            } else {
                                insertPS.setObject(idx++, entry.getMetadata().get(metadataName));
                            }
                        }
                        insertPS.execute();
                    }
                }
            }
            unlock(con, user.getUuid()); // if they're never locked, not my problem!
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Unknown column")) {
                System.out.println("Please note: Stats encountered an error while trying to save user " + user.getUuid().toString());
                System.out.println("It seems a column could not be found in the database; this is likely caused by the faulty conversion of the database from Stats 2 to Stats 3.");
                System.out.println("For now, you can either go back to Stats 2 (how to on the DBO page), wait until this error gets fixed by the developer or manually delete the table.");
                System.out.println("Full error below!");
            }
            System.out.println("The table causing the error: " + table);
            throw new StorageException("Something went wrong while saving the user!", ex);
        }
    }

    public void generateTables() throws StorageException {
        MySQLTable playersTable = new MySQLTable(prefix + "players");
        playersTable.addColumn("uuid", DataType.STRING).addAttributes(MySQLAttribute.PRIMARY_KEY, MySQLAttribute.NOT_NULL, MySQLAttribute.UNIQUE);
        playersTable.addColumn("name", DataType.STRING).addAttribute(MySQLAttribute.NOT_NULL);
        this.tables.put(playersTable.getName(), playersTable);
        MySQLTable locks = new MySQLTable(prefix + "locks");
        locks.addColumn("uuid", DataType.STRING)
                .addAttributes(MySQLAttribute.NOT_NULL, MySQLAttribute.PRIMARY_KEY, MySQLAttribute.UNIQUE)
                .references(playersTable, playersTable.getColumn("uuid"));
        this.tables.put(locks.getName(), locks);
        try (Connection con = this.source.getConnection()) {
            con.createStatement().execute(playersTable.generateCreateQuery());
            con.createStatement().execute(locks.generateCreateQuery());
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
                stat.getDataTypes().entrySet().stream().forEach((entry) -> {
                    table.addColumn(entry.getKey(), entry.getValue());
                });
                String createQuery = table.generateCreateQuery();
                con.createStatement().execute(createQuery);
            }
        } catch (SQLException ex) {
            throw new StorageException("Failed to generate tables for stats", ex);
        }
    }

    public void checkTables() throws StorageException {
        try (Connection con = this.source.getConnection()) {
            for (MySQLTable table : this.tables.values()) {
                ResultSet set = con.createStatement().executeQuery("SELECT * FROM " + table.getName() + " LIMIT 1");
                ResultSetMetaData rsmd = set.getMetaData();
                for (MySQLColumn column : table.getColumns()) {
                    if (hasColumnName(rsmd, column.getName())) {
                        continue; // column exists, all good.
                    }
                    // Column does not exist, let's make it
                    plugin.info("[Stats] Found a column that doesn't exist yet in the table: " + column.getName());
                    plugin.info("[Stats] Don't worry, I got you covered. Generating the column now!");
                    Statement st = con.createStatement();
                    StringBuilder sb = new StringBuilder();
                    sb.append("ALTER TABLE ")
                            .append(table.getName())
                            .append(" ADD COLUMN ")
                            .append(table.getName())
                            .append(" ")
                            .append(column.getMySQLType()).append(" ");
                    column.getAttribues().stream().forEach((attr) -> {
                        sb.append(attr.getMySQLEquiv()).append(" ");
                    });
                    if (column.hasDefault()) {
                        sb.append("DEFAULT ").append(column.getDefault());
                    }
                    st.executeUpdate(sb.toString());
                }

                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    for (MySQLColumn column : table.getColumns()) {
                        if (!hasColumnName(rsmd, column.getName())) {
                            plugin.info("[Stats] Found a column in table " + table.getName() + " that is not used: " + rsmd.getColumnName(i));
                            plugin.info("[Stats] You can safely remove this column if you want, or leave it be.");
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new StorageException("Failed to check tables for stats", ex);
        }
    }

    private boolean hasColumnName(ResultSetMetaData rsmd, String name) throws SQLException {
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            if (rsmd.getColumnName(i).equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
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
                    try (PreparedStatement st = con.prepareStatement("DELETE FROM " + table.getName() + " WHERE uuid=?")) {
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
        this.plugin.scheduleTask(() -> {
            try {
                generateTables();
                checkTables();
            } catch (StorageException ex) {
                Logger.getLogger(MySQLStorage.class.getName()).log(Level.SEVERE, null, ex);
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
