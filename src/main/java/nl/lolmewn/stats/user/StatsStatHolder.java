package nl.lolmewn.stats.user;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.user.StatsHolder;

/**
 *
 * @author Lolmewn
 */
public class StatsStatHolder implements StatsHolder {

    private final UUID uuid;
    private boolean temp = true;
    private final Map<Stat, List<StatEntry>> entries;

    public StatsStatHolder(UUID uuid) {
        this.uuid = uuid;
        this.entries = new ConcurrentHashMap<>();
    }

    public boolean isTemp() {
        return temp;
    }

    public void setTemp(boolean value) {
        this.temp = value;
    }

    @Override
    public void addEntry(Stat stat, StatEntry entry) {
        if (!this.hasStat(stat)) {
            this.entries.put(stat, Collections.synchronizedList(new LinkedList<StatEntry>()));
        }
        this.entries.get(stat).add(entry);
    }

    @Override
    public Collection<Stat> getStats() {
        return entries.keySet();
    }

    @Override
    public Collection<StatEntry> getStats(Stat stat) {
        return entries.get(stat);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean hasStat(Stat stat) {
        return entries.containsKey(stat);
    }

}
