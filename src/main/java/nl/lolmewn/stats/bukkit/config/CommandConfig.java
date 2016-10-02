package nl.lolmewn.stats.bukkit.config;

import java.util.List;
import nl.lolmewn.itemmanager.inv.ManagedInventory;
import nl.lolmewn.stats.bukkit.BukkitMain;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public class CommandConfig {

    private final BukkitMain plugin;
    private final ManagedInventory mainInventory;

    public CommandConfig(BukkitMain plugin) {
        this.plugin = plugin;
        mainInventory = new ManagedInventory(plugin, "&2Stats command");
        init();
    }

    public void start(Player player) {
        mainInventory.open(player);
    }

    private void init() {
        List<String> statsToShow = plugin.getConfig().getStringList("statsCommand.show");
        
    }

}
