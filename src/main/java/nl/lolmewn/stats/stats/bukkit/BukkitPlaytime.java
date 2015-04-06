package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.Playtime;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Lolmewn
 */
public class BukkitPlaytime extends Playtime {

    private final BukkitMain plugin;
    private BukkitRunnable task;

    public BukkitPlaytime(BukkitMain plugin) {
        this.plugin = plugin;
    }

    private void schedulePlaytimeRecording() {
        task = new BukkitRunnable() {

            @Override
            public void run() {
                if (!BukkitPlaytime.this.isEnabled()) {
                    return;
                }
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
                    holder.addEntry(
                            BukkitPlaytime.this,
                            new DefaultStatEntry(
                                    1,
                                    new MetadataPair(
                                            "world",
                                            player.getWorld().getName()
                                    )
                            )
                    );
                }
            }
        };
        task.runTaskTimer(plugin, 0l, 20l);
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        if (value && task == null) {
            schedulePlaytimeRecording();
        }
        if (!value && task != null) {
            task.cancel();
        }
    }
}
