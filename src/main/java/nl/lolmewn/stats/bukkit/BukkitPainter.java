package nl.lolmewn.stats.bukkit;

import nl.lolmewn.stats.Painter;
import org.bukkit.ChatColor;

/**
 *
 * @author Lolmewn
 */
public class BukkitPainter implements Painter {

    @Override
    public String convertColorCodes(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

}
