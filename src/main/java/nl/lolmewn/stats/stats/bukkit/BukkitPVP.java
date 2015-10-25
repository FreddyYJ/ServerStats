package nl.lolmewn.stats.stats.bukkit;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.bukkit.BukkitUtil;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.stat.MetadataPair;
import nl.lolmewn.stats.stats.PVP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 *
 * @author Lolmewn
 */
public class BukkitPVP extends PVP implements Listener {

    private final BukkitMain plugin;

    public BukkitPVP(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public String format(StatEntry se) {
        return super.format(se).replace("%victim%", plugin.getServer().getOfflinePlayer((UUID) se.getMetadata().get("victim")).getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void pvp(PlayerDeathEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getEntity().hasMetadata("NPC")) {
            return;
        }
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            StatsHolder holder = this.plugin.getUserManager().getUser(killer.getUniqueId());
            if (killer.hasMetadata("NPC")) {
                return;
            }
            Player dead = event.getEntity();
            // This stat only tracks pvp
            holder.addEntry(this,
                    new DefaultStatEntry(
                            1,
                            new MetadataPair("weapon", BukkitUtil.getWeaponName(killer.getItemInHand())),
                            new MetadataPair("victim", dead.getUniqueId().toString()),
                            new MetadataPair("time", System.currentTimeMillis()),
                            new MetadataPair("world", killer.getWorld().getName())
                    )
            );
            streaks(killer, dead);
        }
    }

    public void streaks(Player killer, Player victim) {
        StatsHolder holder = this.plugin.getUserManager().getUser(killer.getUniqueId());
        Stat streak = plugin.getStatManager().getStat("PVP streak");
        Stat topStreak = plugin.getStatManager().getStat("PVP top streak");
        holder.addEntry(streak, new DefaultStatEntry(1,
                new MetadataPair("world", killer.getWorld().getName()))
        );
        int currentTopStreak = getCurrentStreak(holder, topStreak, killer.getWorld().getName());
        int currentStreak = getCurrentStreak(holder, streak, killer.getWorld().getName());
        if (currentTopStreak < currentStreak) {
            holder.removeStat(topStreak);
            holder.addEntry(topStreak, new DefaultStatEntry(currentStreak, new MetadataPair("world", killer.getWorld().getName())));
        }

        StatsHolder dead = this.plugin.getUserManager().getUser(victim.getUniqueId());
        dead.removeStat(streak);
    }

    private int getCurrentStreak(StatsHolder holder, Stat stat, final String name) {
        if (!holder.hasStat(stat)) {
            return 0;
        }
        return holder.getStats(stat).stream().filter(new Predicate<StatEntry>() {
            @Override
            public boolean test(StatEntry t) {
                return t.getMetadata().containsKey("world") && t.getMetadata().get("world").equals(name);
            }
        }).mapToInt(new ToIntFunction<StatEntry>() {
            @Override
            public int applyAsInt(StatEntry value) {
                return (int) value.getValue();
            }
        }).sum();
    }

}
