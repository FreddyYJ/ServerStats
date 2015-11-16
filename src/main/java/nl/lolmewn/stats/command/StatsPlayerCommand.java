package nl.lolmewn.stats.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import mkremins.fanciful.FancyMessage;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.util.Timings;
import nl.lolmewn.stats.util.Util;
import nl.lolmewn.stats.util.task.AsyncSyncTask;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Lolmewn
 */
public class StatsPlayerCommand extends SubCommand {

    private final BukkitMain plugin;

    public StatsPlayerCommand(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Dispatcher sender, String[] args) {
        Timings.startTiming("cmd-player", System.nanoTime());
        if (args.length == 0) {
            sender.sendMessage(Messages.getMessage("needs-more-arguments", new Pair("%usage%", "/stats player <player>")));
            return;
        }
        OfflinePlayer player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            player = plugin.getServer().getOfflinePlayer(args[0]);
        }
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(Messages.getMessage("player-not-found", new Pair("%input%", args[0])));
            return;
        }
        final UUID uuid = player.getUniqueId();
        StatsHolder holder = plugin.getUserManager().getUser(uuid);
        if (holder == null) {
            new AsyncSyncTask<StatsHolder>() {

                @Override
                public StatsHolder executeGetTask() {
                    try {
                        return plugin.getUserManager().loadUser(uuid, plugin.getStatManager());
                    } catch (StorageException ex) {
                        Logger.getLogger(StatsPlayerCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return null;
                }

                @Override
                public void executeUseTask(StatsHolder val) {
                    if (val == null) {
                        return;
                    }
                    List<String> statsToShow = plugin.getConfig().getStringList("statsCommand.show");
                    statsToShow.stream().forEach((statDesc) -> {
                        show(sender, holder, statDesc);
                    });
                }
            };
            // probably need to load
            //TODO implement thread -> load -> show
            return;
        } else {
            List<String> statsToShow = plugin.getConfig().getStringList("statsCommand.show");
            statsToShow.stream().forEach((statDesc) -> {
                show(sender, holder, statDesc);
            });
        }
        plugin.debug("cmd-player: " + Timings.finishTimings("cmd-root", System.nanoTime()));
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
        return "stats.view.others";
    }

    private void show(Dispatcher sender, StatsHolder holder, String statDesc) {
        Stat stat;
        HashMap<String, String> cannotContain = new HashMap<>();
        HashMap<String, String> containsOne = new HashMap<>();
        if (statDesc.contains(",")) {
            String[] split = statDesc.split(",");
            String name = split[0];
            stat = Util.findStat(plugin.getStatManager(), name);
            if (stat == null) {
                plugin.getLogger().warning("Incorrect stat specified, not found: '" + name + "'");
                return;
            }
            for (int i = 1; i < split.length; i++) {
                String cond = split[i];
                if (!cond.contains("=")) {
                    plugin.getLogger().warning("Incorrect condition specified, contains no value: '" + cond + "' in '" + name + "'");
                    continue;
                }
                String[] vals = cond.split("=");
                if (vals.length != 2) {
                    plugin.getLogger().warning("Incorrect condition specified, contains multiple values: '" + cond + "' in '" + name + "'");
                    continue;
                }
                String paramName = vals[0];
                String value = vals[1];
                if (value.contains("|")) {
                    //not just one
                    String[] params = value.split("|");
                    for (String param : params) {
                        if (param.startsWith("!")) {
                            cannotContain.put(paramName, param.substring(1));
                        } else {
                            containsOne.put(paramName, param);
                        }
                    }
                } else if (value.startsWith("!")) {
                    cannotContain.put(paramName, value.substring(1));
                } else {
                    containsOne.put(paramName, value);
                }
            }
        } else {
            stat = plugin.getStatManager().getStat(statDesc);
        }
        List<StatEntry> validEntries = new ArrayList<>();
        if (!holder.hasStat(stat)) {
            sender.sendMessage(Messages.getMessage("no-stats-yet"));
            return;
        }
        holder.getStats(stat).stream().filter((entry) -> (isValid(entry, cannotContain, containsOne))).forEach((entry) -> {
            validEntries.add(entry);
        });
        if (validEntries.size() == 1) {
            sender.sendMessage(stat.format(validEntries.get(0)));
        } else if (validEntries.isEmpty()) {
            if (!cannotContain.isEmpty() || !containsOne.isEmpty()) {
                CommandSender cs = sender.isConsole() ? plugin.getServer().getConsoleSender() : plugin.getServer().getPlayer(sender.getUniqueId());
                new FancyMessage(
                        Messages.getMessage("no-stats-yet-fancy", new Pair("%stat%", stat.getName()), new Pair("%fancyMetadata%", ""))
                ).then("metadata").style(ChatColor.UNDERLINE).tooltip(
                        cannotContain.toString().replace("{", "").replace("}", "").replace("=", "!=")
                        + " " + containsOne.toString().replace("{", "").replace("}", ""))
                        .send(cs);
            } else {
                sender.sendMessage(Messages.getMessage("no-stats-yet", new Pair("%stat%", stat.getName())));
            }
        } else {
            sender.sendMessage(stat.format(this.generateCommonEntry(validEntries)));
        }
    }

    private boolean isValid(StatEntry entry, HashMap<String, String> blacklist, HashMap<String, String> orList) {
        if (!blacklist.keySet().stream().noneMatch((metaName) -> (entry.getMetadata().containsKey(metaName) && entry.getMetadata().get(metaName).equals(blacklist.get(metaName))))) {
            return false;
        } // found blacklisted item
        if (orList.keySet().stream().anyMatch((metaName) -> (entry.getMetadata().containsKey(metaName) && entry.getMetadata().get(metaName).equals(orList.get(metaName))))) {
            return true;
        } // found item
        return orList.isEmpty();
    }

    private StatEntry generateCommonEntry(List<StatEntry> entries) {
        double value = 0;
        HashMap<String, Object> pairs = new HashMap<>();
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
        pairs.keySet().stream().forEach((key) -> {
            if (pairs.get(key) instanceof List && ((List) pairs.get(key)).size() == 1) {
                pairs.put(key, ((List) pairs.get(key)).get(0));
            } else {
                pairs.put(key, StringUtils.join((List) pairs.get(key), ", "));
            }
        });
        return new DefaultStatEntry(value, pairs);
    }

}
