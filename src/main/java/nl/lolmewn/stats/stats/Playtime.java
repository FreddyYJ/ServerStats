package nl.lolmewn.stats.stats;

import java.util.concurrent.TimeUnit;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.StatEntry;

/**
 *
 * @author Lolmewn
 */
public class Playtime extends SimpleStat {

    public Playtime() {
        super("Playtime");
    }

    @Override
    public String format(StatEntry entry) {
        long seconds = (long) entry.getValue();
        int days = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (days * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long secondsFormatted = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
        return Messages.getMessage(this.getMessagesRootPath() + ".format",
                new Pair("%days%", days + ""),
                new Pair("%hours%", hours + ""),
                new Pair("%minutes%", minutes + ""),
                new Pair("%seconds%", secondsFormatted + ""),
                new Pair("%world%", entry.getMetadata().get("world").toString())
        );
    }

}
