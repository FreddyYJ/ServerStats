package nl.lolmewn.stats.bukkit.signs;

import java.util.Collection;
import java.util.HashSet;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.signs.SignLocation;
import nl.lolmewn.stats.signs.SignType;
import nl.lolmewn.stats.signs.StatsSign;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 *
 * @author Lolmewn
 */
public class BukkitStatsSign implements StatsSign {

    private final HashSet<StatsHolder> users = new HashSet<>();
    private final SignLocation location;
    private final HashSet<Stat> stats = new HashSet<>();
    private final SignType type;

    public BukkitStatsSign(SignLocation location, SignType type) {
        this.location = location;
        this.type = type;
    }

    @Override
    public Collection<StatsHolder> getHolders() {
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
    public SignType getSignType() {
        return type;
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
        for(int i = 0; i < 4 && i < text.length; i++){
            sign.setLine(i, text[i]);
        }
        sign.update(false, false);
    }

}
