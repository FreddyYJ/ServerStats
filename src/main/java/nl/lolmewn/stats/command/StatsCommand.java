package nl.lolmewn.stats.command;

import java.util.Arrays;
import java.util.HashMap;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.bukkit.BukkitMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Lolmewn
 */
public class StatsCommand implements CommandExecutor {
    
    private final BukkitMain plugin;
    private final HashMap<String, SubCommand> subCommands;

    public StatsCommand(BukkitMain plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
        this.subCommands.put("reset", new StatsResetCommand(plugin));
        this.subCommands.put("root", new StatsRootCommand(plugin));
        this.subCommands.put("player", new StatsPlayerCommand(plugin));
        this.subCommands.put("stat", new StatsStatCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] args) {
        if(args.length == 0){
            return subCommands.get("root").onCommand(sender, args);
        }
        if(subCommands.containsKey(args[0].toLowerCase())){
            return subCommands.get(args[0].toLowerCase()).onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        }
        sender.sendMessage(Messages.getMessage("command-not-found"));
        return true;
    }

}
