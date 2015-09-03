package nl.lolmewn.stats.user;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;

/**
 *
 * @author Lolmewn
 */
public class MySQLStatHolder extends StatsStatHolder {

    private boolean temp = true;
    private final Map<Stat, Queue<StatEntry>> additions = new ConcurrentHashMap<>();

    public MySQLStatHolder(UUID uuid, String name) {
        super(uuid, name);
    }

    @Override
    public void addEntry(Stat stat, StatEntry entry) {
        super.addEntry(stat, entry);
        if (!this.additions.containsKey(stat)) {
            this.additions.put(stat, new ConcurrentLinkedQueue<StatEntry>());
        }
        for (StatEntry existing : additions.get(stat)) {
            if (existing.getMetadata().equals(entry.getMetadata())) {
                existing.setValue(existing.getValue() + entry.getValue());
                return;
            }
        }
        additions.get(stat).add(entry);
    }

    public void addEntryLoaded(Stat stat, StatEntry entry) {
        super.addEntry(stat, entry);
    }

    public Map<Stat, Queue<StatEntry>> getAdditions() {
        return additions;
    }

    public boolean isTemp() {
        return temp;
    }

    public void setTemp(boolean value) {
        this.temp = value;
    }

}
