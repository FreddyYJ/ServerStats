package nl.lolmewn.stats.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.UUID;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.storage.DataType;
import nl.lolmewn.stats.api.storage.StorageEngine;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.user.StatsStatHolder;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author Lolmewn
 */
public class MySQLStorage implements StorageEngine {

    private final Main plugin;
    private final BasicDataSource source;
    private final String prefix;

    public MySQLStorage(Main main, MySQLConfig config) throws StorageException {
        this.plugin = main;
        source = new BasicDataSource();
        source.setDriverClassName("com.mysql.jdbc.Driver");
        source.setUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + "?zeroDateTimeBehavior=convertToNull");
        source.setUsername(config.getUsername());
        source.setPassword(config.getPassword());
        this.prefix = config.getPrefix();

        // TODO more advanced options
        generateTables();
    }

    @Override
    public StatsHolder load(UUID userUuid, StatManager statManager) throws StorageException {
        StatsStatHolder holder = new StatsStatHolder(userUuid);
        try (Connection con = source.getConnection()) {
            for (Stat stat : statManager.getStats()) {
                String table = prefix + formatStatName(stat.getName());

            }
        } catch (SQLException ex) {
            throw new StorageException("Something went wrong while loading the user!", ex);
        }
        // TODO load user
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

        } catch (SQLException ex) {
            throw new StorageException("Something went wrong while saving the user!", ex);
        }
        // TODO save user 
    }

    private void generateTables() throws StorageException {
        // TODO Stats_players table
        try (Connection con = this.source.getConnection()) {
            for (Stat stat : plugin.getStatManager().getStats()) {
                String tableName = prefix + formatStatName(stat.getName());
                StringBuilder createQuery = new StringBuilder();
                createQuery.append("CREATE TABLE IF NOT EXISTS ");
                createQuery.append(tableName);
                createQuery.append(" (id INT PRIMARY KEY UNIQUE AUTO_INCREMENT, uuid VARCHAR(36), value DOUBLE");
                for (Entry<String, DataType> entry : stat.getDataTypes().entrySet()) {
                    createQuery.append(", ");
                    createQuery.append(entry.getKey().toLowerCase());
                    createQuery.append(" ");
                    createQuery.append(getMySQLType(entry.getValue()));
                }
                createQuery.append(" FOREIGN KEY (uuid) REFERENCES ");
                createQuery.append(prefix);
                createQuery.append("players(uuid) ON DELETE CASCADE)");
                con.createStatement().execute(createQuery.toString());
            }
        } catch (SQLException ex) {
            throw new StorageException("Failed to generate tables for stats", ex);
        }
    }

    public String formatStatName(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    private String getMySQLType(DataType type) {
        switch (type) {
            case BOOLEAN:
                return "BIT";
            case DOUBLE:
                return "DOUBLE";
            case FLOAT:
                return "FLOAT";
            case INTEGER:
                return "INT";
            case LONG:
                return "BIGINT";
            case STRING:
                return "VARCHAR(255)";
            case TIMESTAMP:
                return "TIMESTAMP";
            case BYTE_ARRAY:
                return "BLOB";
        }
        plugin.getLogger().warning("Unknown data type " + type.name() + ", attempting MySQL storage plan regardless");
        return type.name();
    }

}
