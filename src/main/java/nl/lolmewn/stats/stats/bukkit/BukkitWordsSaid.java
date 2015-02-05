package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.BukkitMain;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.WordsSaid;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitWordsSaid extends WordsSaid implements Listener {

    private final BukkitMain plugin;

    public BukkitWordsSaid(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(AsyncPlayerChatEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        int words = event.getMessage().split(" ").length;
        words += words == 0 ? 1 : 0;
        Player player = event.getPlayer();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(1,
                new MetadataPair("world", player.getWorld().getName())
        ));
    }

}
