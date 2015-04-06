package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.ItemsDropped;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitItemsDropped extends ItemsDropped implements Listener {
    
    private final BukkitMain plugin;
    
    public BukkitItemsDropped(BukkitMain plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(PlayerDropItemEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(event.getItemDrop().getItemStack().getAmount(),
                new MetadataPair("world", player.getWorld().getName()),
                new MetadataPair("name", event.getItemDrop().getItemStack().getType().name())
        ));
    }
    
}
