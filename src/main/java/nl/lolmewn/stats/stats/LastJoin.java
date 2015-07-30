package nl.lolmewn.stats.stats;

import java.text.DateFormat;
import java.util.Date;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.util.Util;

/**
 *
 * @author Lolmewn
 */
public class LastJoin extends SimpleStat {

    public LastJoin() {
        super("Last join");
    }

    @Override
    public String format(StatEntry entry) {
        return Messages.getMessage(
                getMessagesRootPath() + ".format",
                Util.getDefaultMessage(this, entry),
                new Pair<>(
                        "%value%",
                        DateFormat.getDateTimeInstance().format(
                                new Date(
                                        (long) entry.getValue()
                                )
                        )
                )
        );
    }

    @Override
    public boolean isSummable() {
        return false;
    }

}
