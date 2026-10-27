package net.areofy.fyconnect;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class SiteCommand implements CommandExecutor, TabCompleter {

    private final FyConnectPlugin plugin;
    private final Menus menus;

    public SiteCommand(FyConnectPlugin plugin) {
        this.plugin = plugin;
        this.menus = new Menus(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("fyconnect.admin")) { sender.sendMessage(Text.component(msg("messages.no-permission"))); return true; }
            plugin.reloadConfig();
            sender.sendMessage(Text.component(msg("messages.reloaded")));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("sync")) {
            if (!sender.hasPermission("fyconnect.admin")) { sender.sendMessage(Text.component(msg("messages.no-permission"))); return true; }
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.site().refresh();
                sender.sendMessage(Text.component(msg("messages.sync-ok")));
            });
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncular icin.");
            return true;
        }
        String cmd = command.getName().toLowerCase();
        switch (cmd) {
            case "market"    -> menus.openList(player, Menus.MARKET);
            case "haberler"  -> menus.openList(player, Menus.HABERLER);
            case "duyurular" -> menus.openList(player, Menus.DUYURULAR);
            default          -> menus.openMain(player);
        }
        return true;
    }

    public boolean owns(String plainTitle) {
        return matches(plainTitle, "messages.menu-title")
                || matches(plainTitle, "messages.market-title")
                || matches(plainTitle, "messages.haberler-title")
                || matches(plainTitle, "messages.duyurular-title")
                || matches(plainTitle, "messages.oneriler-title")
                || matches(plainTitle, "messages.guncelleme-title");
    }

    public void click(Player player, String plainTitle, int slot) {
        if (matches(plainTitle, "messages.menu-title")) {
            String which = switch (slot) {
                case 10 -> Menus.MARKET;
                case 12 -> Menus.HABERLER;
                case 13 -> Menus.DUYURULAR;
                case 14 -> Menus.ONERILER;
                case 16 -> Menus.GUNCELLEME;
                default -> "";
            };
            if (!which.isEmpty()) menus.openList(player, which);
            return;
        }
        if (slot == 49) menus.openMain(player);
    }

    private boolean matches(String plain, String key) {
        String expected = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(Text.component(plugin.getConfig().getString(key, "")));
        return plain.equals(expected);
    }

    private String msg(String key) {
        return plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString(key, "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("fyconnect.admin")) return List.of("reload", "sync");
        return List.of();
    }
}
