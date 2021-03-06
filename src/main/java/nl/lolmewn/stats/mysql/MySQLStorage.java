package nl.lolmewn.stats.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
import nl.lolmewn.stats.util.Timings;
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
    private boolean enabled = false;
    private ThreadPoolExecutor threadPool;

    public MySQLStorage(Main main, MySQLConfig config) throws StorageException {
        this.plugin = main;
        this.config = config;
    }

    public void addTable(MySQLTable table) throws SQLException {
        this.tables.put(table.getName(), table);
        if (enabled) {
            try (Connection con = getConnection()) {
                con.createStatement().execute(table.generateCreateQuery());
            }
        }
    }

    public BasicDataSource getDataSource() {
        return source;
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
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

    public void lock(Connection con, UUID uuid) {
        try (PreparedStatement st = con.prepareStatement("INSERT INTO " + prefix + "locks (uuid) VALUES (?)")) {
            st.setString(1, uuid.toString());
            st.execute();
        } catch (SQLException ex) {
            this.plugin.info("Could not lock user with UUID " + uuid.toString());
            this.plugin.info("This may not affect you in any way; just letting you know it happened.");
            this.plugin.info("The cause was: " + ex.getLocalizedMessage() + " (errno " + ex.getErrorCode() + ")");
        }
    }

    public void unlock(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("DELETE FROM " + prefix + "locks WHERE uuid=?")) {
            st.setString(1, uuid.toString());
            st.execute();
        }
    }

    public void saveUser(MySQLStatHolder holder) throws StorageException {
        String table = null;
        Timings.startTiming("saving-" + holder.getUuid(), System.nanoTime());
        try (Connection con = source.getConnection()) {
            con.setAutoCommit(false);
            // First, make sure there's a column in the Stats_players table to maintain integrity of the table
            String playersQuery = "INSERT INTO " + prefix + "players (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
            PreparedStatement playersPS = con.prepareStatement(playersQuery);
            playersPS.setString(1, holder.getUuid().toString());
            playersPS.setString(2, holder.getName());
            playersPS.execute();
            for (Entry<Stat, Collection<StatEntry>> deleted : holder.getRemovedEntries().entrySet()) {
                Stat stat = deleted.getKey();
                table = prefix + formatStatName(stat.getName());
                for (StatEntry deletedEntry : deleted.getValue()) {
                    StringBuilder sb = new StringBuilder("DELETE FROM ");
                    sb.append(table).append(" WHERE uuid=? ");
                    stat.getDataTypes().keySet().stream().map((metadataName) -> {
                        sb.append("AND ").append(metadataName.replace(" ", ""));
                        return metadataName;
                    }).forEach((_item) -> {
                        if (deletedEntry.getMetadata().containsKey(_item)) {
                            sb.append("=? ");
                        } else {
                            sb.append(" IS NULL ");
                        }
                    });
                    PreparedStatement deletePS = con.prepareStatement(sb.toString());
                    deletePS.setString(1, holder.getUuid().toString());
                    int idx = 2;
                    for (String metadataName : stat.getDataTypes().keySet()) {
                        if (!deletedEntry.getMetadata().containsKey(metadataName)) {
                            continue; // Skip for IS NULL
                        }
                        if (stat.getDataTypes().get(metadataName) == DataType.TIMESTAMP) {
                            deletePS.setObject(idx++, new Timestamp((long) deletedEntry.getMetadata().get(metadataName)));
                        } else {
                            deletePS.setObject(idx++, deletedEntry.getMetadata().get(metadataName));
                        }
                    }
                    deletePS.execute();
                }
            }
            holder.getRemovedEntries().clear();
            for (Stat stat : holder.getStats()) {
                table = prefix + formatStatName(stat.getName());
                Queue<StatEntry> save = holder.getAdditions().get(stat);
                if (save == null || save.isEmpty()) {
                    continue;
                }
                StatEntry entry;
                while ((entry = save.poll()) != null) {
                    final StatEntry currentEntry = entry;
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
                        if (currentEntry.getMetadata().containsKey(_item)) {
                            update.append("=? ");
                        } else {
                            update.append(" IS NULL ");
                        }
                    });
                    PreparedStatement updatePS = con.prepareStatement(update.toString());
                    updatePS.setDouble(1, entry.getValue());
                    updatePS.setString(2, holder.getUuid().toString());
                    int idx = 3;
                    for (String metadataName : stat.getDataTypes().keySet()) {
                        if (!currentEntry.getMetadata().containsKey(metadataName)) {
                            continue; // Skip for IS NULL
                        }
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
                        stat.getDataTypes().keySet().stream().forEach((metadataName) -> {
                            insert.append(", ").append(metadataName.replace(" ", ""));
                        });
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
            unlock(con, holder.getUuid()); // if they're never locked, not my problem!
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Unknown column")) {
                System.out.println("Please note: Stats encountered an error while trying to save user " + holder.getUuid().toString());
                System.out.println("It seems a column could not be found in the database; this is likely caused by the faulty conversion of the database from Stats 2 to Stats 3.");
                System.out.println("For now, you can either go back to Stats 2 (how to on the DBO page), wait until this error gets fixed by the developer or manually delete the table.");
                System.out.println("Full error below!");
            }
            System.out.println("The table causing the error: " + table);
            throw new StorageException("Something went wrong while saving the user!", ex);
        }
        plugin.debug("Saving user " + holder.getUuid() + " took " + (Timings.finishTimings("saving-" + holder.getUuid(), System.nanoTime()) / 1000000) + "ms");
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
        threadPool.submit(() -> {
            try {
                saveUser(holder);
            } catch (StorageException ex) {
                Logger.getLogger(MySQLStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
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
                generateTable(con, stat, playersTable);
            }
        } catch (SQLException ex) {
            throw new StorageException("Failed to generate tables for stats", ex);
        }
    }

    private void generateTable(Connection con, Stat stat, MySQLTable playersTable) throws SQLException {
        MySQLTable table = generateTable(stat);
        this.tables.put(table.getName(), table);
        table.getColumn("uuid").references(playersTable, playersTable.getColumn("uuid"));
        String createQuery = table.generateCreateQuery();
        con.createStatement().execute(createQuery);
    }

    public MySQLTable generateTable(Stat stat) {
        MySQLTable table = new MySQLTable(prefix + formatStatName(stat.getName()));
        table.addColumn("id", DataType.LONG).addAttributes(
                MySQLAttribute.PRIMARY_KEY,
                MySQLAttribute.AUTO_INCREMENT,
                MySQLAttribute.NOT_NULL,
                MySQLAttribute.UNIQUE
        );
        table.addColumn("uuid", DataType.STRING).addAttributes(MySQLAttribute.NOT_NULL);
        table.addColumn("value", DataType.DOUBLE).addAttribute(MySQLAttribute.NOT_NULL);
        stat.getDataTypes().entrySet().stream().forEach((entry) -> {
            table.addColumn(entry.getKey(), entry.getValue());
        });
        return table;
    }

    public void checkTables() throws StorageException {
        try (Connection con = this.source.getConnection()) {
            for (MySQLTable table : this.tables.values()) {
                boolean exists = con.createStatement().executeQuery("SHOW TABLES LIKE '" + table.getName() + "'").next();
                if (!exists) {
                    con.createStatement().execute(table.generateCreateQuery());
                    continue; // Freshly created, in-memory is same as database layout
                }
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
                            .append(column.getName())
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

    public String getPrefix() {
        return prefix;
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
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
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
                fixConversionError();
                checkTables();
            } catch (StorageException ex) {
                Logger.getLogger(MySQLStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }, 1);
        this.enabled = true;
    }

    @Override
    public void disable() throws StorageException {
        this.enabled = false;
        try {
            this.threadPool.shutdown();
            this.threadPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            plugin.info("Time-out occured while waiting for threads to shutdown - killing them.");
            this.threadPool.shutdownNow();
        }
        try {
            this.source.close();
        } catch (SQLException ex) {
            throw new StorageException("Exception while disabling the StorageEngine", ex);
        }
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    private void fixConversionError() throws StorageException {
        // TODO: Implement using http://stackoverflow.com/a/3836911/1122834
        try (Connection con = this.getConnection()) {
            fixConversionForDeath(con);
            fixConversionForKill(con);
        } catch (SQLException ex) {
            throw new StorageException("Could not check if conversion was done properly", ex);
        }
    }

    private void fixConversionForKill(Connection con) throws SQLException {
        Statement st = con.createStatement();
        ResultSet needsFix = st.executeQuery("SELECT * FROM " + getPrefix() + "kill WHERE (convert(entityType using latin1) COLLATE latin1_general_cs) NOT REGEXP '^[A-Z,_]+$'");
        int updated = 0;
        PreparedStatement update = con.prepareStatement("UPDATE " + getPrefix() + "kill SET value=value+? WHERE uuid=? AND weapon=? AND world=? AND (convert(entityType using latin1) COLLATE latin1_general_cs)=?");
        PreparedStatement insert = con.prepareStatement("INSERT INTO " + getPrefix() + "kill (uuid, value, weapon, world, entityType) VALUES (?, ?, ?, ?, ?)");
        boolean messageSent = false;
        while (needsFix.next()) {
            if (!messageSent) {
                messageSent = true;
                plugin.info("Fixing conversion error in the Kill table, this may take a while...");
            }
            // Try to update an already existing all-uppercase row
            update.setDouble(1, needsFix.getInt("value"));
            update.setString(2, needsFix.getString("uuid"));
            update.setString(3, needsFix.getString("weapon"));
            update.setString(4, needsFix.getString("world"));
            update.setString(5, needsFix.getString("entityType").toUpperCase()); // Here's the difference with the original query
            if (update.executeUpdate() == 0) {
                // Update didn't find row to update; insert it
                insert.setString(1, needsFix.getString("uuid"));
                insert.setDouble(2, needsFix.getInt("value"));
                insert.setString(3, needsFix.getString("weapon"));
                insert.setString(4, needsFix.getString("world"));
                insert.setString(5, needsFix.getString("entityType").toUpperCase()); // Here's the difference with the original query
                insert.execute();
            }
            updated++;
        }
        // Delete all old non-adhering rows from the database
        st.execute("DELETE FROM " + getPrefix() + "kill WHERE (convert(entityType using latin1) COLLATE latin1_general_cs) NOT REGEXP '^[A-Z,_]+$'");

        if (updated != 0) {
            plugin.info("Fixed " + updated + " rows of data in the Kill table");
        }
    }

    private void fixConversionForDeath(Connection con) throws SQLException {
        Statement st = con.createStatement();
        ResultSet needsFix = st.executeQuery("SELECT * FROM " + getPrefix() + "death WHERE (convert(cause using latin1) COLLATE latin1_general_cs) NOT REGEXP '^[A-Z,_]+$'");
        int updated = 0;
        PreparedStatement update = con.prepareStatement("UPDATE " + getPrefix() + "death SET value=value+? WHERE uuid=? AND world=? AND (convert(cause using latin1) COLLATE latin1_general_cs)=?");
        PreparedStatement insert = con.prepareStatement("INSERT INTO " + getPrefix() + "death (uuid, value, world, cause) VALUES (?, ?, ?, ?)");
        boolean messageSent = false;
        while (needsFix.next()) {
            if (!messageSent) {
                messageSent = true;
                plugin.info("Fixing conversion error in the Death table, this may take a while...");
            }
            update.setDouble(1, needsFix.getInt("value"));
            update.setString(2, needsFix.getString("uuid"));
            update.setString(3, needsFix.getString("world"));
            update.setString(4, needsFix.getString("cause").toUpperCase()); // Here's the difference with the original query
            if (update.executeUpdate() == 0) {
                // Update didn't do anything; insert instead.
                insert.setString(1, needsFix.getString("uuid"));
                insert.setDouble(2, needsFix.getInt("value"));
                insert.setString(3, needsFix.getString("world"));
                insert.setString(4, needsFix.getString("cause").toUpperCase()); // Here's the difference with the original query
                insert.execute();
            }
            updated++;
        }
        // Delete all old non-adhering rows from the database
        st.execute("DELETE FROM " + getPrefix() + "death WHERE (convert(cause using latin1) COLLATE latin1_general_cs) NOT REGEXP '^[A-Z,_]+$'");

        if (updated != 0) {
            plugin.info("Fixed " + updated + " rows of data in the Death table");
        }
    }

}
