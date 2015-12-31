package nl.lolmewn.stats.command;

import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.util.Util;

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
    public void execute(Dispatcher sender, String[] args) {
        if(args.length == 0){
            sender.sendMessage(Messages.getMessage("needs-more-arguments", new Pair("%usage%", "/stats stat <statname>")));
            return;
        }
        String statName = args[0];
        Stat stat = Util.findStat(plugin.getStatManager(), statName);
        if(stat == null){
            sender.sendMessage(Messages.getMessage("stat-not-found"));
            return;
        }
        StatsHolder holder = plugin.getUserManager().getUser((sender).getUniqueId());
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
