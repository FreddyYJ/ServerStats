package nl.lolmewn.stats.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.DataType;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public class Playtime extends DefaultStat {

    public Playtime(Main plugin) {
        schedulePlaytimeRecording(plugin);
    }

    @Override
    public String getName() {
        return "Playtime";
    }

    @Override
    public String format(StatEntry entry) {
        long seconds = (long)entry.getValue();
        int days = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (days * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long secondsFormatted = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
        return Messages.getMessage(this.getMessagesRootPath() + ".format", 
                new Pair("%days%", days + ""),
                new Pair("%hours%", hours + ""),
                new Pair("%minutes%", minutes + ""),
                new Pair("%seconds%", secondsFormatted + "")
        );
    }

    private void schedulePlaytimeRecording(final Main plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for(Player player : plugin.getServer().getOnlinePlayers()){
                    StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
                    holder.addEntry(
                            Playtime.this,
                            new DefaultStatEntry(
                                    Playtime.this,
                                    1,
                                    new MetadataPair(
                                            "world",
                                            player.getWorld().getName()
                                    )
                            )
                    );
                }
            }
        }, 0L, 20L);
    }

    @Override
    public Map<String, DataType> getDataTypes() {
        return new HashMap<String, DataType>(){{
            this.put("world", DataType.STRING);
        }};
    }

}
