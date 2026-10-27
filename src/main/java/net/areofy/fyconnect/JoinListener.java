package net.areofy.fyconnect;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {

    private final FyConnectPlugin plugin;

    public JoinListener(FyConnectPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int delivered = plugin.queue().flush();
            if (delivered > 0) {
                String message = plugin.getConfig().getString("messages.pending-delivered", "")
                        .replace("{count}", String.valueOf(delivered));
                event.getPlayer().sendMessage(Text.component(
                        plugin.getConfig().getString("messages.prefix", "") + message));
            }
        }, 40L);
    }
}
