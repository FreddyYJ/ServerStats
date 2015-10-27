package nl.lolmewn.stats.mysql.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.storage.DataType;

/**
 *
 * @author Lolmewn
 */
public class MySQLColumn {

    private final String name;
    private final DataType type;
    private final List<MySQLAttribute> attribues;
    private String def;

    private MySQLTable refTable;
    private MySQLColumn refColumn;

    public MySQLColumn(String name, DataType type) {
        this.name = name;
        this.type = type;
        this.attribues = new ArrayList<>();
    }

    public MySQLColumn(String name, DataType type, String def) {
        this(name, type);
        this.def = def;
    }

    public MySQLColumn addAttribute(MySQLAttribute attr) {
        this.attribues.add(attr);
        return this;
    }

    public MySQLColumn addAttributes(MySQLAttribute... attrs) {
        this.attribues.addAll(Arrays.asList(attrs));
        return this;
    }

    public List<MySQLAttribute> getAttribues() {
        return attribues;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public boolean hasDefault() {
        return def != null;
    }

    public String getDefault() {
        return def;
    }

    public boolean references() {
        return this.refTable != null;
    }

    public MySQLTable getRefTable() {
        return refTable;
    }

    public MySQLColumn getRefColumn() {
        return refColumn;
    }

    public MySQLColumn references(MySQLTable table, MySQLColumn column) {
        this.refColumn = column;
        this.refTable = table;
        return this;
    }

    public MySQLColumn setDefault(String def) {
        this.def = def;
        return this;
    }

    public String getMySQLType() {
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
        Logger.getLogger(MySQLColumn.class.getName()).log(Level.SEVERE, "Unknown data type " + type.name() + ", attempting MySQL storage plan regardless");
        return type.name();
    }

}
