package nl.lolmewn.stats.mysql.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.api.storage.DataType;

/**
 *
 * @author Lolmewn
 */
public class MySQLTable {

    private final String name;
    private final Map<String, MySQLColumn> columns = new HashMap<>();

    public MySQLTable(String name) {
        this.name = name;
    }

    public MySQLColumn addColumn(String name, DataType type) {
        this.columns.put(name, new MySQLColumn(name, type));
        return this.columns.get(name);
    }

    public Collection<MySQLColumn> getColumns() {
        return columns.values();
    }

    public String getName() {
        return name;
    }

}
