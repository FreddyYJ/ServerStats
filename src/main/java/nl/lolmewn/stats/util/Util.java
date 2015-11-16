package nl.lolmewn.stats.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
        return "%value%"
                + (entry.getMetadata().containsKey("world")
                ? " in world %world%"
                : "");

    }

    public static List<Pair<String, ?>> getSafePairs(Stat stat, StatEntry entry) {
        return getSafePairs(entry);
    }

    public static List<Pair<String, ?>> getSafePairs(StatEntry entry) {
        List<Pair<String, ?>> list = new ArrayList<>();
        entry.getMetadata().entrySet().stream().filter((metadata) -> metadata.getValue() != null).forEach((metadata) -> {
            list.add(new Pair("%" + metadata.getKey() + "%", metadata.getValue()));
        });
        list.add(new Pair<>("%value%", entry.getValue()));
        return list;
    }

    public static Collection<Pair<String, ?>> removePair(Collection<Pair<String, ?>> pairs, String key) {
        Iterator<Pair<String, ?>> it = pairs.iterator();
        while (it.hasNext()) {
            if (it.next().getKey().equals(key)) {
                it.remove();
            }
        }
        return pairs;
    }

    public static double sumAll(Collection<StatEntry> entries) {
        if (entries == null) {
            return 0;
        }
        double sum = 0;
        sum = entries.stream().map((entry) -> entry.getValue()).reduce(sum, (accumulator, _item) -> accumulator + _item);
        return sum;
    }

}
