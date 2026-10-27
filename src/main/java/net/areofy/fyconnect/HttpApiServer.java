package net.areofy.fyconnect;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class HttpApiServer {

    private final FyConnectPlugin plugin;
    private HttpServer server;

    public HttpApiServer(FyConnectPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() throws IOException {
        int port = plugin.getConfig().getInt("port", 4580);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/ping", this::ping);
        server.createContext("/command", this::command);
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    public int port() {
        return plugin.getConfig().getInt("port", 4580);
    }

    private void ping(HttpExchange exchange) throws IOException {
        if (!authorized(exchange)) return;
        reply(exchange, 200, Map.of(
                "ok", true,
                "plugin", "FyConnect",
                "online", Bukkit.getOnlinePlayers().size()
        ));
    }

    private void command(HttpExchange exchange) throws IOException {
        if (!authorized(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            reply(exchange, 405, Map.of("ok", false, "message", "POST gerekli."));
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String command = Json.readString(body, "command");
        if (command == null || command.isBlank()) {
            reply(exchange, 400, Map.of("ok", false, "message", "Komut bos."));
            return;
        }
        String clean = command.startsWith("/") ? command.substring(1) : command;
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean ok = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clean);
            if (!ok && plugin.getConfig().getBoolean("queue-offline-commands", true)) {
                plugin.queue().add(clean);
            }
        });
        reply(exchange, 200, Map.of("ok", true, "message", "Komut alindi."));
    }

    private boolean authorized(HttpExchange exchange) throws IOException {
        String expected = plugin.getConfig().getString("api-key", "");
        String given = exchange.getRequestHeaders().getFirst("x-api-key");
        if (expected.isBlank() || !expected.equals(given)) {
            reply(exchange, 401, Map.of("ok", false, "message", "Yetkisiz."));
            return false;
        }
        return true;
    }

    private void reply(HttpExchange exchange, int status, Map<String, ?> body) throws IOException {
        byte[] bytes = Json.obj(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
