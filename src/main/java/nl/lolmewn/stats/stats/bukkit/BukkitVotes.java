package nl.lolmewn.stats.stats.bukkit;

import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stats.Votes;
import org.bukkit.event.Listener;

/**
 *
 * @author Lolmewn
 */
public class BukkitVotes extends Votes implements Listener {

    private final BukkitMain plugin;

    public BukkitVotes(BukkitMain plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("The Votes stat has been disabled due to issues with Votifier.");
        plugin.getLogger().info("You may disregard this message if you don't use Votifier.");
    }

    /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void vote(VotifierEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = plugin.getServer().getPlayer(event.getVote().getUsername());
        if (player == null) {
            return;
        }
        plugin.getUserManager().getUser(player.getUniqueId()).addEntry(this, new DefaultStatEntry(1));
    }*/
}
