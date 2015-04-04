package nl.lolmewn.stats;

import java.util.UUID;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.user.UserManager;
import nl.lolmewn.stats.storage.StorageEngineManager;

/**
 *
 * @author Lolmewn
 */
public interface Main {
    
    public StorageEngineManager getStorageEngineManager();
    public UserManager getUserManager();
    public StatManager getStatManager();
    public String getName(UUID player);
    public void scheduleTask(Runnable runnable, int ticks);

}
