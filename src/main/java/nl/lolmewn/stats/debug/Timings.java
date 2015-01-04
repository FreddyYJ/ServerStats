package nl.lolmewn.stats.debug;

import java.util.HashMap;

/**
 *
 * @author Lolmewn
 */
public class Timings {

    private static boolean enabled = false;
    private final static HashMap<String, Long> timings = new HashMap<>();

    public static void startTiming(String name, long nanos) {
        if (!enabled) {
            return;
        }
        if (timings.containsKey(name)) {
            throw new IllegalStateException("Timings already started for " + name);
        }
        timings.put(name, nanos);
    }

    public static long finishTimings(String name, long nanos) {
        if (!enabled) {
            return -1;
        }
        if (!timings.containsKey(name)) {
            throw new IllegalStateException("Timings were not started for " + name);
        }
        return nanos - timings.remove(name);
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

}
