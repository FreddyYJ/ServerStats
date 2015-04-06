package nl.lolmewn.stats.stats.bukkit;

import java.util.UUID;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.PVP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Lolmewn
 */
public class BukkitPVP extends PVP implements Listener {

    private final BukkitMain plugin;

    public BukkitPVP(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public String format(StatEntry se) {
        return super.format(se).replace("%victim%", plugin.getServer().getOfflinePlayer((UUID) se.getMetadata().get("victim")).getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void pvp(PlayerDeathEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            Player dead = event.getEntity();
            ItemStack weapon = killer.getItemInHand();
            String weaponName = weapon == null ? "Fists" : (weapon.getType().name().substring(0, 1) + weapon.getType().name().substring(1).toLowerCase().replace("_", " "));
            // This stat only tracks pvp
            StatsHolder holder = this.plugin.getUserManager().getUser(killer.getUniqueId());
            holder.addEntry(this,
                    new DefaultStatEntry(
                            1,
                            new MetadataPair("weapon", weaponName),
                            new MetadataPair("victim", dead.getUniqueId().toString()),
                            new MetadataPair("time", System.currentTimeMillis()),
                            new MetadataPair("world", killer.getWorld().getName())
                    )
            );
        }
    }

}
