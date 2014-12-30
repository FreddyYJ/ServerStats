package nl.lolmewn.stats.stats.bukkit;

import java.util.UUID;
import nl.lolmewn.stats.BukkitMain;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.stats.PVP;

/**
 *
 * @author Lolmewn
 */
public class BukkitPVP extends PVP {

    private final BukkitMain plugin;

    public BukkitPVP(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public String format(StatEntry se) {
        return super.format(se).replace("%victim%", plugin.getServer().getOfflinePlayer((UUID) se.getMetadata().get("victim")).getName());
    }

}
