package nl.lolmewn.stats.command;

import nl.lolmewn.stats.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public abstract class SubCommand {
    
    public abstract void execute(CommandSender sender, String[] args);
    
    public abstract boolean consoleOnly();
    public abstract boolean playerOnly();
    public abstract String getPermissionNode();
    
    public boolean onCommand(CommandSender sender, String[] args){
        if(consoleOnly() && !(sender instanceof ConsoleCommandSender)){
            sender.sendMessage(Messages.getMessage("console-only"));
            return true;
        }
        if(playerOnly() && !(sender instanceof Player)){
            sender.sendMessage(Messages.getMessage("player-only"));
            return true;
        }
        if(getPermissionNode() != null && !sender.hasPermission(getPermissionNode())){
            sender.sendMessage(Messages.getMessage("no-perms"));
            return true;
        }
        execute(sender, args);
        return true;
    }

}
