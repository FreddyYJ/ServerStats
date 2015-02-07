package nl.lolmewn.stats.util;

import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;

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

}
