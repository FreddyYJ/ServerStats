package nl.lolmewn.stats.user;

import java.util.UUID;

/**
 *
 * @author Lolmewn
 */
public class StatsStatHolder extends DefaultStatsHolder {

    private boolean temp = true;
    
    public StatsStatHolder(UUID uuid) {
        super(uuid);
    }

    public boolean isTemp() {
        return temp;
    }

    public void setTemp(boolean value) {
        this.temp = value;
    }

}
