package nl.lolmewn.stats.mysql;

/**
 *
 * @author Lolmewn
 */
public class MySQLConfig {
    
    private String host, database, username, password, prefix;
    private int port;

    public String getHost() {
        return host;
    }

    public MySQLConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public MySQLConfig setDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MySQLConfig setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public MySQLConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public int getPort() {
        return port;
    }

    public MySQLConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public MySQLConfig setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

}
