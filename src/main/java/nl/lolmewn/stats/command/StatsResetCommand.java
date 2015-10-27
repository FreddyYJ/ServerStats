package nl.lolmewn.stats.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public class StatsResetCommand extends SubCommand {

    private final BukkitMain plugin;
    private final HashMap<String, List<UUID>> toReset = new HashMap<>();

    public StatsResetCommand(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Dispatcher sender, String[] args) {
        if (toReset.containsKey(sender.getName())) {
            //confirmed
            List<UUID> list = toReset.get(sender.getName());
            list.stream().forEach((uuid) -> {
                plugin.getUserManager().resetUser(uuid);
            });
            sender.sendMessage(Messages.getMessage("commands.reset.success"));
            toReset.remove(sender.getName());
            return;
        }
        if (args.length == 0) {
            // self 
            if (!sender.hasPermission("stats.reset.self")) {
                sender.sendMessage(Messages.getMessage("no-perms"));
                return;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(Messages.getMessage("player-only"));
                return;
            }
            final Player player = (Player) sender;
            sender.sendMessage(Messages.getMessage("commands.reset.confirm", new Pair("%target%", Messages.getMessage("commands.reset.target-self"))));
            toReset.put(player.getName(), new ArrayList<UUID>() {
                {
                    this.add(player.getUniqueId());
                }
            });
            return;
        }
        if (args[0].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("stats.reset.all")) {
                sender.sendMessage(Messages.getMessage("no-perms"));
                return;
            }
            sender.sendMessage(Messages.getMessage("commands.reset.confirm", new Pair("%target%", Messages.getMessage("commands.reset.target-all"))));
            List<UUID> list = new ArrayList<>();
            for (OfflinePlayer op : plugin.getServer().getOfflinePlayers()) {
                list.add(op.getUniqueId());
            }
            toReset.put(sender.getName(), list);
            return;
        }
        if (!sender.hasPermission("stats.reset.other")) {
            sender.sendMessage(Messages.getMessage("no-perms"));
            return;
        }
        List<UUID> list = new ArrayList<>(args.length);
        for (String arg : args) {
            OfflinePlayer player = this.plugin.getServer().getPlayer(arg);
            if (player == null) {
                player = this.plugin.getServer().getOfflinePlayer(arg);
            }
            if (player == null || !player.hasPlayedBefore()) {
                sender.sendMessage(Messages.getMessage("player-not-found", new Pair("%input%", arg)));
                continue; // Really can't find him
            }
            list.add(player.getUniqueId());
        }
        sender.sendMessage(Messages.getMessage("commands.reset.confirm", new Pair("%target%", list.size() + "")));
        toReset.put(sender.getName(), list);
    }

    @Override
    public boolean consoleOnly() {
        return false;
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String getPermissionNode() {
        return "stats.reset"; // let command handle it
    }

}
