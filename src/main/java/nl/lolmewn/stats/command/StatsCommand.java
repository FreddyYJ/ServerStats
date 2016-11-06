package nl.lolmewn.stats.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.bukkit.BukkitMain;

/**
 *
 * @author Lolmewn
 */
public class StatsCommand extends Command {
    
    private final HashMap<String, SubCommand> subCommands;
    
    public StatsCommand(BukkitMain plugin) {
        this.subCommands = new HashMap<>();
        
        this.subCommands.put("admin", new StatsAdminCommand(plugin));
        this.subCommands.put("debug", new StatsDebugCommand(plugin));
        
        this.subCommands.put("add", new StatsAddCommand(plugin));
        this.subCommands.put("create", new StatsCreateCommand(plugin));
        this.subCommands.put("set", new StatsSetCommand(plugin));
        
        this.subCommands.put("reset", new StatsResetCommand(plugin));
        this.subCommands.put("root", new StatsRootCommand(plugin));
        this.subCommands.put("player", new StatsPlayerCommand(plugin));
        this.subCommands.put("stat", new StatsStatCommand(plugin));
        this.subCommands.put("help", new StatsHelpCommand(this));
    }
    
    @Override
    public void handleCommand(Dispatcher sender, String[] args) {
        if (args.length == 0) {
            subCommands.get("root").onCommand(sender, args);
            return;
        }
        if (subCommands.containsKey(args[0].toLowerCase())) {
            subCommands.get(args[0].toLowerCase()).onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        sender.sendMessage(Messages.getMessage("command-not-found"));
    }
    
    public Set<Entry<String, SubCommand>> getSubCommands() {
        return this.subCommands.entrySet();
    }
    
}
