package nl.lolmewn.stats.stats;

import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;

/**
 *
 * @author Lolmewn
 */
public class Move extends DefaultStat {

    @Override
    public String format(StatEntry entry) {
        return "<//TODO>";
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<String, DataType>() {
            {
                this.put("type", DataType.INTEGER);
                this.put("world", DataType.STRING);
            }
        };
    }

    @Override
    public String getName() {
        return "Move";
    }

}
