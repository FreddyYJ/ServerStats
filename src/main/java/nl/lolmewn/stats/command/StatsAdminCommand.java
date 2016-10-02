package nl.lolmewn.stats.command;

import nl.lolmewn.itemmanager.inv.ManagedInventory;
import nl.lolmewn.itemmanager.items.ManagedItem;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.bukkit.config.CommandConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Lolmewn
 */
public class StatsAdminCommand extends SubCommand {

    private final ManagedInventory inventory;
    private final CommandConfig config;

    public StatsAdminCommand(BukkitMain plugin) {
        this.inventory = new ManagedInventory(plugin, "&cAdmin inventory");
        this.config = new CommandConfig(plugin);
        inventory.setItem(0,
                new ManagedItem(new ItemStack(Material.NETHER_STAR)).setOnClickAction((Player player) -> {
                    config.start(player);
                })
                .setItemName(ChatColor.GOLD + "Configure /stats")
                .setItemDescription(ChatColor.GREEN + "This allows you to configure what players will see when they perform /stats"));
    }

    @Override
    public void execute(Dispatcher sender, String[] args) {

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
        return "stats.admin";
    }

}
