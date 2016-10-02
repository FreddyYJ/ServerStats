package nl.lolmewn.stats;

import java.util.Set;

/**
 *
 * @author Lolmewn
 */
public class MessageParser {

    private static Config config;

    public MessageParser(Config config) {
        MessageParser.config = config;
    }

    public static String parse(String message, ConditionalStatEntry entry) {
        if (config == null) {
            System.err.println("Warning: Message parser was called before init was done");
            return message; // avoid NPE
        }
        // Let's see what conditions we have, and apply some parsing on that
        Set<Condition> conditions = entry.getConditions();
        for (Condition cond : conditions) {
            if (config.hasPath("metadata." + cond.getName())) {
                // Ooh, we can do stuff
                if (message.contains("%" + cond.getName() + "%")) {
                    message = message.replace("%" + cond.getName() + "%", getAddition(cond));
                } else {
                    message += (getAddition(cond));
                }
            }
        }
        return message;
    }

    private static String getAddition(Condition cond) {
        if (cond.getMatches().size() > 1) {
            // multiple!
            return config.getString("metadata." + cond.getName() + ".values", config.getString("metadata." + cond.getName() + ".value", "%" + cond.getName() + "%"));
        } else {
            return config.getString("metadata." + cond.getName() + ".value", "%" + cond.getName() + "%");
        }
    }

}
