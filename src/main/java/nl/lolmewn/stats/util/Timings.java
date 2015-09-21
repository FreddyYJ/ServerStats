package nl.lolmewn.stats.util;

import java.util.HashMap;

/**
 *
 * @author Lolmewn
 */
public class Timings {

    private static boolean enabled = false;
    private final static HashMap<String, Long> TIMINGS = new HashMap<>();

    public static void startTiming(String name, long nanos) {
        if (!enabled) {
            return;
        }
        TIMINGS.put(name, nanos);
    }

    public static long finishTimings(String name, long nanos) {
        if (!enabled) {
            return -1;
        }
        return nanos - TIMINGS.remove(name);
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

}
