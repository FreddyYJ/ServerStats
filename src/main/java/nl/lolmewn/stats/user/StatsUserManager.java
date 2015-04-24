package nl.lolmewn.stats.user;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.event.StatsHolderLoadedEvent;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.StorageEngine;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.api.user.UserManager;
import nl.lolmewn.stats.bukkit.BukkitMain;

/**
 *
 * @author Lolmewn
 */
public class StatsUserManager implements UserManager {

    private final Map<UUID, StatsHolder> users = new ConcurrentHashMap<>();
    private final BukkitMain plugin; // TODO find a way to not have this here
    private final StorageEngine storage;

    public StatsUserManager(BukkitMain plugin, StorageEngine storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public void saveUser(UUID uuid) throws StorageException {
        storage.save(this.getUser(uuid));
    }

    @Override
    public StatsHolder loadUser(UUID uuid, StatManager statManager) throws StorageException {
        StatsHolder holder = new StatsStatHolder(uuid, plugin.getName(uuid));
        this.addUser(holder);
        loadAsync(holder, statManager);
        return holder;
    }

    @Override
    public void addUser(StatsHolder user) {
        this.users.put(user.getUuid(), user);
    }

    @Override
    public StatsHolder getUser(UUID uuid) {
        return this.users.get(uuid);
    }

    @Override
    public Collection<StatsHolder> getUsers() {
        return this.users.values();
    }

    @Override
    public void removeUser(UUID uuid) {
        this.users.remove(uuid);
    }

    private void loadAsync(final StatsHolder holder, final StatManager statManager) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                try {
                    final StatsHolder loadedHolder = storage.load(holder.getUuid(), statManager);
                    if (loadedHolder == null) {
                        return; // There was none yet
                    }
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
        this.addUser(holder);
        plugin.getServer().getPluginManager().callEvent(new StatsHolderLoadedEvent(holder));
    }

    public void merge() {
//        for(StatsHolder holder : this.getUsers()){
//            merge(holder.getUuid());
//        }
    }

    public void merge(UUID uuid) {
//        StatsHolder holder = this.getUser(uuid);
//        if(holder == null){
//            return;
//        }
//        for(Stat stat : holder.getStats()){
//            Map<Map<String, Object>, Double> map = new HashMap<>();
//            Iterator<StatEntry> it = holder.getStats(stat).iterator();
//            while(it.hasNext()){
//                StatEntry entry = it.next();
//                if(map.containsKey(entry.getMetadata())){
//                    map.put(entry.getMetadata(), map.get(entry.getMetadata()) + entry.getValue());
//                }else{
//                    map.put(entry.getMetadata(), entry.getValue());
//                }
//                it.remove();
//            }
//            for(Entry<Map<String, Object>, Double> entry : map.entrySet()){
//                holder.addEntry(stat, new DefaultStatEntry(entry.getValue(), entry.getKey()));
//            }
//        }
    }

    @Override
    public void resetUser(UUID uuid) {
        if (!this.users.containsKey(uuid)) {
            return;
        }
        try {
            this.storage.delete(this.getUser(uuid));
        } catch (StorageException ex) {
            Logger.getLogger(StatsUserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.users.get(uuid).getStats().clear(); //TODO check if this actually works o.o
    }

}
