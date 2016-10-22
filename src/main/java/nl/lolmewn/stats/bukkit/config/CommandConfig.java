package nl.lolmewn.stats.bukkit.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nl.lolmewn.itemmanager.inv.ManagedInventory;
import nl.lolmewn.itemmanager.items.ManagedItem;
import nl.lolmewn.stats.Condition;
import nl.lolmewn.stats.bukkit.BukkitMain;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
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
    private final Map<StatDescriptor, ManagedInventory> statInventories = new HashMap<>();

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

    private ManagedItem addItemForStat(String desc) {
        StatDescriptor descriptor = StatDescriptor.parse(desc, plugin.getStatManager());
        if (descriptor == null) {
            return null; // Well, at least I tried.
        }
        plugin.debug("Adding item to command inv for " + descriptor.getStat() + " with conditions " + descriptor.getConditions());
        ItemStack stack = new ItemStack(Material.WOOL);
        fixItemMeta(stack, descriptor);
        ManagedItem item = new ManagedItem(stack);
        item.setOnClickAction(player -> {
            this.statInventories.computeIfAbsent(descriptor, ignored -> createStatInventory(descriptor)).open(player);
        });
        this.mainInventory.addItem(item);
        return item;
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
        inv.setItem(inv.getFirstFreeSlot() + 1,
                new ManagedItem(Material.TNT)
                .setItemDescription("Removes this stat", "from the list of", "stats to show")
                .setItemName("Remove this stat")
                .setOnClickAction(p -> {
                    removeStat(descriptor);
                    p.sendMessage(ChatColor.RED + "Deleted entry for " + ChatColor.GOLD + descriptor.getStat().getName());
                    mainInventory.clear();
                    init();
                }));
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
                        addItemForStat(stat.getName()).perform(p);
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

    public void reloadStat(StatDescriptor oldDesc, StatDescriptor createdDesc) {
        ManagedInventory old = this.statInventories.get(oldDesc);
        ManagedInventory created = this.createStatInventory(createdDesc);
        old.consume(created);
    }

    private void removeStat(StatDescriptor descriptor) {
        ManagedInventory inv = this.statInventories.remove(descriptor);
        // Move all viewers from the old stat to the main inventory again
        List<HumanEntity> viewers = new ArrayList<>(inv.getViewers()); // Copy the list to avoid CME
        viewers.stream().filter(e -> e instanceof Player).map(e -> (Player) e).forEach(p -> mainInventory.open(p));
        List<String> showList = plugin.getConfig().getStringList("statsCommand.show");
        for (Iterator<String> it = showList.iterator(); it.hasNext();) {
            String desc = it.next();
            StatDescriptor confDesc = StatDescriptor.parse(desc, plugin.getStatManager());
            if (confDesc.equals(descriptor)) {
                it.remove();
                break;
            }
        }
        plugin.getConfig().set("statsCommand.show", showList);
        plugin.saveConfig();
    }

}
