package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.stats.BlockBreak;
import org.bukkit.event.Listener;

/**
 *
 * @author Lolmewn
 */
public class BukkitBlockBreak extends BlockBreak implements Listener {
    
    private final Main plugin;

    public BukkitBlockBreak(Main plugin) {
        this.plugin = plugin;
    }
    
    

}
