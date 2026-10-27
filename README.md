# FyConnect

**Areofy Web CMS ↔ Minecraft eklentisi**

FyConnect, [Areofy Web CMS](https://areofy.net) kullanan her Minecraft sunucusunun web sitesiyle entegre olmasını sağlar. Market ürün teslimati, haberler, duyurular, oyuncu önerileri ve güncelleme notları doğrudan oyun içinden erişilebilir hale gelir.

---

## Özellikler

| Özellik | Açıklama |
|---|---|
| 🛒 Market teslimati | Siteden yapılan alışverişlerin komutları otomatik çalıştırılır |
| 📦 Per-player çevrimdışı kuyruk | Oyuncu sunucuda değilse komut sadece o oyuncuya ait sıraya alınır, girişte teslim edilir |
| 📰 Haberler | Sitenizdeki son haberler oyun içi menüde görüntülenir |
| 📢 Duyurular | Resmi duyurular envanter menüsünde listelenir |
| 💡 Öneriler | Oyuncuların site önerileri menüden görüntülenebilir |
| 🔄 Güncelleme Notları | Changelog kayıtları oyun içinden takip edilebilir |
| 🔑 Otomatik API Key | İlk açılışta güvenli anahtar otomatik oluşturulur |

---

## Gereksinimler

- **Paper / Spigot / Purpur / Folia** 1.20.x veya üstü
- **Java** 17 veya üstü
- **Areofy Web CMS** kurulu ve erişilebilir bir site

---

## Kurulum

1. `FyConnect-1.0.0.jar` dosyasını sunucunun `plugins/` klasörüne kopyalayın.
2. Sunucuyu başlatın. Eklenti otomatik olarak `plugins/FyConnect/config.yml` dosyasını oluşturur ve bir API anahtarı üretir.
3. Konsol çıktısında şunu göreceksiniz:
   ```
   [FyConnect] Yeni API anahtari otomatik olusturuldu: abc123...
   [FyConnect] FyConnect hazir. Port 4580 dinleniyor.
   [FyConnect] API Key: abc123...
   ```
4. `config.yml` dosyasını açın ve `site-url` değerini güncelleyin:
   ```yaml
   site-url: "https://siteniz.com"
   ```
5. Güvenlik duvarınızda `4580` portuna panelin IP adresinden erişim izni verin.

---

## Areofy Panel Bağlantısı

Panel → **Entegrasyonlar** → **Oyun İçi Eklentiler** → **Sunucu Ekle**

| Alan | Değer |
|---|---|
| Bağlantı Türü | FyConnect |
| API URL | `http://SUNUCU_IP:4580` |
| API Key | `config.yml` içindeki `api-key` değeri |

Bağlantıyı test etmek için **Ping** butonunu kullanın. Aşağıdaki yanıt geliyorsa kurulum tamamdır:
```json
{ "ok": true, "plugin": "FyConnect", "platform": "Paper/Spigot", "online": 0 }
```

---

## Komutlar

| Komut | Açıklama | Yetki |
|---|---|---|
| `/site` | Ana site menüsünü açar | Herkes |
| `/market` | Market menüsünü doğrudan açar | Herkes |
| `/haberler` | Haberler menüsünü doğrudan açar | Herkes |
| `/duyurular` | Duyurular menüsünü doğrudan açar | Herkes |
| `/site reload` | Ayarları yeniden yükler | `fyconnect.admin` (op) |
| `/site sync` | Siteden içerikleri hemen günceller | `fyconnect.admin` (op) |

Alternatif komutlar: `/web`, `/fyconnect`

---

## config.yml

```yaml
port: 4580
api-key: ""
site-url: "https://siteniz.com"
queue-offline-commands: true
sync-interval-seconds: 120

menu:
  market: true
  haberler: true
  duyurular: true
  oneriler: true
  guncelleme: true

messages:
  prefix: "&6Web &8» &r"
  menu-title: "&8Site Menusu"
  market-title: "&8Market"
  haberler-title: "&8Haberler"
  duyurular-title: "&8Duyurular"
  oneriler-title: "&8Oneriler"
  guncelleme-title: "&8Guncelleme Notlari"
  no-permission: "&cBu komutu kullanma yetkin yok."
  reloaded: "&aFyConnect ayarlari yenilendi."
  sync-ok: "&aSiteden icerik basariyla guncellendi."
  sync-fail: "&cSiteye ulasilamadi, tekrar dene."
  delivered: "&aSatin aldigin &e{product} &ahesabina eklendi."
  pending-delivered: "&aCevrimdisiyken aldigin &e{count} &aurun hesabina teslim edildi."
  empty: "&7Burada henuz icerik yok."
```

---

## Market Teslimati Nasıl Çalışır?

1. Oyuncu siteden ürün satın alır.
2. Panel, `POST http://SUNUCU_IP:4580/command` isteği gönderir:
   ```json
   { "command": "give Steve diamond 64", "player": "Steve" }
   ```
   Header: `x-api-key: <api-key>`
3. Eklenti `player` alanını okur ve oyuncunun online olup olmadığını kontrol eder.
4. Oyuncu **online** → komut anında çalışır.
5. Oyuncu **çevrimdışı** → komut `plugins/FyConnect/queue.yml` dosyasına o oyuncuya özel kaydedilir.
6. Oyuncu sunucuya girdiğinde **sadece kendi komutları** çalışır ve bilgi mesajı gönderilir.

---

## HTTP API Referansı

### `GET /ping`
Header: `x-api-key: <key>`

```json
{
  "ok": true,
  "plugin": "FyConnect",
  "version": "1.0.0",
  "platform": "Paper/Spigot",
  "online": 12
}
```

### `POST /command`
Header: `x-api-key: <key>`, `Content-Type: application/json`

```json
{ "command": "eco give Steve 1000", "player": "Steve" }
```

`player` alanı opsiyoneldir. Belirtilirse çevrimdışı kontrolü yapılır. Belirtilmezse komut anında çalışır.

```json
{ "ok": true, "message": "Komut alindi." }
```

---

## Birden Fazla Sunucu

Her sunucuya ayrı JAR kopyalanır. Her sunucu ilk açılışta kendi API anahtarını üretir. Farklı makinelerde aynı port kullanılabilir; aynı makinede her sunucu için `config.yml`'de farklı `port` değeri ayarlanmalıdır.

---

## Derleme (Kaynak Koddan)

```bash
git clone https://github.com/areofy/fyconnect
cd fyconnect
mvn package
```

Çıktı: `target/FyConnect-1.0.0.jar` — Gereksinimler: JDK 17+, Maven 3.8+

---

## Uyumluluk

| Platform | Durum |
|---|---|
| Paper 1.20.x – 1.21.x | ✅ Tam uyumlu |
| Spigot 1.20.x – 1.21.x | ✅ Destekleniyor |
| Purpur 1.20.x – 1.21.x | ✅ Destekleniyor |
| Folia 1.20.x – 1.21.x | ✅ Destekleniyor |

---

## Lisans

MIT © [Areofy](https://areofy.net)
