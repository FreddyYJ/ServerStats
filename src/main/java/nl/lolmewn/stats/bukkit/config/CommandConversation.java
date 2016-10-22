package nl.lolmewn.stats.bukkit.config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import mkremins.fanciful.FancyMessage;
import nl.lolmewn.itemmanager.inv.ManagedInventory;
import nl.lolmewn.stats.Condition;
import nl.lolmewn.stats.Condition.MatchingMode;
import nl.lolmewn.stats.api.storage.DataType;
import nl.lolmewn.stats.bukkit.BukkitMain;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

/**
 * This class starts a conversation with a Player for setting up metadata for a
 * stat to be viewed using /stats.
 *
 * @author Lolmewn
 */
public class CommandConversation {

    private final BukkitMain plugin;
    private final StatDescriptor desc;
    private final CommandConfig config;
    private final String condName;
    private final Player player;
    private final ManagedInventory inventory;

    public CommandConversation(
            BukkitMain plugin,
            Player player,
            StatDescriptor descriptor,
            String condName,
            ManagedInventory previous,
            CommandConfig config) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.desc = descriptor;
        this.condName = condName;
        this.inventory = previous;
        Conversation conv = new ConversationFactory(plugin)
                .withEscapeSequence("/quit")
                .withFirstPrompt(new MainMenuPrompt())
                .withLocalEcho(false)
                .withModality(true)
                .buildConversation(player);
        conv.addConversationAbandonedListener((ConversationAbandonedEvent unused) -> {
            if (player.isOnline()) {
                inventory.open(player);
            }
        });
        conv.begin();
        plugin.debug("Opening conversation for " + player.getName() + " for stat " + desc.getStat().getName() + " and cond " + condName);
    }

    private class MainMenuPrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.DARK_AQUA + "You are editing the " + ChatColor.DARK_RED + condName + ChatColor.DARK_AQUA + " condition\n"
                    + ChatColor.DARK_AQUA + "To leave this prompt, use " + ChatColor.GOLD + "/quit\n"
                    + ChatColor.DARK_AQUA + "This condition is of type "
                    + ChatColor.DARK_RED + desc.getStat().getDataTypes().getOrDefault(condName, DataType.STRING).toString().toLowerCase() + "\n";
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new InputPrompt();
        }

    }

    private class InputPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            new FancyMessage("Click here").color(ChatColor.GOLD).style(ChatColor.UNDERLINE).command("disable").then()
                    .text(" to disable this condition or ").color(ChatColor.DARK_AQUA).then()
                    .text("click here").color(ChatColor.GOLD).style(ChatColor.UNDERLINE).command("quit").then()
                    .text(" to go back to the " + desc.getStat().getName() + " inventory").color(ChatColor.DARK_AQUA).send(player);
            return ChatColor.DARK_AQUA + "Please input the condition line (see "
                    + ChatColor.GOLD + ChatColor.UNDERLINE + "http://bit.ly/s3conf" + ChatColor.RESET + ChatColor.DARK_AQUA + " for help)";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("disable")) {
                // Sure thing.
                StatDescriptor old = new StatDescriptor(desc.getStat(), desc.getConditions().toArray(new Condition[desc.getConditions().size()]));
                removeCondition(condName, true);
                config.reloadStat(old, desc);
                return END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("quit")) {
                return END_OF_CONVERSATION;
            }
            // validate cond
            Condition parsed = Condition.parse(condName + "=" + input);
            if (parsed == null) {
                player.sendMessage(ChatColor.RED + "Could not parse your condition! Please try again.");
                return this;
            } else {
                // wowe, a new condition. Better save it!
                StatDescriptor old = new StatDescriptor(desc.getStat(), desc.getConditions().toArray(new Condition[desc.getConditions().size()]));
                addOrEditCondition(condName, parsed);
                config.reloadStat(old, desc);
                return END_OF_CONVERSATION;
            }
        }

        private void addOrEditCondition(String oldName, Condition newer) {
            StatDescriptor old = new StatDescriptor(desc.getStat(), desc.getConditions().toArray(new Condition[desc.getConditions().size()]));
            removeCondition(oldName, false);
            desc.addCondition(newer);
            saveStatDescriptor(desc, old);
        }

        private void removeCondition(String condName, boolean save) {
            Optional<Condition> cond = desc.getConditions().stream().filter(c -> c.getName().equals(condName)).findAny();
            if (!cond.isPresent()) {
                return; // Don't have to check save either since there is no change.
            }
            StatDescriptor old = new StatDescriptor(desc.getStat(), desc.getConditions().toArray(new Condition[desc.getConditions().size()]));
            desc.getConditions().remove(cond.get());
            if (save) {
                saveStatDescriptor(desc, old);
            }
        }

        private void saveStatDescriptor(StatDescriptor desc, StatDescriptor old) {
            String line = generateLine(desc);
            List<String> showList = plugin.getConfig().getStringList("statsCommand.show");
            List<String> list = showList.stream().filter(str -> !StatDescriptor.parse(str, plugin.getStatManager()).equals(old)).collect(Collectors.toList());
            list.add(line);
            plugin.getConfig().set("statsCommand.show", list);
            plugin.saveConfig();
        }

        private String generateLine(StatDescriptor desc) {
            StringBuilder sb = new StringBuilder();
            sb.append(desc.getStat().getName());
            desc.getConditions().stream().forEach((cond) -> {
                sb.append(",").append(cond.getName()).append("=");
                MatchingMode mode = cond.getMode();
                cond.getMatches().stream().forEach(match -> {
                    if (!sb.toString().endsWith("=")) {
                        // not first
                        sb.append("|");
                    }
                    if (mode == MatchingMode.EXCLUDING) {
                        sb.append("!");
                    }
                    sb.append(match);
                });
            });
            return sb.toString();
        }

    }

}
