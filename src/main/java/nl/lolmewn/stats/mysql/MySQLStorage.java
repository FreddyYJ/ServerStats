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
    private final BasicDataSource source;
    private final String prefix;
    private final Map<String, MySQLTable> tables;

    public MySQLStorage(Main main, MySQLConfig config) throws StorageException {
        this.plugin = main;
        this.source = new BasicDataSource();
        this.source.setDriverClassName("com.mysql.jdbc.Driver");
        this.source.setUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + "?zeroDateTimeBehavior=convertToNull");
        this.source.setUsername(config.getUsername());
        this.source.setPassword(config.getPassword());
        this.prefix = config.getPrefix();
        this.tables = new HashMap<>();

        // TODO more advanced options
        generateTables();
    }

    @Override
    public StatsHolder load(UUID userUuid, StatManager statManager) throws StorageException {
        StatsStatHolder holder = new StatsStatHolder(userUuid);
        try (Connection con = source.getConnection()) {
            for (Stat stat : statManager.getStats()) {
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
                                plugin.getLogger().warning("Unknown data type " + param.getValue() + ", just trying something");
                                value = set.getObject(param.getKey());
                        }
                        params.add(new MetadataPair(param.getKey(), value));
                    }
                    StatEntry entry = new DefaultStatEntry(stat, set.getDouble("value"), params);
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
            for (Stat stat : holder.getStats()) {
                String table = prefix + formatStatName(stat.getName());
                // TODO improve saving method by updating the value
                for (StatEntry entry : holder.getStats(stat)) {
                    StringBuilder update = new StringBuilder("UPDATE ");
                    update.append(table);
                    update.append(" SET value=? WHERE uuid=?");
                    // TODO metadata
                    PreparedStatement st = con.prepareStatement(update.toString());
                    st.setDouble(1, entry.getValue());
                    st.setString(2, holder.getUuid().toString());
                    // TODO metadata
                    st.execute();
                }
            }
        } catch (SQLException ex) {
            throw new StorageException("Something went wrong while saving the user!", ex);
        }
    }

    private void generateTables() throws StorageException {
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

}
