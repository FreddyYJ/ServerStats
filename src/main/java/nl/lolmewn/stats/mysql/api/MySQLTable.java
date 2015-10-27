package nl.lolmewn.stats.mysql.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.storage.DataType;

/**
 * Class representing a table in a MySQL database. Contains simple information
 * such as the name and columns of the table
 *
 * @author Lolmewn
 */
public class MySQLTable {

    private final String name;
    private final Map<String, MySQLColumn> columns = new LinkedHashMap<>();

    /**
     * Constructs a new MySQLTable Object
     *
     * @param name Name of the MySQL table
     */
    public MySQLTable(String name) {
        this.name = name;
    }

    /**
     * Add a column to this table.
     *
     * If this is done before the tables are generated, the MySQL engine will
     * create your tables if they do not exist in the database. This method can
     * be chained to add extra attributes to the column.
     *
     * @see MySQLColumn#addAttribute(nl.lolmewn.stats.mysql.api.MySQLAttribute)
     * @param name Name of the column
     * @param type Data type of the column
     * @return The newly created column
     */
    public MySQLColumn addColumn(String name, DataType type) {
        this.columns.put(name, new MySQLColumn(name, type));
        return this.columns.get(name);
    }
    
    public void addColumn(MySQLColumn column){
        this.columns.put(column.getName(), column);
    }

    /**
     * Returns all columns in this MySQLTable object.
     *
     * @return All columns
     */
    public Collection<MySQLColumn> getColumns() {
        return columns.values();
    }

    /**
     * Name of this MySQLTable
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Return a MySQLColumn with a given name
     *
     * @param name name of the column to return
     * @return MySQLColumn associated with the name, otherwise null as per
     * {@link java.util.TreeMap} implementation
     */
    public MySQLColumn getColumn(String name) {
        return this.columns.get(name);
    }

    /**
     * Builds the query required to generate the table containing the columns of
     * this object. Takes defaults, attributes and references into account
     *
     * @return query-ready String
     */
    public String generateCreateQuery() {
        StringBuilder createQuery = new StringBuilder();
        createQuery.append("CREATE TABLE IF NOT EXISTS ");
        createQuery.append(this.name);
        createQuery.append(" (");
        for (Iterator<MySQLColumn> it = getColumns().iterator(); it.hasNext();) {
            MySQLColumn column = it.next();
            createQuery.append(column.getName());
            createQuery.append(" ");
            createQuery.append(column.getMySQLType());
            createQuery.append(" ");
            Iterator<MySQLAttribute> attrIterator = column.getAttribues().listIterator();
            while (attrIterator.hasNext()) {
                createQuery.append(attrIterator.next().getMySQLEquiv());
                if (attrIterator.hasNext()) {
                    createQuery.append(" ");
                }
            }
            if (column.hasDefault()) {
                createQuery.append(" DEFAULT ");
                createQuery.append(column.getDefault());
            }
            if (it.hasNext()) {
                createQuery.append(", ");
            }
        }
        this.getColumns().stream().filter((column) -> (column.references())).map((column) -> {
            createQuery.append(", FOREIGN KEY (");
            createQuery.append(column.getName());
            return column;
        }).map((column) -> {
            createQuery.append(") REFERENCES ");
            createQuery.append(column.getRefTable().getName());
            return column;
        }).map((column) -> {
            createQuery.append("(");
            createQuery.append(column.getRefColumn().getName());
            return column;
        }).forEach((_item) -> {
            createQuery.append(") ON DELETE CASCADE ON UPDATE CASCADE");
        });
        createQuery.append(") ENGINE = INNODB;");
        return createQuery.toString();
    }

}
