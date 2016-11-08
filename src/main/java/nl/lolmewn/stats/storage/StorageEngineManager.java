package nl.lolmewn.stats.storage;

import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.api.storage.StorageEngine;

/**
 *
 * @author Lolmewn
 */
public class StorageEngineManager {

    private static final StorageEngineManager INSTANCE;
    private final Map<String, StorageEngine> storageEngines = new HashMap<>();
    
    static{
        INSTANCE = new StorageEngineManager();
    }
    
    private StorageEngineManager(){}
    
    public static StorageEngineManager getInstance(){
        return INSTANCE;
    }

    public void addStorageEngine(String name, StorageEngine engine) {
        this.storageEngines.put(name.toLowerCase(), engine);
    }

    public StorageEngine getStorageEngine(String name) {
        return storageEngines.get(name.toLowerCase());
    }

    public boolean hasStorageEngine(String name) {
        return storageEngines.containsKey(name.toLowerCase());
    }

    public Map<String, StorageEngine> getStorageEngines() {
        return storageEngines;
    }

}
