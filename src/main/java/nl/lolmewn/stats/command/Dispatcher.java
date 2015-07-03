package nl.lolmewn.stats.command;

import java.util.UUID;

/**
 *
 * @author Lolmewn
 */
public interface Dispatcher {

    public boolean hasPermission(String node);

    public void sendMessage(String message);

    public boolean isPlayer();
    
    public boolean isConsole();

    public UUID getUniqueId();

    public String getName();

}
