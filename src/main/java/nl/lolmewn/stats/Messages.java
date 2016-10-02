package nl.lolmewn.stats;

import java.io.IOException;
import java.util.Arrays;
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
        new MessageParser(config);
    }

    public static Config getConfig() {
        return config;
    }

    public static Painter getPainter() {
        return painter;
    }

    public static boolean hasMessage(String path) {
        return config.hasPath(path) && !config.getString(path).trim().isEmpty();
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
        return getMessage(path, def, Arrays.asList(replace));
    }

    public static String replace(String message, Pair<String, ?> value) {
        Object val = value.getValue();
        if (val instanceof Double && ((Double) val) == (long) ((Double) val).doubleValue()) {
            return message.replace(value.getKey(), ((Double) val).longValue() + "");
        }
        return message.replace(value.getKey(), value.getValue().toString());
    }

    public static String replace(String message, List<Pair<String, ?>> values) {
        for (Pair<String, ?> pair : values) {
            message = replace(message, pair);
        }
        return message;
    }

    public static String getMessage(String path, String def, List<Pair<String, ?>> replace) {
        String msg = config.getString(path, def);
        msg = replace(msg, replace);
        return colorise(msg);

    }

    public static List<String> getMessages(String path) {
        List<String> msgs = config.getStringList(path);
        List<String> re = new LinkedList<>();
        msgs.stream().forEach((msg) -> {
            re.add(colorise(msg));
        });
        return re;
    }

    public static List<String> getMessages(String path, Pair<String, ?>... replace) {
        List<String> msgs = config.getStringList(path);
        if (msgs.isEmpty()) {
            msgs.add("&cCouldn't find message (path: " + path + ")");
            return msgs;
        }
        List<String> re = new LinkedList<>();
        msgs.stream().map((message) -> {
            message = replace(message, Arrays.asList(replace));
            return message;
        }).forEach((message) -> {
            re.add(colorise(message));
        });
        return re;
    }

    public static String colorise(String message) {
        return painter.convertColorCodes(message);
    }

}
