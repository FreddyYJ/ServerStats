package nl.lolmewn.stats.bukkit.signs;

import nl.lolmewn.stats.signs.SignLocation;
import org.bukkit.Location;

/**
 *
 * @author Lolmewn
 */
public class BukkitSignLocation extends SignLocation {
    
    public BukkitSignLocation(Location loc){
        this(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public BukkitSignLocation(String world, int x, int y, int z) {
        super(world, x, y, z);
    }

}
