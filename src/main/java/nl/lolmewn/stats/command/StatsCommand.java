package nl.lolmewn.stats.command;

import java.util.Arrays;
import java.util.HashMap;
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
        this.subCommands.put("reset", new StatsResetCommand(plugin));
        this.subCommands.put("root", new StatsRootCommand(plugin));
        this.subCommands.put("player", new StatsPlayerCommand(plugin));
        this.subCommands.put("stat", new StatsStatCommand(plugin));
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

}
