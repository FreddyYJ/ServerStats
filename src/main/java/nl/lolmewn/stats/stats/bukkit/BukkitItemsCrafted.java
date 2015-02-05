package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.BukkitMain;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.ItemsCrafted;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitItemsCrafted extends ItemsCrafted implements Listener {

    private final BukkitMain plugin;

    public BukkitItemsCrafted(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(CraftItemEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(event.getCurrentItem().getAmount(),
                new MetadataPair("world", player.getWorld().getName()),
                new MetadataPair("name", event.getCurrentItem().getType().name()))
        );
    }

}
