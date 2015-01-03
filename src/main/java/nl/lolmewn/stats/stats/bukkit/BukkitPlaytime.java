package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.BukkitMain;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.Playtime;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public class BukkitPlaytime extends Playtime {

    public BukkitPlaytime(BukkitMain plugin) {
        schedulePlaytimeRecording(plugin);
    }

    private void schedulePlaytimeRecording(final BukkitMain plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
                    holder.addEntry(
                            BukkitPlaytime.this,
                            new DefaultStatEntry(
                                    BukkitPlaytime.this,
                                    1,
                                    new MetadataPair(
                                            "world",
                                            player.getWorld().getName()
                                    )
                            )
                    );
                }
            }
        }, 0L, 20L);
    }
}
