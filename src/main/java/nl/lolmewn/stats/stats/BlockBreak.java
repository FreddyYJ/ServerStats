package nl.lolmewn.stats.stats;

import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;

/**
 *
 * @author Lolmewn
 */
public class BlockBreak extends DefaultStat {

    @Override
    public String format(StatEntry entry) {
        return "<//todo>";
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<String, DataType>(){{
            this.put("world", DataType.STRING);
            this.put("name", DataType.STRING);
        }};
    }

    @Override
    public String getName() {
        return "Blocks broken";
    }

}
