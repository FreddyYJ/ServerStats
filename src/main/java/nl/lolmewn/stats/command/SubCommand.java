package nl.lolmewn.stats.command;

import nl.lolmewn.stats.Messages;

/**
 *
 * @author Lolmewn
 */
public abstract class SubCommand {
    
    public abstract void execute(Dispatcher sender, String[] args);
    
    public abstract boolean consoleOnly();
    public abstract boolean playerOnly();
    public abstract String getPermissionNode();
    
    public boolean onCommand(Dispatcher sender, String[] args){
        if(consoleOnly() && !sender.isConsole()){
            sender.sendMessage(Messages.getMessage("console-only"));
            return true;
        }
        if(playerOnly() && !sender.isPlayer()){
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
