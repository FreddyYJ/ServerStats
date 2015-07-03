package nl.lolmewn.stats.command.bukkit;

import java.util.UUID;
import nl.lolmewn.stats.command.Dispatcher;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public class BukkitDispatcher implements Dispatcher {

    private final CommandSender sender;

    public BukkitDispatcher(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean hasPermission(String node) {
        return sender.hasPermission(node);
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    /**
     * Returns the unique identifier belonging to the command sender.
     * Only returns a value if the sender is a player
     * @return 
     */
    @Override
    public UUID getUniqueId() {
        return isPlayer() ? ((Player) sender).getUniqueId() : null;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

}
