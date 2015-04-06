package nl.lolmewn.stats.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;
import nl.lolmewn.stats.util.Util;

/**
 *
 * @author Lolmewn
 */
public class PVP extends DefaultStat {

    @Override
    public String format(StatEntry se) {
        List<Pair<String, ?>> pairs = Util.getSafePairs(this, se);
        Util.removePair(pairs, "time");
        return Messages.getMessage(this.getMessagesRootPath() + ".format",
                Util.getDefaultMessage(this, se),
                pairs
        );
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<String, DataType>() {
            {
                this.put("world", DataType.STRING);
                this.put("victim", DataType.STRING); // UUID of the victim
                this.put("time", DataType.TIMESTAMP); // Time and date of the murder
                this.put("weapon", DataType.STRING);
            }
        };
    }

    @Override
    public String getName() {
        return "PVP";
    }

}
