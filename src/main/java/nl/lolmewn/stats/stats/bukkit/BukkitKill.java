package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.Kill;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitKill extends Kill implements Listener {

    private final BukkitMain plugin;

    public BukkitKill(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(EntityDeathEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getEntity().getKiller() == null) {
            return; // natural cause-ish.
        }
        Player player = event.getEntity().getKiller();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        if (holder == null) {
            plugin.debug("Killer was not null but holder was not found: " + player);
            return;
        }
        holder.addEntry(
                this,
                new DefaultStatEntry(
                        1,
                        new MetadataPair("world", player.getWorld().getName()),
                        new MetadataPair("weapon",
                                player.getItemInHand() == null
                                        ? "Fists"
                                        : (player.getItemInHand().getItemMeta().hasDisplayName()
                                                ? player.getItemInHand().getItemMeta().getDisplayName()
                                                : player.getItemInHand().getType().toString())
                        ),
                        new MetadataPair("entityType", event.getEntity().getType().toString())
                ));
    }

}
