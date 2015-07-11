package nl.lolmewn.stats.bukkit.signs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import nl.lolmewn.stats.api.StatManager;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.api.user.UserManager;
import nl.lolmewn.stats.signs.SignLocation;
import nl.lolmewn.stats.signs.SignPlayerType;
import nl.lolmewn.stats.signs.SignStatType;
import nl.lolmewn.stats.signs.StatsSign;
import nl.lolmewn.stats.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 *
 * @author Lolmewn
 */
public class BukkitStatsSign implements StatsSign {

    private final HashSet<UUID> users = new HashSet<>();
    private final SignLocation location;
    private final HashSet<Stat> stats = new HashSet<>();
    private final SignPlayerType playerType;
    private final SignStatType statType;

    private transient final Random rand;
    private transient final Queue<UUID> pDisplayQueue = new ConcurrentLinkedQueue<>();
    private transient final Queue<Stat> sDisplayQueue = new ConcurrentLinkedQueue<>();
    
    public BukkitStatsSign(){
        location = null;
        playerType = null;
        statType = null;
        rand = null;
    }

    public BukkitStatsSign(SignLocation location, SignPlayerType playerType, SignStatType statType) {
        this.location = location;
        this.playerType = playerType;
        this.statType = statType;
        if (playerType == SignPlayerType.RANDOM || statType == SignStatType.RANDOM) {
            rand = new Random();
        } else {
            rand = null;
        }
    }

    @Override
    public Collection<UUID> getHolders() {
        return users;
    }

    @Override
    public SignLocation getLocation() {
        return location;
    }

    @Override
    public Collection<Stat> getStats() {
        return stats;
    }

    @Override
    public SignPlayerType getSignType() {
        return playerType;
    }

    @Override
    public boolean isActive() {
        World world = Bukkit.getWorld(location.getWorld());
        if (world == null) {
            return false;
        }
        if (!world.isChunkLoaded(location.getX() >> 4, location.getZ() >> 4)) {
            return false;
        }
        Block block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
        return (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN));
    }

    @Override
    public void setText(String... text) {
        if (!isActive()) {
            return;
        }
        Sign sign = (Sign) Bukkit.getWorld(location.getWorld()).getBlockAt(location.getX(), location.getY(), location.getZ()).getState();
        for (int i = 0; i < 4 && i < text.length; i++) {
            sign.setLine(i, text[i]);
        }
        sign.update(false, false);
    }

    @Override
    public void addHolder(UUID uuid) {
        this.users.add(uuid);
    }

    @Override
    public void addStat(Stat stat) {
        this.stats.add(stat);
    }

    @Override
    public void update(StatManager statManager, UserManager userManager) {
        if (pDisplayQueue.isEmpty()) {
            sDisplayQueue.poll(); // We've gone through all players, next stat please!
            refillPlayerQueue();
        }
        if (!verifyPlayerOnline()) {
            return;
        }
        StatsHolder holder;
        do {
            UUID uuid = getUUID();
            if (uuid == null) {
                refillPlayerQueue();
                uuid = getUUID();
                if (uuid == null) {
                    return; // no players online
                }
            }
            holder = userManager.getUser(uuid);
        } while (holder == null);
        Stat stat = getStat(statManager);
        double value = Util.sumAll(holder.getStats(stat));
        setText(
                ChatColor.BLACK + "[" + ChatColor.YELLOW + "Stats" + ChatColor.BLACK + "]",
                stat.getName(),
                "by " + Bukkit.getServer().getOfflinePlayer(holder.getUuid()).getName(),
                String.valueOf(value));

    }

    private UUID getUUID() {
        if (playerType == SignPlayerType.RANDOM) {
            refillPlayerQueue();
            if (pDisplayQueue.isEmpty()) {
                return null;
            }
            for (int i = 0; i < rand.nextInt(pDisplayQueue.size()); i++) {
                pDisplayQueue.poll(); // poll a random amount between 0-size
            }
        }
        return pDisplayQueue.poll();
    }

    private Stat getStat(StatManager statManager) {
        if (sDisplayQueue.isEmpty()) {
            refillStatQueue(statManager); // We've gone through all stats too, re-start!
        }
        return sDisplayQueue.peek(); // Return element at the top, but don't remove it
    }

    private void refillPlayerQueue() {
        pDisplayQueue.clear();
        switch (playerType) {
            case ALL:
            case RANDOM:
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    pDisplayQueue.add(player.getUniqueId());
                }
                break;
            default:
                pDisplayQueue.addAll(users);
        }
    }

    private void refillStatQueue(StatManager statManager) {
        sDisplayQueue.clear();
        switch (statType) {
            case ALL:
            case RANDOM:
                for (Stat stat : statManager.getStats()) {
                    if (stat.isEnabled()) {
                        sDisplayQueue.add(stat);
                    }
                }
                break;
            default:
                sDisplayQueue.addAll(stats);
        }
    }

    private boolean verifyPlayerOnline() {
        switch (playerType) {
            case ALL:
            case RANDOM:
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    return true; // If there exists one, return true
                }
                return false;
            default:
                for (UUID uuid : users) {
                    if (Bukkit.getServer().getPlayer(uuid) != null) {
                        return true;
                    }
                }
                return false;
        }
    }
}
