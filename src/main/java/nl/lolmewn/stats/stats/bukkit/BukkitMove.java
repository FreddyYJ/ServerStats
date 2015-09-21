package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.Move;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitMove extends Move implements Listener {

    private final BukkitMain plugin;

    public BukkitMove(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent) {
            return;
        }
        if (!this.isEnabled()) {
            return;
        }
        if (event.getPlayer().hasMetadata("NPC")) {
            return;
        }
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }
        // For now, just throw the value in the Holder object. If it's too heavy, cache it just like in Stats 2
        StatsHolder holder = plugin.getUserManager().getUser(event.getPlayer().getUniqueId());
        holder.addEntry(this,
                new DefaultStatEntry(
                        event.getFrom().distance(event.getTo()),
                        new MetadataPair("world", event.getFrom().getWorld().getName()),
                        new MetadataPair("type", getMoveType(event.getPlayer()))
                )
        );
    }

    private int getMoveType(Player player) {
        if (player.isFlying()) {
            return 6;
        }
        if (player.isInsideVehicle()) {
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof Boat) {
                return 1;
            } else if (vehicle instanceof Minecart) {
                return 2;
            } else if (vehicle instanceof Pig) {
                if (vehicle.isInsideVehicle() && vehicle.getVehicle() instanceof Minecart) {
                    return 4;
                } else {
                    return 3;
                }
            } else {
                try {
                    if (vehicle instanceof Horse) {
                        return 5;
                    }
                } catch (Exception e) {
                    //MC version doesn't have horses yet :O
                }
            }
        }
        return 0; // Anything that we don't know is walking.
    }

}
