package nl.lolmewn.stats.command;

import java.util.UUID;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Lolmewn
 */
public class StatsAddCommand extends SubCommand {
    
    private final Main plugin;

    public StatsAddCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Dispatcher sender, String[] args) {
        if(args.length < 2){
            sender.sendMessage("Please specify a stat and a value and optionally a player.");
            sender.sendMessage("/stats add [player] <stat> <value>");
            sender.sendMessage("/stats add <stat> <value> (if [player] is you)");
            return;
        }
        UUID targetPlayer;
        int offset = -1;
        if(args.length == 2){
            // <stat> and <value>
            if(!sender.isPlayer()){
                sender.sendMessage("Can only perform this command as a player.");
                sender.sendMessage("Try /stats add <player> <stat> <value> instead");
                return;
            }
            targetPlayer = sender.getUniqueId();
        }else{
            OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
            if(op.isOnline()){
                targetPlayer = op.getUniqueId();
                offset = 0;
            }else{
                sender.sendMessage("Player " + args[0] + " could not be found; is he online?");
                return;
            }
        }
        
        StatsHolder holder = plugin.getUserManager().getUser(targetPlayer);
        if(holder == null){
            // shouldn't happen
            sender.sendMessage("This player does not have a Stats profile; Don't know what to do now.");
            sender.sendMessage("Please report this to the server owner and tell him exactly what you did to get this message.");
            return;
        }
        
        Stat stat = Util.findStat(plugin.getStatManager(), args[1 + offset]);
        if(stat == null){
            sender.sendMessage("Could not find stat " + args[1 + offset] + "; did you type it correctly?");
            return;
        }
        
        try{
            int value = Integer.parseInt(args[2 + offset]);
            holder.addEntry(stat, new DefaultStatEntry(value));
            sender.sendMessage("Added " + value + " to stat " + stat.getName());
        }catch(NumberFormatException ignored){
            sender.sendMessage("Value is not a number, can only add numbers!");
        }
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
        return "stats.add";
    }

}
