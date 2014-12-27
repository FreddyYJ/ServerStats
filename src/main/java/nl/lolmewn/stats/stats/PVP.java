package nl.lolmewn.stats.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;

/**
 *
 * @author Lolmewn
 */
public class PVP extends DefaultStat {
    
    private final Main plugin;

    public PVP(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String format(StatEntry se) {
        return Messages.getMessage(this.getMessagesRootPath() + ".format",
                new Pair("%world%", se.getMetadata().get("world").toString()),
                new Pair("%victim%", plugin.getServer().getOfflinePlayer((UUID)se.getMetadata().get("victim")).getName()),
                new Pair("%weapon%", se.getMetadata().get("weapon").toString()),
                new Pair("%amount%", se.getValue() + "")
        );
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<String, DataType>(){{
            this.put("world", DataType.STRING);
            this.put("victim", DataType.STRING); // UUID of the victim
            this.put("time", DataType.TIMESTAMP); // Time and date of the murder
            this.put("weapon", DataType.STRING);
        }};
    }

    @Override
    public String getName() {
        return "PVP";
    }

}
