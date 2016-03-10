package nl.lolmewn.stats.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.stat.MetadataPair;

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
        return entries.stream().mapToDouble((entry) -> entry.getValue()).sum();
    }

    public static double sumWhere(Collection<StatEntry> entries, MetadataPair... matchers) {
        if (entries == null || matchers.length == 0) {
            return sumAll(entries);
        }
        double value = 0;
        for (StatEntry entry : entries) {
            if(Arrays.asList(matchers).stream().allMatch(pair -> {return matches(entry, pair);})){
                value += entry.getValue();
            }
        }
        return value;
    }

    public static boolean matches(StatEntry entry, MetadataPair pair) {
        if (entry == null || pair == null) {
            return pair != null; // returns true if pair is not null
        }
        if (!entry.getMetadata().containsKey(pair.getKey())) {
            return false;
        }
        return entry.getMetadata().get(pair.getKey()).equals(pair.getValue());
    }
    
    public static boolean matchesAll(StatEntry entry, MetadataPair... pairs){
        return Arrays.asList(pairs).stream().allMatch(pair -> Util.matches(entry, pair));
    }
    
    public static boolean matchesAny(StatEntry entry, MetadataPair... pairs){
        return Arrays.asList(pairs).stream().anyMatch(pair -> Util.matches(entry, pair));
    }
    
    public static boolean matchesNone(StatEntry entry, MetadataPair... pairs){
        return Arrays.asList(pairs).stream().noneMatch(pair -> Util.matches(entry, pair));
    }
    
    public static boolean matchesAll(StatEntry entry, List<MetadataPair> pairs){
        return pairs.stream().allMatch(pair -> Util.matches(entry, pair));
    }
    
    public static boolean matchesAny(StatEntry entry, List<MetadataPair> pairs){
        return pairs.stream().anyMatch(pair -> Util.matches(entry, pair));
    }
    
    public static boolean matchesNone(StatEntry entry, List<MetadataPair> pairs){
        return pairs.stream().noneMatch(pair -> Util.matches(entry, pair));
    }

}
