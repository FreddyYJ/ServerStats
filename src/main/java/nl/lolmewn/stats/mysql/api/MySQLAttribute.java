package nl.lolmewn.stats.mysql.api;

/**
 *
 * @author Lolmewn
 */
public enum MySQLAttribute {

    NULL("NULL"),
    NOT_NULL("NOT NULL"),
    PRIMARY_KEY("PRIMARY KEY"),
    AUTO_INCREMENT("AUTO_INCREMENT");

    private final String mysqlEquiv;

    private MySQLAttribute(String mysqlEquiv) {
        this.mysqlEquiv = mysqlEquiv;
    }

    public String getMySQLEquiv() {
        return this.mysqlEquiv;
    }
}
