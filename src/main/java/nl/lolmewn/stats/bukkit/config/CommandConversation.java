package nl.lolmewn.stats.bukkit.config;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * This class starts a conversation with a Player for setting up metadata for a
 * stat to be viewed using /stats.
 *
 * @author Lolmewn
 */
public class CommandConversation {

    private final StatDescriptor desc;

    public CommandConversation(Plugin plugin, Player player, StatDescriptor descriptor) {
        this.desc = descriptor;
        new ConversationFactory(plugin)
                .withEscapeSequence("quit")
                .withFirstPrompt(new MainMenuPrompt())
                .withLocalEcho(false)
                .withModality(true).buildConversation(player);
    }

    private class MainMenuPrompt extends FixedSetPrompt {

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            return Prompt.END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return "To-implement";
        }

    }

}
