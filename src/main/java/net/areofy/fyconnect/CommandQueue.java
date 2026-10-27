package net.areofy.fyconnect;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class CommandQueue {

    private final FyConnectPlugin plugin;
    private final File file;
    private final Map<String, List<String>> byPlayer = new HashMap<>();

    public CommandQueue(FyConnectPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "queue.yml");
        load();
    }

    public synchronized void add(String player, String command) {
        String key = (player == null || player.isBlank()) ? "__global__" : player.toLowerCase();
        byPlayer.computeIfAbsent(key, k -> new ArrayList<>()).add(command);
        saveAsync();
        plugin.getLogger().info("[Queue] " + key + " icin siraya alindi: " + command);
    }

    public synchronized void add(String command) {
        add(null, command);
    }

    public synchronized int flush(String playerName) {
        String key      = playerName.toLowerCase();
        List<String> personal = byPlayer.remove(key);
        List<String> global   = byPlayer.remove("__global__");
        List<String> all      = new ArrayList<>();
        if (personal != null) all.addAll(personal);
        if (global   != null) all.addAll(global);
        if (all.isEmpty()) return 0;
        saveAsync();
        int done = 0;
        for (String cmd : all) {
            String resolved = cmd.replace("{username}", playerName);
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
                done++;
            } catch (Exception e) {
                plugin.getLogger().warning("[Queue] Komut calistirilamadi: " + resolved + " -> " + e.getMessage());
            }
        }
        return done;
    }

    private void load() {
        if (!file.exists()) return;
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            if (yaml.isConfigurationSection("players")) {
                for (String key : yaml.getConfigurationSection("players").getKeys(false)) {
                    List<String> cmds = yaml.getStringList("players." + key);
                    if (!cmds.isEmpty()) byPlayer.put(key.toLowerCase(), new ArrayList<>(cmds));
                }
            }
            int total = byPlayer.values().stream().mapToInt(List::size).sum();
            if (total > 0) plugin.getLogger().info("[Queue] " + total + " bekleyen komut yuklendi.");
        } catch (Exception e) {
            plugin.getLogger().warning("[Queue] Yuklenemedi: " + e.getMessage());
        }
    }

    private void saveAsync() {
        Map<String, List<String>> snapshot = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : byPlayer.entrySet()) {
            snapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        PlatformUtil.runAsync(plugin, () -> {
            YamlConfiguration yaml = new YamlConfiguration();
            for (Map.Entry<String, List<String>> entry : snapshot.entrySet()) {
                yaml.set("players." + entry.getKey(), entry.getValue());
            }
            try {
                yaml.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("[Queue] Kaydedilemedi: " + e.getMessage());
            }
        });
    }
}