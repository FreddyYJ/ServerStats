package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.DamageTaken;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitDamageTaken extends DamageTaken implements Listener {

    private final BukkitMain plugin;

    public BukkitDamageTaken(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(EntityDamageEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (player.hasMetadata("NPC")) {
            return;
        }
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        if (holder == null) {
            // ignore, likely an NPC
            return;
        }
        holder.addEntry(this, new DefaultStatEntry(event.getDamage(),
                new MetadataPair("world", player.getWorld().getName()),
                new MetadataPair("cause", event.getCause().toString())
        ));
    }

}
