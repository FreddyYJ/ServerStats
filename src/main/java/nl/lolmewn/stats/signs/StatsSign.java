package nl.lolmewn.stats.signs;

import java.util.Collection;
import java.util.UUID;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.user.UserManager;

/**
 *
 * @author Lolmewn
 */
public interface StatsSign {
    
    public void addHolder(UUID uuid);
    public void addStat(Stat stat);
    public Collection<UUID> getHolders();
    public SignLocation getLocation();
    public Collection<Stat> getStats();
    public SignPlayerType getSignType();
    public boolean isActive();
    public void update(StatManager statManager, UserManager userManager);
    public void setText(String... text);
    
}
