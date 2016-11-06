package nl.lolmewn.stats.command;

import java.util.concurrent.ThreadPoolExecutor;
import nl.lolmewn.stats.api.storage.StorageEngine;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.mysql.MySQLStorage;
import org.bukkit.ChatColor;

/**
 *
 * @author Lolmewn
 */
public class StatsDebugCommand extends SubCommand {
    
    private final BukkitMain plugin;
    
    public StatsDebugCommand(BukkitMain plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(Dispatcher sender, String[] args) {
        sender.sendMessage(ChatColor.GRAY + "===== Checking storage engines =====");
        for (String name : plugin.getStorageEngineManager().getStorageEngines().keySet()) {
            StorageEngine engine = plugin.getStorageEngineManager().getStorageEngine(name);
            sender.sendMessage(ChatColor.GOLD + name + ": " + (engine.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            if (engine.isEnabled()) {
                debugEngine(sender, engine);
            }
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
        return "stats.debug";
    }
    
    private void debugEngine(Dispatcher sender, StorageEngine engine) {
        if (engine instanceof MySQLStorage) {
            MySQLStorage mysql = (MySQLStorage) engine;
            sender.sendMessage("Num connections: " + mysql.getDataSource().getNumActive() + ", num idle: " + mysql.getDataSource().getNumIdle());
            ThreadPoolExecutor tpe = mysql.getThreadPool();
            sender.sendMessage("Num threads saving users: " + tpe.getActiveCount() + "/" + tpe.getPoolSize() + " with a queue of " + tpe.getQueue().size() + " users");
        }
    }
    
}
