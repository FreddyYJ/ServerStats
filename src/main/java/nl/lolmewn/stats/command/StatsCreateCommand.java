package nl.lolmewn.stats.command;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.mysql.MySQLStorage;
import nl.lolmewn.stats.stats.SimpleStat;
import nl.lolmewn.stats.util.Util;

/**
 *
 * @author Lolmewn
 */
public class StatsCreateCommand extends SubCommand {

    private final Main plugin;

    public StatsCreateCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Dispatcher sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Please specify a name");
            return;
        }
        String name = args[0];
        if (Util.findStat(plugin.getStatManager(), name) != null) {
            // Another stat found with this name
            sender.sendMessage("A stat with this name already exists!");
            sender.sendMessage("Avoiding overwriting of other stat.");
            sender.sendMessage("Please use a different name");
            return;
        }
        Stat stat = new SimpleStat(name) {
        };
        plugin.getStatManager().addStat(stat);
        MySQLStorage storage = (MySQLStorage) plugin.getStorageEngineManager().getStorageEngine("mysql");

        if (storage != null && storage.isEnabled()) {
            sender.sendMessage("Generating the table in another thread so your server won't be bothered...");
            plugin.scheduleTaskAsync(() -> {
                try {
                    storage.addTable(storage.generateTable(stat));
                    sender.sendMessage("Table created! You can now use your '" + name + "' stat.");
                } catch (SQLException ex) {
                    Logger.getLogger(StatsCreateCommand.class.getName()).log(Level.SEVERE, null, ex);
                    sender.sendMessage("Something went horribly wrong while creating a table in the database, please check your logs.");
                }
            }, 0);
        }else{
            sender.sendMessage("Stat added! You can now use your '" + name + "' stat.");
        }
    }

    @Override
    public boolean consoleOnly() {
        return false;
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String getPermissionNode() {
        return "stats.create";
    }

}
