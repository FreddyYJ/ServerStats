package nl.lolmewn.stats.bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import nl.lolmewn.stats.Config;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Lolmewn
 */
public class BukkitMessages implements Config {

    private final YamlConfiguration config;

    public BukkitMessages(Plugin plugin) throws IOException {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", true);
            config = YamlConfiguration.loadConfiguration(file);
        } else {
            config = YamlConfiguration.loadConfiguration(file);
            YamlConfiguration jar = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml")));
            jar.getKeys(true).stream().filter((path) -> (!config.contains(path.toLowerCase()))).map((path) -> {
                config.set(path.toLowerCase(), config.contains(path) ? config.get(path) : jar.get(path));
                return path;
            }).forEach((path) -> {
                config.set(path, null);
            });
            config.save(file);
        }
    }

    @Override
    public String getString(String key) {
        return config.getString(key);
    }

    @Override
    public String getString(String key, String def) {
        return config.getString(key, def);
    }

    @Override
    public int getInteger(String key) {
        return config.getInt(key);
    }

    @Override
    public int getInteger(String key, int def) {
        return config.getInt(key, def);
    }

    @Override
    public List<String> getStringList(String key) {
        return config.getStringList(key);
    }

    @Override
    public boolean hasPath(String path) {
        return config.contains(path);
    }

}
