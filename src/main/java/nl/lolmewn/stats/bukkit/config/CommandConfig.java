package nl.lolmewn.stats.bukkit.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nl.lolmewn.itemmanager.inv.ManagedInventory;
import nl.lolmewn.itemmanager.items.ManagedItem;
import nl.lolmewn.stats.Condition;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.bukkit.BukkitMain;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Lolmewn
 */
public class CommandConfig {

    private final BukkitMain plugin;
    private final ManagedInventory mainInventory;
    private final Map<Stat, ManagedInventory> statInventories = new HashMap<>();

    public CommandConfig(BukkitMain plugin) {
        this.plugin = plugin;
        mainInventory = new ManagedInventory(plugin, "&2Stats command");
        init();
    }

    public void start(Player player) {
        mainInventory.open(player);
    }

    private void init() {
        List<String> statsToShow = plugin.getConfig().getStringList("statsCommand.show");
        statsToShow.forEach(this::addItemForStat);
        mainInventory.addItem(new ManagedItem(Material.STAINED_GLASS_PANE)
                .setItemDescription("Click to add new stat")
                .setItemName("Add new")
                .setOnClickAction(this::chooseStat)
        );
    }

    private void addItemForStat(String desc) {
        StatDescriptor descriptor = StatDescriptor.parse(desc, plugin.getStatManager());
        if (descriptor == null) {
            return; // Well, at least I tried.
        }
        plugin.debug("Adding item to command inv for " + descriptor.getStat() + " with conditions " + descriptor.getConditions());
        ItemStack stack = new ItemStack(Material.WOOL);
        fixItemMeta(stack, descriptor);
        ManagedItem item = new ManagedItem(stack);
        item.setOnClickAction(player -> {
            this.statInventories.computeIfAbsent(descriptor.getStat(), ignored -> createStatInventory(descriptor)).open(player);
        });
        this.mainInventory.addItem(item);
    }

    private void fixItemMeta(ItemStack stack, StatDescriptor descriptor) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(descriptor.getStat().getName());
        meta.setLore(Arrays.asList(descriptor.getStat().getDescription(), "Conditions: " + descriptor.getConditions().size()));
        stack.setItemMeta(meta);
    }

    private ManagedInventory createStatInventory(StatDescriptor descriptor) {
        plugin.debug("Generating stat inventory for " + descriptor);
        ManagedInventory inv = new ManagedInventory(plugin, "Editing " + descriptor.getStat().getName());
        if (descriptor.getStat().getDataTypes() == null || descriptor.getStat().getDataTypes().isEmpty()) {
            // Well - nothing really to configure here
            addBackButton(inv, this.mainInventory);
            return inv;
        }
        descriptor.getStat().getDataTypes().entrySet().stream().forEach((metadata) -> {
            Optional<Condition> condition;
            if ((condition = descriptor.getConditions().stream().filter(cond -> cond.getName().equals(metadata.getKey())).findAny()).isPresent()) {
                // There is a condition for this metadata!
                Condition cond = condition.get();
                inv.addItem(
                        new ManagedItem(Material.REDSTONE_TORCH_ON)
                        .setItemName(metadata.getKey())
                        .setItemDescription("Mode: " + cond.getMode(), "Matches: " + cond.getMatches(), "Click to edit or disable")
                        .setOnClickAction(player -> {
                            player.closeInventory();
                            new CommandConversation(plugin, player, descriptor, cond.getName(), inv, this);
                        }));
            } else {
                inv.addItem(
                        new ManagedItem(Material.REDSTONE_LAMP_OFF)
                        .setItemName(metadata.getKey())
                        .setItemDescription("Click to enable")
                        .setOnClickAction(player -> {
                            player.closeInventory();
                            new CommandConversation(plugin, player, descriptor, metadata.getKey(), inv, this);
                        })
                );
            }
        });
        addBackButton(inv, mainInventory);
        return inv;
    }

    private void chooseStat(Player player) {
        ManagedInventory inv = new ManagedInventory(plugin, "Choose a stat");
        plugin.getStatManager().getStats().forEach(stat -> {
            inv.addItem(new ManagedItem(Material.DIAMOND)
                    .setItemName(stat.getName())
                    .setItemDescription(stat.getDescription())
                    .setOnClickAction(p -> {
                        this.mainInventory.clear();
                        List<String> curr = plugin.getConfig().getStringList("statsCommand.show");
                        curr.add(stat.getName());
                        plugin.getConfig().set("statsCommand.show", curr);
                        plugin.saveConfig();
                        this.init();
                        createStatInventory(new StatDescriptor(stat)).open(p);
                    }));
        });
        inv.open(player);
    }

    private void addBackButton(ManagedInventory inv, ManagedInventory previous) {
        inv.setItem(
                inv.getLastFreeSlot(),
                new ManagedItem(Material.NETHERRACK)
                .setItemName("&1Go back")
                .setItemDescription("Takes you to the previous inventory")
                .setOnClickAction(player -> previous.open(player)));
    }

    public void reloadStat(StatDescriptor desc) {
        ManagedInventory old = this.statInventories.remove(desc.getStat());
        ManagedInventory created = this.createStatInventory(desc);
        old.consume(created);
    }

}
