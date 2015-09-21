package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.Trades;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;

/**
 *
 * @author Lolmewn
 */
public class BukkitTrades extends Trades implements Listener {

    private final BukkitMain plugin;

    public BukkitTrades(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(InventoryClickEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getInventory().getType() != InventoryType.MERCHANT) {
            return;
        }
        if (!event.getSlotType().equals(SlotType.RESULT)) {
            return;
        }
        if (!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && !event.getAction().equals(InventoryAction.PICKUP_ALL)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if(player.hasMetadata("NPC")){
            return;
        }
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        holder.addEntry(this, new DefaultStatEntry(1,
                new MetadataPair("world", player.getWorld().getName())
        ));
    }

}
