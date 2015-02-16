package nl.lolmewn.stats.api;

import java.util.UUID;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.user.StatsHolder;

/**
 *
 * @author Lolmewn
 */
public class StatsAPI {
    
    private final Main plugin;

    public StatsAPI(Main plugin) {
        this.plugin = plugin;
    }
    
    public StatsHolder getPlayer(UUID uuid){
        return plugin.getUserManager().getUser(uuid);
    }

    public StatManager getStatManager() {
        return plugin.getStatManager();
    }

}
