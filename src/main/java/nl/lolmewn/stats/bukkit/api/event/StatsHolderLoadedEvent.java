package nl.lolmewn.stats.bukkit.api.event;

import nl.lolmewn.stats.api.user.StatsHolder;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn
 */
public class StatsHolderLoadedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final StatsHolder holder;

    public StatsHolderLoadedEvent(StatsHolder holder) {
        this.holder = holder;
    }

    public StatsHolder getStatsHolder() {
        return holder;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
