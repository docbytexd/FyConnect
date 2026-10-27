package net.areofy.fyconnect;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class MenuListener implements Listener {

    private final SiteCommand command;

    public MenuListener(SiteCommand command) {
        this.command = command;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!command.owns(title)) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        command.click(player, title, event.getRawSlot());
    }
}