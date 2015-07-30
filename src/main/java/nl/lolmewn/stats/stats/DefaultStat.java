package nl.lolmewn.stats.stats;

import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.api.stat.Stat;

/**
 *
 * @author Lolmewn
 */
public abstract class DefaultStat implements Stat, Summable {

    private boolean enabled = false;

    @Override
    public String getDescription() {
        return Messages.getMessage(getMessagesRootPath() + ".description");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    public String getMessagesRootPath() {
        return "stats." + this.getName().toLowerCase().replace(" ", "_");
    }

    @Override
    public boolean isSummable() {
        return true;
    }

}
