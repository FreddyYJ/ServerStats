package nl.lolmewn.stats.bukkit;

import nl.lolmewn.stats.Messages;
import nl.lolmewn.stats.signs.SignPlayerType;
import nl.lolmewn.stats.signs.SignStatType;
import nl.lolmewn.stats.util.Util;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Lolmewn
 */
public class SignEvents implements Listener {

    private final BukkitMain plugin;

    public SignEvents(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void signChange(SignChangeEvent event) {
        if (!event.getLine(0).equalsIgnoreCase("[Stats]")) {
            return;
        }
        if (!event.getPlayer().hasPermission("stats.sign.place")) {
            event.getPlayer().sendMessage(Messages.getMessage("no-perms"));
            cancelEvent(event);
            return;
        }
        SignStatType statType = getStatType(event.getLine(1));
        if (statType == null) {
            event.getPlayer().sendMessage(Messages.getMessage("sign.unknown-stat"));
            cancelEvent(event);
            return;
        }
        SignPlayerType playerType = getPlayerType(event.getLine(2));
        if (playerType == null) {
            event.getPlayer().sendMessage(Messages.getMessage("sign.unknown-player"));
            cancelEvent(event);
            return;
        }
        if(playerType == SignPlayerType.MULTIPLE || statType == SignStatType.MULTIPLE){
            //TODO implement some kind of conversation on what type they want
            event.getPlayer().sendMessage("Unimplemented - this feature will be live soon!");
            cancelEvent(event);
            return;
        }
        SignPlayerType type = getSignType(statType, playerType);
    }

    public void cancelEvent(SignChangeEvent event) {
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        event.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN, 1));
    }

    private SignStatType getStatType(String line) {
        if (line.equalsIgnoreCase("all")) {
            return SignStatType.ALL;
        } else if (line.equalsIgnoreCase("multiple")) {
            return SignStatType.MULTIPLE;
        } else if (line.equalsIgnoreCase("random")) {
            return SignStatType.RANDOM;
        } else if (Util.findStat(plugin.getStatManager(), line) != null) {
            return SignStatType.SINGLE;
        }
        return null;
    }

    private SignPlayerType getPlayerType(String line) {
        if (line.equalsIgnoreCase("all")) {
            return SignPlayerType.ALL;
        } else if (line.equalsIgnoreCase("multiple")) {
            return SignPlayerType.MULTIPLE;
        } else if (line.equalsIgnoreCase("random")) {
            return SignPlayerType.RANDOM;
        } else {
            OfflinePlayer player = plugin.getServer().getPlayer(line);
            if (player != null) {
                return SignPlayerType.SINGLE;
            }
            player = plugin.getServer().getOfflinePlayer(line);
            if (player.hasPlayedBefore()) {
                return SignPlayerType.SINGLE;
            }
            return null;
        }
    }

}
