package nl.lolmewn.stats.command;

import nl.lolmewn.itemmanager.inv.ManagedInventory;
import nl.lolmewn.itemmanager.items.ManagedItem;
import nl.lolmewn.stats.bukkit.BukkitMain;
import nl.lolmewn.stats.bukkit.config.CommandConfig;
import org.bukkit.Bukkit;
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
                new ManagedItem(new ItemStack(Material.NETHER_STAR)).setOnClickAction(config::start)
                .setItemName(ChatColor.GOLD + "Configure /stats")
                .setItemDescription(ChatColor.GREEN + "Configure /stats contents"));
        inventory.setItem(1, 
                new ManagedItem(Material.CHEST).setOnClickAction(this::convert)
                .setItemName(ChatColor.RED + "Change storage type")
                .setItemDescription(ChatColor.GOLD + "You can change the storage type that Stats uses here")
        );
    }

    @Override
    public void execute(Dispatcher sender, String[] args) {
        inventory.open(Bukkit.getPlayer(sender.getUniqueId()));
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
    
    public void convert(Player player){
        player.closeInventory();
        player.sendMessage("Not yet implemented");
    }

}
