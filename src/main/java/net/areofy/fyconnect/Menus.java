package net.areofy.fyconnect;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class Menus {

    public static final String MAIN        = "main";
    public static final String MARKET      = "market";
    public static final String HABERLER    = "haberler";
    public static final String DUYURULAR   = "duyurular";
    public static final String ONERILER    = "oneriler";
    public static final String GUNCELLEME  = "guncelleme";

    private final FyConnectPlugin plugin;

    public Menus(FyConnectPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, title("messages.menu-title"));
        paint(inventory);
        if (enabled("market"))    item(inventory, 10, Material.CHEST,          "&6Market",           "&7Sitedeki urunleri goster");
        if (enabled("haberler"))  item(inventory, 12, Material.WRITABLE_BOOK,  "&6Haberler",         "&7Son haber ve duyurular");
        if (enabled("duyurular")) item(inventory, 13, Material.BELL,           "&6Duyurular",        "&7Resmi duyurular");
        if (enabled("oneriler"))  item(inventory, 14, Material.MAP,            "&6Oneriler",         "&7Oyuncu onerileri");
        if (enabled("guncelleme"))item(inventory, 16, Material.BOOK,           "&6Guncelleme Notlari","&7Son degisiklikler");
        player.openInventory(inventory);
    }

    public void openList(Player player, String which) {
        List<SiteClient.Entry> entries = switch (which) {
            case HABERLER   -> plugin.site().haberler();
            case DUYURULAR  -> plugin.site().duyurular();
            case ONERILER   -> plugin.site().oneriler();
            case GUNCELLEME -> plugin.site().guncelleme();
            default         -> plugin.site().market();
        };
        String titleKey = switch (which) {
            case HABERLER   -> "messages.haberler-title";
            case DUYURULAR  -> "messages.duyurular-title";
            case ONERILER   -> "messages.oneriler-title";
            case GUNCELLEME -> "messages.guncelleme-title";
            default         -> "messages.market-title";
        };
        Material material = switch (which) {
            case HABERLER   -> Material.PAPER;
            case DUYURULAR  -> Material.BELL;
            case ONERILER   -> Material.MAP;
            case GUNCELLEME -> Material.BOOK;
            default         -> Material.GOLD_INGOT;
        };
        Inventory inventory = Bukkit.createInventory(null, 54, title(titleKey));
        paint(inventory);
        if (entries.isEmpty()) {
            item(inventory, 22, Material.BARRIER, "&7Bos", plugin.getConfig().getString("messages.empty", ""));
        } else {
            int slot = 0;
            for (SiteClient.Entry entry : entries) {
                while (slot < 45 && isFrame(slot)) slot++;
                if (slot >= 45) break;
                item(inventory, slot, material, "&f" + entry.title(), entry.detail().isBlank() ? "" : "&7" + entry.detail());
                slot++;
            }
        }
        item(inventory, 49, Material.ARROW, "&eGeri Don", "&7Ana menuye don");
        player.openInventory(inventory);
    }

    private boolean enabled(String key) {
        return plugin.getConfig().getBoolean("menu." + key, true);
    }

    private net.kyori.adventure.text.Component title(String key) {
        return Text.component(plugin.getConfig().getString(key, "&8Minevia"));
    }

    private void paint(Inventory inventory) {
        ItemStack pane = named(Material.GRAY_STAINED_GLASS_PANE, " ");
        int size = inventory.getSize();
        int rows = size / 9;
        for (int slot = 0; slot < size; slot++) {
            int row = slot / 9;
            int col = slot % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                inventory.setItem(slot, pane);
            }
        }
    }

    private boolean isFrame(int slot) {
        int col = slot % 9;
        return col == 0 || col == 8;
    }

    private void item(Inventory inventory, int slot, Material material, String name, String lore) {
        ItemStack stack = named(material, name);
        if (lore != null && !lore.isBlank()) {
            ItemMeta meta = stack.getItemMeta();
            List<String> lines = new ArrayList<>();
            for (String line : lore.split("\\n")) lines.add(Text.color(line));
            meta.setLore(lines);
            stack.setItemMeta(meta);
        }
        inventory.setItem(slot, stack);
    }

    private ItemStack named(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Text.component(name));
        stack.setItemMeta(meta);
        return stack;
    }
}
