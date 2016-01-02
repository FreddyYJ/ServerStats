package nl.lolmewn.stats.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Lolmewn
 */
public class BukkitUtil {

    private static boolean IS_BUKKIT;

    static {
        try {
            Bukkit.getServerName();
            IS_BUKKIT = true;
        } catch (Exception ignored) {
            System.out.println("[Stats] It would appear this server is not running Bukkit. No worries, I got you covered!");
            IS_BUKKIT = false;
        }
    }

    public static String getWeaponName(ItemStack stack) {
        return stack == null
                ? "Fists"
                : (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()
                        ? stack.getItemMeta().getDisplayName()
                        : (stack.getType().name().substring(0, 1) + stack.getType().name().substring(1).toLowerCase().replace("_", " ")));
    }

    public static boolean isBukkit() {
        return IS_BUKKIT;
    }

}
