package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.LastLeave;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitLastLeave extends LastLeave implements Listener {

    private final BukkitMain plugin;

    public BukkitLastLeave(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(PlayerQuitEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getPlayer().hasMetadata("NPC")) {
            return;
        }
        Player player = event.getPlayer();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        if (holder == null) {
            return;
        }
        holder.getStats(this).clear();
        holder.addEntry(this, new DefaultStatEntry(System.currentTimeMillis(),
                new MetadataPair("world", player.getWorld().getName())
        ));
    }

}
