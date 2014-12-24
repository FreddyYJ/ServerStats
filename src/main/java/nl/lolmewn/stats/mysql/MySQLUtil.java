package nl.lolmewn.stats.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Lolmewn
 */
public class MySQLUtil {

    public boolean tableExists(Connection con, String table) throws SQLException {
        DatabaseMetaData dbm = con.getMetaData();
        ResultSet tables = dbm.getTables(null, null, table, null);
        return tables.next();
    }

    public boolean columnExists(Connection con, String table, String column) throws SQLException {
        DatabaseMetaData dbm = con.getMetaData();
        ResultSet columns = dbm.getColumns(null, null, table, column);
        return columns.next();
    }

}
