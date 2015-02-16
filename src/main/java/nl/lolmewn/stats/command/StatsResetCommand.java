package nl.lolmewn.stats.command;

import nl.lolmewn.stats.BukkitMain;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.api.user.StatsHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public class StatsResetCommand extends SubCommand{
    
    private final BukkitMain plugin;

    public StatsResetCommand(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0){
            // self 
            if(!sender.hasPermission("stats.reset.self")){
                sender.sendMessage(Messages.getMessage("no-perms"));
                return;
            }
            if(!(sender instanceof Player)){
                sender.sendMessage(Messages.getMessage("player-only"));
                return;
            }
            Player player = (Player)sender;
            StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
            holder.getStats().clear();
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
        return null; // let command handle it
    }

}
