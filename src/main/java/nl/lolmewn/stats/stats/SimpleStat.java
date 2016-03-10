package nl.lolmewn.stats.stats;

import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;
import nl.lolmewn.stats.util.Util;

/**
 *
 * @author Lolmewn
 */
public class SimpleStat extends DefaultStat implements Summable {

    private final String name;

    public SimpleStat(String name) {
        this.name = name;
    }

    @Override
    public String format(StatEntry entry) {
        return Messages.getMessage(
                getMessagesRootPath() + ".format",
                Util.getDefaultMessage(this, entry),
                Util.getSafePairs(entry));
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<String, DataType>() {
            {
                this.put("world", DataType.STRING);
            }
        };
    }

    @Override
    public String getName() {
        return name;
    }

}
