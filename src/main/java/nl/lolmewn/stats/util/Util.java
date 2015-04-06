package nl.lolmewn.stats.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;

/**
 *
 * @author Lolmewn
 */
public class Util {

    public static Stat findStat(StatManager haystack, String needle) {
        for (Stat stat : haystack.getStats()) {
            if (stat.getName().equalsIgnoreCase(needle)) {
                return stat;
            }
        }
        for (Stat stat : haystack.getStats()) {
            if (stat.getName().toLowerCase().startsWith(needle.toLowerCase())) {
                return stat;
            }
        }
        return null;
    }

    public static String getDefaultMessage(Stat stat, StatEntry entry) {
        return stat.getName()
                + (entry.getMetadata().containsKey("world")
                        ? "in " + entry.getMetadata().get("world").toString()
                        : "")
                + ": " + entry.getValue();

    }

    public static List<Pair<String, ?>> getSafePairs(Stat stat, StatEntry entry) {
        List<Pair<String, ?>> list = new ArrayList<>();
        for (Entry<String, Object> metadata : entry.getMetadata().entrySet()) {
            if (metadata.getValue() == null) {
                continue;
            }
            list.add(new Pair(metadata.getKey(), metadata.getValue()));
        }
        list.add(new Pair<>("%value%", entry.getValue()));
        return list;
    }

    public static List<Pair<String, ?>> removePair(List<Pair<String, ?>> pairs, String key) {
        Iterator<Pair<String, ?>> it = pairs.iterator();
        while (it.hasNext()) {
            if (it.next().getKey().equals(key)) {
                it.remove();
            }
        }
        return pairs;
    }

}
