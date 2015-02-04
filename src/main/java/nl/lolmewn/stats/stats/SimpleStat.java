package nl.lolmewn.stats.stats;

import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;

/**
 *
 * @author Lolmewn
 */
public abstract class SimpleStat extends DefaultStat {

    private final String name;

    public SimpleStat(String name) {
        this.name = name;
    }

    @Override
    public String format(StatEntry entry) {
        return Messages.getMessage(
                this.getMessagesRootPath() + "." + name,
                new Pair("world", entry.getMetadata().get("world").toString())
        );
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
