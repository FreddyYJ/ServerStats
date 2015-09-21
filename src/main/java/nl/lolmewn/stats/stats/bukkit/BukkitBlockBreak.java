package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.BlockBreak;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitBlockBreak extends BlockBreak implements Listener {

    private final Main plugin;

    public BukkitBlockBreak(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getPlayer().hasMetadata("NPC")) {
            return;
        }
        Block block = event.getBlock();
        StatsHolder holder = plugin.getUserManager().getUser(event.getPlayer().getUniqueId());
        holder.addEntry(this,
                new DefaultStatEntry(
                        1,
                        new MetadataPair("name", block.getType().toString()),
                        new MetadataPair("data", block.getData()), // Replace once a better system is in place
                        new MetadataPair("world", block.getWorld().getName())
                )
        );
    }

}
