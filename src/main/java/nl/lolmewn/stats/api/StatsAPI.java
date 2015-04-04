package nl.lolmewn.stats.api;

import java.util.UUID;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.storage.StorageEngine;
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

    public StatsHolder getPlayer(UUID uuid) {
        return plugin.getUserManager().getUser(uuid);
    }
    
    public void loadPlayer(UUID uuid) throws Exception{
        this.plugin.getUserManager().loadUser(uuid, plugin.getStatManager());
    }

    public StatManager getStatManager() {
        return plugin.getStatManager();
    }
    
    public void addStorageEngine(String name, StorageEngine engine){
        plugin.getStorageEngineManager().addStorageEngine(name, engine);
    }
    
    public StorageEngine getStorageEngine(String name){
        return plugin.getStorageEngineManager().getStorageEngine(name);
    }

}
