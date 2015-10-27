package nl.lolmewn.stats.command;

import nl.lolmewn.stats.Messages;

/**
 * @author Sybren
 */
public class StatsHelpCommand extends SubCommand {
    
    private final StatsCommand comm;
    
    StatsHelpCommand(StatsCommand command) {
        this.comm = command;
    }
    
    @Override
    public void execute(Dispatcher sender, String[] args) {
        comm.getSubCommands().stream().forEach(ent -> {
            if (sender.hasPermission(ent.getValue().getPermissionNode())) {
                sender.sendMessage("/stats " + ent.getKey() + ": " + Messages.getMessage("commands.help-message." + ent.getKey()));
            }
        });
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
        return null;
    }
    
}
