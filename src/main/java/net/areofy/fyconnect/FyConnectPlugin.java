package net.areofy.fyconnect;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FyConnectPlugin extends JavaPlugin {

    private HttpApiServer api;
    private SiteClient site;
    private CommandQueue queue;
    private int syncTask = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ensureApiKey();
        this.queue = new CommandQueue(this);
        this.site = new SiteClient(this);
        this.api = new HttpApiServer(this);

        try {
            api.start();
        } catch (Exception e) {
            getLogger().severe("API sunucusu baslatilamadi: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        SiteCommand command = new SiteCommand(this);
        getCommand("site").setExecutor(command);
        getCommand("site").setTabCompleter(command);
        getCommand("market").setExecutor(command);
        getCommand("haberler").setExecutor(command);
        getCommand("duyurular").setExecutor(command);
        getServer().getPluginManager().registerEvents(new MenuListener(command), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        long ticks = Math.max(20L, getConfig().getLong("sync-interval-seconds", 120) * 20L);
        syncTask = PlatformUtil.runAsyncTimer(this, site::refresh, 40L, ticks);

        getLogger().info("FyConnect hazir. Port " + api.port() + " dinleniyor.");
        getLogger().info("API Key: " + getConfig().getString("api-key"));
    }

    private void ensureApiKey() {
        String current = getConfig().getString("api-key", "");
        if (current == null || current.isBlank() || current.equals("BURAYA_GIZLI_ANAHTARINIZI_YAZIN")) {
            String generated = java.util.UUID.randomUUID().toString().replace("-", "");
            getConfig().set("api-key", generated);
            saveConfig();
            getLogger().info("Yeni API anahtari otomatik olusturuldu: " + generated);
        }
    }

    @Override
    public void onDisable() {
        PlatformUtil.cancel(syncTask);
        PlatformUtil.cancelAll(this);
        if (api != null) api.stop();
    }

    public SiteClient site() {
        return site;
    }

    public CommandQueue queue() {
        return queue;
    }

    public String color(String text) {
        return Text.color(text);
    }
}
