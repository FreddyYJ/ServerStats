package nl.lolmewn.stats.signs;

import java.util.Collection;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.user.StatsHolder;

/**
 *
 * @author Lolmewn
 */
public interface StatsSign {
    
    public Collection<StatsHolder> getHolders();
    public SignLocation getLocation();
    public Collection<Stat> getStats();
    public SignType getSignType();
    public boolean isActive();
    public void setText(String... text);
    
}
