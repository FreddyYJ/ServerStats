package nl.lolmewn.stats;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * @author Sybren
 */
public class Messages {

    private static YamlConfiguration config;

    public Messages(Plugin plugin) throws IOException {
        File messageFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("messages.yml", true);
            Messages.config = YamlConfiguration.loadConfiguration(messageFile);
        } else {
            Messages.config = YamlConfiguration.loadConfiguration(messageFile);
            YamlConfiguration jar = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml")));
            for (String path : jar.getKeys(true)) {
                if (!config.contains(path)) {
                    config.set(path, jar.get(path));
                }
            }
            config.save(messageFile);
        }
    }

    public static String getMessage(String path) {
        return getMessage(path, "&cCouldn't find message (path: " + path + ")");
    }

    public static String getMessage(String path, String def) {
        return colorise(config.getString(path, def));
    }

    public static String getMessage(String path, Pair... replace) {
        return getMessage(path, "&cCouldn't find message (path: " + path + ")", replace);
    }

    public static String getMessage(String path, List<Pair<String, Object>> replace) {
        return getMessage(path, "&cCouldn't find message (path: " + path + ")", replace);
    }

    public static String getMessage(String path, String def, Pair<String, Object>... replace) {
        String msg = config.getString(path, def);
        for (Pair<String, Object> pair : replace) {
            msg = msg.replace(pair.getKey(), pair.getValue().toString());
        }
        return colorise(msg);
    }

    public static String getMessage(String path, String def, List<Pair<String, Object>> replace) {
        String msg = config.getString(path, def);
        for (Pair<String, Object> pair : replace) {
            msg = msg.replace(pair.getKey(), pair.getValue().toString());
        }
        return colorise(msg);

    }

    public static List<String> getMessages(String path) {
        List<String> msgs = config.getStringList(path);
        List<String> re = new LinkedList<>();
        for (String msg : msgs) {
            re.add(colorise(msg));
        }
        return re;
    }

    public static List<String> getMessages(String path, Pair... replace) {
        List<String> msgs = config.getStringList(path);
        if (msgs.isEmpty()) {
            msgs.add("&cCouldn't find message (path: " + path + ")");
            return msgs;
        }
        List<String> re = new LinkedList<>();
        for (String message : msgs) {
            for (Pair<String, Object> pair : replace) {
                message = message.replace(pair.getKey(), pair.getValue().toString());
            }
            re.add(colorise(message));

        }
        return re;
    }

    public static String colorise(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
