package nl.lolmewn.stats.bukkit.api.event;

import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.user.StatsHolder;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn
 */
public class StatsHolderUpdateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final StatsHolder holder;
    private final Stat stat;
    private final StatEntry entry;
    private boolean cancelled = false;

    public StatsHolderUpdateEvent(StatsHolder holder, Stat stat, StatEntry entry) {
        this.holder = holder;
        this.stat = stat;
        this.entry = entry;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public StatsHolder getHolder() {
        return holder;
    }

    public StatEntry getEntry() {
        return entry;
    }

    public Stat getStat() {
        return stat;
    }

}
