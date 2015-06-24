package nl.lolmewn.stats.bukkit;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.stats.api.storage.StorageEngine;
import nl.lolmewn.stats.api.storage.StorageException;
import nl.lolmewn.stats.mysql.MySQLStorage;
import nl.lolmewn.stats.user.DefaultStatsHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Lolmewn
 */
public class PlayerIOEvents implements Listener {

    private final BukkitMain plugin;

    public PlayerIOEvents(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerJoinEvent event) {
        try {
            if (plugin.getUserManager().getUser(event.getPlayer().getUniqueId()) == null) {
                plugin.getUserManager().loadUser(event.getPlayer().getUniqueId(), plugin.getStatManager());
            }
        } catch (Exception ex) {
            Logger.getLogger(PlayerIOEvents.class.getName()).log(Level.SEVERE, null, ex);
            plugin.getLogger().severe("The error above means Stats was unable to load player " + event.getPlayer().getName());
            plugin.getLogger().severe("In an attempt to not completely ruin your server, we've given him an empty Stats account");
            plugin.getUserManager().addUser(new DefaultStatsHolder(event.getPlayer().getUniqueId()));
            plugin.getLogger().severe("Please check if this issue has been reported before. If not, report it with as much info as you can");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        quit(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        quit(event.getPlayer().getUniqueId());
    }

    public void quit(final UUID uuid) {
        try {
            Field engineField = plugin.getUserManager().getClass().getDeclaredField("storage");
            engineField.setAccessible(true);
            final StorageEngine engine = (StorageEngine) engineField.get(plugin.getUserManager());
            if (engine instanceof MySQLStorage) {
                final MySQLStorage mysql = (MySQLStorage) engine;
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                    @Override
                    public void run() {
                        try (Connection con = mysql.getConnection()) {
                            mysql.lock(con, uuid);
                            plugin.getUserManager().saveUser(uuid);
                        } catch (SQLException | StorageException ex) {
                            Logger.getLogger(PlayerIOEvents.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (plugin.getServer().getPlayer(uuid) == null) {
                            plugin.getUserManager().removeUser(uuid);
                        }
                    }
                });
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(PlayerIOEvents.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
