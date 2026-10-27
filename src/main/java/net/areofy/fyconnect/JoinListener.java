package net.areofy.fyconnect;

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
        final org.bukkit.entity.Player player = event.getPlayer();
        final String name = player.getName();
        PlatformUtil.runLaterForPlayer(plugin, player, () -> {
            int delivered = plugin.queue().flush(name);
            if (delivered > 0) {
                String message = plugin.getConfig().getString("messages.pending-delivered", "")
                        .replace("{count}", String.valueOf(delivered))
                        .replace("{username}", name);
                player.sendMessage(Text.component(
                        plugin.getConfig().getString("messages.prefix", "") + message));
            }
        }, 40L);
    }
}