package net.areofy.fyconnect;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class CommandQueue {

    private final FyConnectPlugin plugin;
    private final File file;
    private final List<String> pending = new ArrayList<>();

    public CommandQueue(FyConnectPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "queue.yml");
        load();
    }

    public synchronized void add(String command) {
        pending.add(command);
        save();
        plugin.getLogger().info("Oyuncu cevrimdisi, komut siraya alindi: " + command);
    }

    public synchronized int flush() {
        if (pending.isEmpty()) return 0;
        List<String> copy = new ArrayList<>(pending);
        pending.clear();
        save();
        int done = 0;
        for (String command : copy) {
            if (Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)) done++;
        }
        return done;
    }

    private void load() {
        if (!file.exists()) return;
        List<String> stored = YamlConfiguration.loadConfiguration(file).getStringList("pending");
        pending.addAll(stored);
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("pending", pending);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Kuyruk kaydedilemedi: " + e.getMessage());
        }
    }
}
