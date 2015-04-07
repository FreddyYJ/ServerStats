package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.Death;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitDeath extends Death implements Listener {

    private final BukkitMain plugin;

    public BukkitDeath(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(PlayerDeathEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
        String cause;
        if(damageEvent != null){
            if(damageEvent instanceof EntityDamageByEntityEvent){
                cause = ((EntityDamageByEntityEvent)damageEvent).getDamager().getType().toString();
            }else{
                cause = damageEvent.getCause().toString();
            }
        }else{
            cause = "Unknown";
        }
        Player player = event.getEntity();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(1,
                new MetadataPair("world", player.getWorld().getName()),
                new MetadataPair("cause", cause)
        ));
    }

}
