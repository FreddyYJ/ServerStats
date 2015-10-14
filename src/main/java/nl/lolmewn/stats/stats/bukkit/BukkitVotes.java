package nl.lolmewn.stats.stats.bukkit;

import com.vexsoftware.votifier.model.VotifierEvent;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stats.Votes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 *
 * @author Lolmewn
 */
public class BukkitVotes extends Votes implements Listener {

    private final BukkitMain plugin;

    public BukkitVotes(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void vote(VotifierEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = plugin.getServer().getPlayer(event.getVote().getUsername());
        if (player == null) {
            return;
        }
        plugin.getUserManager().getUser(player.getUniqueId()).addEntry(this, new DefaultStatEntry(1));
    }
}
