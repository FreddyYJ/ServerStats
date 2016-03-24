package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.FishCaught;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

/**
 *
 * @author Lolmewn
 */
public class BukkitFishCaught extends FishCaught implements Listener {

    private final BukkitMain plugin;

    public BukkitFishCaught(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(PlayerFishEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (!event.getState().equals(State.CAUGHT_FISH)) {
            return;
        }
        if (event.getPlayer().hasMetadata("NPC")) {
            return;
        }
        Player player = event.getPlayer();
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(1,
                new MetadataPair("world", player.getWorld().getName())
        ));
    }

}
