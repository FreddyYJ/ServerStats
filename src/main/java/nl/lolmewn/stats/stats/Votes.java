package nl.lolmewn.stats.stats;

import java.util.HashMap;
import java.util.Map;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;

/**
 *
 * @author Lolmewn
 */
public class Votes extends DefaultStat {

    @Override
    public String format(StatEntry entry) {
        return "<//TODO>";
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<>(0);
    }

    @Override
    public String getName() {
        return "Votes";
    }

}
