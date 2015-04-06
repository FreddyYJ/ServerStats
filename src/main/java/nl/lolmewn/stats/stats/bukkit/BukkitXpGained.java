package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.XpGained;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitXpGained extends XpGained implements Listener {

    private final BukkitMain plugin;

    public BukkitXpGained(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(PlayerExpChangeEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(event.getAmount(),
                new MetadataPair("world", player.getWorld().getName())
        ));
    }

}
