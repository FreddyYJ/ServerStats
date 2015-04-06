package nl.lolmewn.stats;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Sybren
 */
public class Messages {

    private static Config config;
    private static Painter painter;

    public Messages(Config config, Painter painter) throws IOException {
        Messages.config = config;
        Messages.painter = painter;
    }

    public static String getMessage(String path) {
        return getMessage(path, "&cCouldn't find message (path: " + path + ")");
    }

    public static String getMessage(String path, String def) {
        return colorise(config.getString(path, def));
    }

    public static String getMessage(String path, Pair<String, ?>... replace) {
        return getMessage(path, "&cCouldn't find message (path: " + path + ")", replace);
    }

    public static String getMessage(String path, List<Pair<String, ?>> replace) {
        return getMessage(path, "&cCouldn't find message (path: " + path + ")", replace);
    }

    public static String getMessage(String path, String def, Pair<String, ?>... replace) {
        String msg = config.getString(path, def);
        for (Pair<String, ?> pair : replace) {
            msg = msg.replace(pair.getKey(), pair.getValue().toString());
        }
        return colorise(msg);
    }

    public static String getMessage(String path, String def, List<Pair<String, ?>> replace) {
        String msg = config.getString(path, def);
        for (Pair<String, ?> pair : replace) {
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

    public static List<String> getMessages(String path, Pair<String, ?>... replace) {
        List<String> msgs = config.getStringList(path);
        if (msgs.isEmpty()) {
            msgs.add("&cCouldn't find message (path: " + path + ")");
            return msgs;
        }
        List<String> re = new LinkedList<>();
        for (String message : msgs) {
            for (Pair<String, ?> pair : replace) {
                message = message.replace(pair.getKey(), pair.getValue().toString());
            }
            re.add(colorise(message));

        }
        return re;
    }

    public static String colorise(String message) {
        return painter.convertColorCodes(message);
    }

}
