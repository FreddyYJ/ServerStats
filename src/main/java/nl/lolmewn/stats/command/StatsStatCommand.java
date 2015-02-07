package nl.lolmewn.stats.command;

import nl.lolmewn.stats.BukkitMain;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public class StatsStatCommand extends SubCommand{

    private final BukkitMain plugin;

    public StatsStatCommand(BukkitMain plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0){
            sender.sendMessage(Messages.getMessage("needs-more-arguments", new Pair("%usage%", "/stats stat <statname>")));
            return;
        }
        String statName = args[0];
        Stat stat = Util.findStat(plugin.getStatManager(), statName);
        StatsHolder holder = plugin.getUserManager().getUser(((Player)sender).getUniqueId());
    }

    @Override
    public boolean consoleOnly() {
        return false;
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String getPermissionNode() {
        return "stats.show";
    }

}
