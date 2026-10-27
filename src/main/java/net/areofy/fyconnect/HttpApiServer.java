package net.areofy.fyconnect;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public final class HttpApiServer {

    private final FyConnectPlugin plugin;
    private HttpServer server;

    public HttpApiServer(FyConnectPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() throws IOException {
        int port = plugin.getConfig().getInt("port", 4580);
        server = HttpServer.create(new InetSocketAddress(port), 10);
        server.createContext("/ping",    this::ping);
        server.createContext("/command", this::command);
        server.setExecutor(Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "FyConnect-HTTP");
            t.setDaemon(true);
            return t;
        }));
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(1);
    }

    public int port() {
        return plugin.getConfig().getInt("port", 4580);
    }

    private void ping(HttpExchange ex) throws IOException {
        if (!authorized(ex)) return;
        reply(ex, 200, Map.of(
            "ok",       true,
            "plugin",   "FyConnect",
            "version",  plugin.getDescription().getVersion(),
            "platform", PlatformUtil.isFolia() ? "Folia" : "Paper/Spigot",
            "online",   Bukkit.getOnlinePlayers().size()
        ));
    }

    private void command(HttpExchange ex) throws IOException {
        if (!authorized(ex)) return;
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            reply(ex, 405, Map.of("ok", false, "message", "POST gerekli."));
            return;
        }
        String body   = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String cmd    = Json.readString(body, "command");
        if (cmd == null || cmd.isBlank()) {
            reply(ex, 400, Map.of("ok", false, "message", "Komut bos."));
            return;
        }
        final String clean  = cmd.startsWith("/") ? cmd.substring(1) : cmd;
        final String player = Json.readString(body, "player");
        reply(ex, 200, Map.of("ok", true, "message", "Komut alindi."));
        PlatformUtil.runSync(plugin, () -> {
            boolean queued = false;
            if (player != null && !player.isBlank()
                    && plugin.getConfig().getBoolean("queue-offline-commands", true)) {
                if (Bukkit.getPlayerExact(player) == null) {
                    plugin.queue().add(player, clean);
                    queued = true;
                }
            }
            if (!queued) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clean);
            }
        });
    }

    private boolean authorized(HttpExchange ex) throws IOException {
        String expected = plugin.getConfig().getString("api-key", "");
        String given    = ex.getRequestHeaders().getFirst("x-api-key");
        if (expected.isBlank() || !expected.equals(given)) {
            reply(ex, 401, Map.of("ok", false, "message", "Yetkisiz."));
            return false;
        }
        return true;
    }

    private void reply(HttpExchange ex, int status, Map<String, ?> body) throws IOException {
        byte[] bytes = Json.obj(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Connection",   "close");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}