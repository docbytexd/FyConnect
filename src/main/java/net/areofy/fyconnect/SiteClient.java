package net.areofy.fyconnect;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SiteClient {

    public record Entry(String title, String detail) {
    }

    private final FyConnectPlugin plugin;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6)).build();

    private volatile List<Entry> market     = List.of();
    private volatile List<Entry> haberler   = List.of();
    private volatile List<Entry> duyurular  = List.of();
    private volatile List<Entry> oneriler   = List.of();
    private volatile List<Entry> guncelleme = List.of();

    public SiteClient(FyConnectPlugin plugin) {
        this.plugin = plugin;
    }

    public void refresh() {
        String base = base();
        if (base.isEmpty()) return;
        // /api/public/news -> haberler + duyurular (news API'si her ikisini de dondurur)
        List<Entry> allNews = read(base + "/api/public/news", "title", "category");
        haberler  = allNews;
        duyurular = read(base + "/api/public/announcements", "title", "category");
        if (duyurular.isEmpty()) duyurular = allNews; // fallback
        oneriler   = read(base + "/api/public/suggestions", "title", "votes");
        guncelleme = read(base + "/api/public/changelog",   "title", "version");
        market     = read(base + "/api/public/shop",        "name",  "price");
    }

    public List<Entry> market()     { return market; }
    public List<Entry> haberler()   { return haberler; }
    public List<Entry> duyurular()  { return duyurular; }
    public List<Entry> oneriler()   { return oneriler; }
    public List<Entry> guncelleme() { return guncelleme; }

    private String base() {
        String url = plugin.getConfig().getString("site-url", "");
        if (url == null) return "";
        url = url.trim();
        while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return url;
    }

    private List<Entry> read(String url, String titleKey, String detailKey) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return List.of();
            return parse(response.body(), titleKey, detailKey);
        } catch (Exception e) {
            plugin.getLogger().warning("Site verisi alinamadi (" + url + "): " + e.getMessage());
            return List.of();
        }
    }

    private List<Entry> parse(String json, String titleKey, String detailKey) {
        List<Entry> out = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"" + titleKey + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find() && out.size() < 36) {
            String title = unescape(matcher.group(1));
            String detail = nearby(json, matcher.end(), detailKey);
            out.add(new Entry(strip(title), strip(detail)));
        }
        return out;
    }

    private String nearby(String json, int from, String key) {
        int end = Math.min(json.length(), from + 900);
        String slice = json.substring(from, end);
        Matcher named = Pattern.compile("\"" + key + "\"\\s*:\\s*\\{[^{}]*\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(slice);
        if (named.find()) return unescape(named.group(1));
        Matcher plain = Pattern.compile("\"" + key + "\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\"|-?[0-9]+(?:\\.[0-9]+)?)").matcher(slice);
        if (!plain.find()) return "";
        String raw = plain.group(1);
        if (raw.startsWith("\"")) raw = unescape(raw.substring(1, raw.length() - 1));
        return raw;
    }

    private String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\n", " ").replace("\\/", "/");
    }

    private String strip(String value) {
        return value.replaceAll("<[^>]+>", "").trim();
    }
}
