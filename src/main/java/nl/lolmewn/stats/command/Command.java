package nl.lolmewn.stats.command;

/**
 *
 * @author Lolmewn
 */
public abstract class Command {
    
    public abstract void handleCommand(Dispatcher sender, String[] args);

}
