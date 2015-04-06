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
public class BlockBreak extends DefaultStat {

    @Override
    public String format(StatEntry entry) {
        return Messages.getMessage(this.getMessagesRootPath() + ".format",
                new Pair("%world%", entry.getMetadata().get("world").toString()),
                new Pair("%name%", entry.getMetadata().get("name").toString().toLowerCase().replace("_", " ")),
                new Pair("%amount%", entry.getValue())
        // No data, since that's just a number and will mean nothing.
        );
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<String, DataType>() {
            {
                this.put("world", DataType.STRING);
                this.put("name", DataType.STRING);
                this.put("data", DataType.INTEGER); // Actually a byte, but oh well. Replace once a better system is in place
            }
        };
    }

    @Override
    public String getName() {
        return "Blocks broken";
    }

}
