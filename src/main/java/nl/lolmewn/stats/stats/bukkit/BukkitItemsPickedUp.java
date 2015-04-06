package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.ItemsPickedUp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitItemsPickedUp extends ItemsPickedUp implements Listener {
    
    private final BukkitMain plugin;
    
    public BukkitItemsPickedUp(BukkitMain plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(PlayerPickupItemEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(event.getItem().getItemStack().getAmount(),
                new MetadataPair("world", player.getWorld().getName()),
                new MetadataPair("name", event.getItem().getItemStack().getType().name())
        ));
    }
    
}
