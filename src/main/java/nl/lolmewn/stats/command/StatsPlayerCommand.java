package nl.lolmewn.stats.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import nl.lolmewn.stats.BukkitMain;
import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.Pair;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.util.Timings;
import nl.lolmewn.stats.stat.DefaultStatEntry;
import nl.lolmewn.stats.util.Util;
import org.apache.commons.lang.StringUtils;
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
    public void execute(CommandSender sender, String[] args) {
        Timings.startTiming("cmd-player", System.nanoTime());
        if(args.length == 0){
            sender.sendMessage(Messages.getMessage("needs-more-arguments", new Pair("%usage%", "/stats player <player>")));
            return;
        }
        OfflinePlayer player = plugin.getServer().getPlayer(args[0]);
        if(player == null){
            player = plugin.getServer().getOfflinePlayer(args[0]);
        }
        if(!player.hasPlayedBefore()){
            sender.sendMessage(Messages.getMessage("player-not-found", new Pair("%input%", args[0])));
            return;
        }
        StatsHolder holder = plugin.getUserManager().getUser(player.getUniqueId());
        if(holder == null){
            // probably need to load
            //TODO implement thread -> load -> show
            return;
        }
        List<String> statsToShow = plugin.getConfig().getStringList("statsCommand.show");
        for (String statDesc : statsToShow) {
            show(sender, holder, statDesc);
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

    private void show(CommandSender sender, StatsHolder holder, String statDesc) {
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
                } else {
                    if (value.startsWith("!")) {
                        cannotContain.put(paramName, value.substring(1));
                    } else {
                        containsOne.put(paramName, value);
                    }
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
        for (StatEntry entry : holder.getStats(stat)) {
            if (isValid(entry, cannotContain, containsOne)) {
                validEntries.add(entry);
            }
        }
        if (validEntries.size() == 1) {
            sender.sendMessage(stat.format(validEntries.get(0)));
        } else if (validEntries.isEmpty()) {
            sender.sendMessage(Messages.getMessage("no-stats-yet"));
        } else {
            sender.sendMessage(stat.format(this.generateCommonEntry(validEntries)));
        }
    }

    private boolean isValid(StatEntry entry, HashMap<String, String> blacklist, HashMap<String, String> orList) {
        for (String metaName : blacklist.keySet()) {
            if (entry.getMetadata().containsKey(metaName) && entry.getMetadata().get(metaName).equals(blacklist.get(metaName))) {
                return false; // found blacklisted item
            }
        }
        for (String metaName : orList.keySet()) {
            if (entry.getMetadata().containsKey(metaName) && entry.getMetadata().get(metaName).equals(orList.get(metaName))) {
                return true; // found item
            }
        }
        return orList.isEmpty();
    }

    private StatEntry generateCommonEntry(List<StatEntry> entries) {
        double value = 0;
        HashMap<String, Object> pairs = new HashMap<>();
        for (StatEntry entry : entries) {
            for (final Entry<String, Object> pair : entry.getMetadata().entrySet()) {
                if (pairs.containsKey(pair.getKey())) {
                    ((List) pairs.get(pair.getKey())).add(pair.getValue());
                } else {
                    pairs.put(pair.getKey(), new ArrayList<Object>() {
                        {
                            this.add(pair.getValue());
                        }
                    });
                }
            }
            value += entry.getValue();
        }
        for (String key : pairs.keySet()) {
            if (pairs.get(key) instanceof List && ((List) pairs.get(key)).size() == 1) {
                pairs.put(key, ((List) pairs.get(key)).get(0));
            } else {
                pairs.put(key, StringUtils.join((List) pairs.get(key), ", "));
            }
        }
        return new DefaultStatEntry(value, pairs);
    }

}
