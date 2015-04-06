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
public class DamageTaken extends DefaultStat {

    @Override
    public String format(StatEntry entry) {
        return Messages.getMessage(
                getMessagesRootPath() + ".format", 
                Util.getDefaultMessage(this, entry), 
                Util.getSafePairs(this, entry));
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<String, DataType>() {
            {
                this.put("world", DataType.STRING);
                this.put("cause", DataType.STRING);
            }
        };
    }

    @Override
    public String getName() {
        return "Damage taken";
    }

}
