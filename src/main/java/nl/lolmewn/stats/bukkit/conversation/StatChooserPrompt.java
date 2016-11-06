package nl.lolmewn.stats.bukkit.conversation;

import java.util.Optional;
import java.util.stream.Collectors;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.util.Util;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;

/**
 *
 * @author Lolmewn
 */
public abstract class StatChooserPrompt extends FixedSetPrompt {

    private final StatManager statManager;
    private final Optional<Prompt> nextPrompt;

    public StatChooserPrompt(StatManager statManager, Prompt nextPrompt) {
        super(statManager.getStats().stream().map(stat -> stat.getName()).collect(Collectors.toList()).toArray(new String[statManager.getStats().size()]));
        this.statManager = statManager;
        this.nextPrompt = Optional.ofNullable(nextPrompt);
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        Stat stat = Util.findStat(statManager, input);
        accept(stat);
        return nextPrompt.orElse(END_OF_CONVERSATION);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        // TODO
        return "";
    }

    public abstract void accept(Stat stat);

}
