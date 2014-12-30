package nl.lolmewn.stats;

import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.user.DefaultStatsHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Lolmewn
 */
public class Events implements Listener {
    
    private final BukkitMain plugin; 

    public Events(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event){
        try {
            plugin.getUserManager().loadUser(event.getPlayer().getUniqueId(), plugin.getStatManager());
        } catch (Exception ex) {
            Logger.getLogger(Events.class.getName()).log(Level.SEVERE, null, ex);
            plugin.getLogger().severe("The error above means Stats was unable to load player " + event.getPlayer().getName());
            plugin.getLogger().severe("In an attempt to not completely ruin your server, we've given him an empty Stats account");
            plugin.getUserManager().addUser(new DefaultStatsHolder(event.getPlayer().getUniqueId()));
            plugin.getLogger().severe("Please check if this issue has been reported before. If not, report it with as much info as you can");
        }
    }
    
}
