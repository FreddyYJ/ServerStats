package nl.lolmewn.stats.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import mkremins.fanciful.FancyMessage;
import nl.lolmewn.stats.Condition;
import nl.lolmewn.stats.ConditionalStatEntry;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.bukkit.config.StatDescriptor;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.util.Timings;
import nl.lolmewn.stats.util.Util;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Lolmewn
 */
public class StatsRootCommand extends SubCommand {

    private final BukkitMain plugin;

    public StatsRootCommand(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Dispatcher sender, String[] args) {
        Timings.startTiming("cmd-root", System.nanoTime());
        StatsHolder holder = plugin.getUserManager().getUser(sender.getUniqueId());
        List<String> statsToShow = plugin.getConfig().getStringList("statsCommand.show");
        if (Messages.hasMessage("commands.root.header")) {
            sender.sendMessage(Messages.getMessage("commands.root.header"));
        }
        statsToShow.stream().forEach((statDesc) -> {
            show(sender, holder, statDesc);
        });
        if (Messages.hasMessage("commands.root.footer")) {
            sender.sendMessage(Messages.getMessage("commands.root.footer"));
        }
        plugin.debug("cmd-root: " + Timings.finishTimings("cmd-root", System.nanoTime()));
    }

    @Override
    public boolean consoleOnly() {
        return false;
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String getPermissionNode() {
        return "stats.view";
    }

    private void show(Dispatcher sender, StatsHolder holder, String statDesc) {
        StatDescriptor sd;
        if (statDesc.contains(",")) {
            String[] split = statDesc.split(",");
            String name = split[0];
            Stat stat = Util.findStat(plugin.getStatManager(), name);
            if (stat == null) {
                plugin.getLogger().warning("Incorrect stat specified, not found: '" + name + "'");
                return;
            }
            sd = new StatDescriptor(stat);
            for (int i = 1; i < split.length; i++) {
                String condDescription = split[i];
                Condition cond = Condition.parse(condDescription);
                sd.addCondition(cond);
            }
        } else {
            Stat stat = Util.findStat(plugin.getStatManager(), statDesc);
            if (stat == null) {
                plugin.getLogger().warning("Incorrect stat specified, not found: '" + statDesc + "'");
                return;
            }
            sd = new StatDescriptor(stat);
        }
        List<StatEntry> validEntries = new ArrayList<>();
        if (!holder.hasStat(sd.getStat())) {
            sender.sendMessage(Messages.getMessage("no-stats-yet", new Pair("%stat%", sd.getStat().getName())));
            return;
        }
        holder.getStats(sd.getStat()).stream().filter((entry) -> sd.getConditions().stream().allMatch(cond -> cond.matches(entry))).forEach(validEntries::add);
        CommandSender cs = sender.isConsole() ? plugin.getServer().getConsoleSender() : plugin.getServer().getPlayer(sender.getUniqueId());
        if (validEntries.size() == 1) {
            sender.sendMessage(sd.getStat().format(validEntries.get(0)));
        } else if (validEntries.isEmpty()) {
            if (sd.getConditions().isEmpty()) {
                sender.sendMessage(Messages.getMessage("no-stats-yet", new Pair("%stat%", sd.getStat().getName())));
            } else {
                new FancyMessage(
                        Messages.getMessage("no-stats-yet-fancy", new Pair("%stat%", sd.getStat().getName()), new Pair("%fancyMetadata%", ""))
                ).then("metadata").style(ChatColor.UNDERLINE).tooltip(sd.getConditions().toString()).send(cs);

            }
        } else {
            StatEntry entry = this.generateCommonEntry(validEntries, sd.getConditions());
            String message = sd.getStat().format(entry);
            message = message.replaceAll("%(.*?)%", "");
            sender.sendMessage(message);
        }
    }

    private StatEntry generateCommonEntry(List<StatEntry> entries, Set<Condition> conditions) {
        double value = 0;
        HashMap<String, List<Object>> pairs = new HashMap<>();
        value = entries.stream().map((entry) -> {
            entry.getMetadata().entrySet().stream().forEach((pair) -> {
                if (pairs.containsKey(pair.getKey())) {
                    ((List) pairs.get(pair.getKey())).add(pair.getValue());
                } else {
                    pairs.put(pair.getKey(), new ArrayList<Object>() {
                        {
                            this.add(pair.getValue());
                        }
                    });
                }
            });
            return entry;
        }).map((entry) -> entry.getValue()).reduce(value, (accumulator, _item) -> accumulator + _item);
        HashMap<String, Object> finalPairs = new HashMap<>();
        pairs.keySet().stream().forEach((key) -> {
            if (pairs.get(key) instanceof List && ((List) pairs.get(key)).size() == 1) {
                finalPairs.put(key, ((List) pairs.get(key)).get(0));
            } else {
                finalPairs.put(key, StringUtils.join((List) pairs.get(key), ", "));
            }
        });
        return new ConditionalStatEntry(new DefaultStatEntry(value, finalPairs), conditions);
    }

}
