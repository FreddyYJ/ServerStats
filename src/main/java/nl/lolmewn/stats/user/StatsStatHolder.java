package nl.lolmewn.stats.user;

import java.util.ArrayList;
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
import nl.lolmewn.stats.bukkit.BukkitUtil;
import nl.lolmewn.stats.bukkit.api.event.StatsHolderUpdateEvent;
import org.bukkit.Bukkit;

/**
 *
 * @author Lolmewn
 */
public class StatsStatHolder implements StatsHolder {

    private final UUID uuid;
    private final String name;
    private final Map<Stat, List<StatEntry>> entries;
    private final List<StatEntry> removedEntries = new ArrayList<>();

    public StatsStatHolder(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.entries = new ConcurrentHashMap<>();
    }

    public String getName() {
        return name;
    }

    @Override
    public void addEntry(Stat stat, StatEntry entry) {
        if (stat == null) {
            throw new IllegalStateException("Stat can't be null");
        }
        if (!this.hasStat(stat)) {
            this.entries.put(stat, Collections.synchronizedList(new LinkedList<StatEntry>()));
        }
        if(BukkitUtil.isBukkit()){
            StatsHolderUpdateEvent event = new StatsHolderUpdateEvent(this, stat, entry);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if(event.isCancelled()){
                return;
            }
        }
        for (StatEntry existing : getStats(stat)) {
            if (existing.getMetadata().equals(entry.getMetadata())) {
                existing.setValue(existing.getValue() + entry.getValue());
                return;
            }
        }
        getStats(stat).add(entry);
    }

    @Override
    public Collection<Stat> getStats() {
        return entries.keySet();
    }

    /**
     * Get all {@link nl.lolmewn.stats.api.stat.StatEntry}s belonging to a
     * {@link nl.lolmewn.stats.api.stat.Stat} If there are no stats, an empty
     * list is returned
     *
     * @param stat Stat to lookup
     * @return List of {@link nl.lolmewn.stats.api.stat.StatEntry}s
     */
    @Override
    public synchronized Collection<StatEntry> getStats(Stat stat) {
        return hasStat(stat) ? entries.get(stat) : new ArrayList<>();
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public synchronized List<StatEntry> getRemovedEntries() {
        return removedEntries;
    }

    @Override
    public boolean hasStat(Stat stat) {
        if (stat == null) {
            throw new IllegalStateException("Stat can't be null");
        }
        return entries.containsKey(stat);
    }

    @Override
    public void removeStat(Stat stat) {
        if (!hasStat(stat)) {
            return;
        }
        synchronized (removedEntries) {
            this.removedEntries.addAll(this.getStats(stat));
        }
        getStats().remove(stat);
    }

    @Override
    public void removeEntry(Stat stat, StatEntry entry) {
        if (!hasStat(stat)) {
            return;
        }
        if (getStats(stat).remove(entry)) {
            if (getStats(stat).isEmpty()) {
                removeStat(stat);
            }
            synchronized (removedEntries) {
                this.removedEntries.add(entry);
            }
        }
    }

}
