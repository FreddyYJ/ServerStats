package nl.lolmewn.stats.bukkit.signs;

import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.signs.StatsSign;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Lolmewn
 */
public class SignTask extends BukkitRunnable {

    private final BukkitMain plugin;

    public SignTask(BukkitMain plugin) {
        this.plugin = plugin;
        this.runTaskTimer(plugin, 0l, 60l);
    }

    @Override
    public void run() {
        plugin.getSignManager().getSigns().stream().forEach((sign) -> {
            sign.update(plugin.getStatManager(), plugin.getUserManager());
        });
    }

}
