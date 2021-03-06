package nl.lolmewn.stats;

import java.io.IOException;
import java.util.UUID;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.user.UserManager;
import nl.lolmewn.stats.storage.StorageEngineManager;

/**
 *
 * @author Lolmewn
 */
public interface Main {
    
    public void debug(String message);
    public void info(String message);
    public void disableStat(Stat stat);
    public void enableStat(Stat stat);
    public StorageEngineManager getStorageEngineManager();
    public UserManager getUserManager();
    public StatManager getStatManager();
    public String getName(UUID player);
    public boolean hasPlugin(String name);
    public void scheduleTask(Runnable runnable, int ticks);
    public void scheduleTaskAsync(Runnable runnable, int ticks);
    public void saveCustomStat(Stat stat) throws IOException;

}
