package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.LastJoin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitLastJoin extends LastJoin implements Listener {

    private final BukkitMain plugin;

    public BukkitLastJoin(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(PlayerJoinEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(System.currentTimeMillis(),
                new MetadataPair("world", player.getWorld().getName())
        ));
    }

}
