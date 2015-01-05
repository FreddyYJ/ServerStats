package nl.lolmewn.stats;

import java.util.UUID;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.user.UserManager;

/**
 *
 * @author Lolmewn
 */
public interface Main {
    
    public UserManager getUserManager();
    public StatManager getStatManager();
    public String getName(UUID player);

}
