package nl.lolmewn.stats.user;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;

/**
 *
 * @author Lolmewn
 */
public class MySQLStatHolder extends StatsStatHolder {

    private boolean temp = true;
    private final ConcurrentLinkedQueue<Pair<Stat, StatEntry>> additions = new ConcurrentLinkedQueue<>();

    public MySQLStatHolder(UUID uuid, String name) {
        super(uuid, name);
    }

    @Override
    public void addEntry(Stat stat, StatEntry entry) {
        super.addEntry(stat, entry);
        additions.add(new Pair(stat, entry));
    }

    public void addEntryLoaded(Stat stat, StatEntry entry) {
        super.addEntry(stat, entry);
    }

    public ConcurrentLinkedQueue<Pair<Stat, StatEntry>> getAdditions() {
        return additions;
    }

    public boolean isTemp() {
        return temp;
    }

    public void setTemp(boolean value) {
        this.temp = value;
    }

}
