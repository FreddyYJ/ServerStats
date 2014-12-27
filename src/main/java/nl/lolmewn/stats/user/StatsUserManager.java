package nl.lolmewn.stats.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.StorageEngine;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;

/**
 *
 * @author Lolmewn
 */
public class StatsUserManager extends DefaultUserManager {

    private final Main plugin;
    private final StorageEngine storage;

    public StatsUserManager(Main plugin, StorageEngine storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public void saveUser(UUID uuid) throws StorageException {
        storage.save(this.getUser(uuid));
    }

    @Override
    public StatsHolder loadUser(UUID uuid, StatManager statManager) throws StorageException {
        StatsHolder holder = new DefaultStatsHolder(uuid);
        loadAsync(holder, statManager);
        this.addUser(holder);
        return holder;
    }

    private void loadAsync(final StatsHolder holder, final StatManager statManager) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                try {
                    final StatsHolder loadedHolder = storage.load(holder.getUuid(), statManager);
                    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

                        @Override
                        public void run() {
                            sync(loadedHolder);
                        }
                    });
                } catch (StorageException ex) {
                    Logger.getLogger(StatsUserManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void sync(StatsHolder holder) {
        StatsHolder other = this.getUser(holder.getUuid());
        if (other == null) {
            this.addUser(holder);
            return;
        }
        for (Stat stat : other.getStats()) {
            for (StatEntry entry : other.getStats(stat)) {
                holder.addEntry(stat, entry);
            }
        }
        this.removeUser(other.getUuid());
        this.addUser(other);
    }
    
    public void merge(){
        for(StatsHolder holder : this.getUsers()){
            merge(holder.getUuid());
        }
    }
    
    public void merge(UUID uuid){
        StatsHolder holder = this.getUser(uuid);
        if(holder == null){
            return;
        }
        for(Stat stat : holder.getStats()){
            Map<Map<String, Object>, Double> map = new HashMap<>();
            Iterator<StatEntry> it = holder.getStats(stat).iterator();
            while(it.hasNext()){
                StatEntry entry = it.next();
                if(map.containsKey(entry.getMetadata())){
                    map.put(entry.getMetadata(), map.get(entry.getMetadata()) + entry.getValue());
                }else{
                    map.put(entry.getMetadata(), entry.getValue());
                }
                it.remove();
            }
            for(Entry<Map<String, Object>, Double> entry : map.entrySet()){
                holder.addEntry(stat, new DefaultStatEntry(stat, entry.getValue(), entry.getKey()));
            }
        }
    }

}
