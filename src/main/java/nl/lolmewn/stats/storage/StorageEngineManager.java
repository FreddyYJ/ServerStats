package nl.lolmewn.stats.storage;

import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.api.storage.StorageEngine;

/**
 *
 * @author Lolmewn
 */
public class StorageEngineManager {

    private final Map<String, StorageEngine> storageEngines = new HashMap<>();

    public void addStorageEngine(String name, StorageEngine engine) {
        this.storageEngines.put(name.toLowerCase(), engine);
    }

    public StorageEngine getStorageEngine(String name) {
        return storageEngines.get(name.toLowerCase());
    }

    public boolean hasStorageEngine(String name) {
        return storageEngines.containsKey(name.toLowerCase());
    }

}
