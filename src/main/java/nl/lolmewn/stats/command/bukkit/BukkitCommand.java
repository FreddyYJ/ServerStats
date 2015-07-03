package nl.lolmewn.stats.command.bukkit;

import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.command.StatsCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Lolmewn
 */
public class BukkitCommand extends StatsCommand implements CommandExecutor {

    public BukkitCommand(BukkitMain plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        handleCommand(new BukkitDispatcher(sender), args);
        return true;
    }

}
