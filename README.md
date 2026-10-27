# FyConnect

**Areofy Web CMS ↔ Minecraft Paper eklentisi**

FyConnect, [Areofy Web CMS](https://areofy.net) kullanan her Minecraft sunucusunun web sitesiyle entegre olmasını sağlar. Market ürün teslimati, haberler, duyurular, oyuncu önerileri ve güncelleme notları doğrudan oyun içinden erişilebilir hale gelir.

---

**Docbytexd Tarafından Yapıldı**

## Özellikler

| Özellik | Açıklama |
|---|---|
| 🛒 Market teslimati | Siteden yapılan alışverişlerin komutları otomatik çalıştırılır |
| 📦 Çevrimdışı kuyruk | Oyuncu sunucuda değilse komut sıraya alınır, girişte teslim edilir |
| 📰 Haberler | Sitenizdeki son haberler oyun içi menüde görüntülenir |
| 📢 Duyurular | Resmi duyurular envanter menüsünde listelenir |
| 💡 Öneriler | Oyuncuların site önerileri menüden görüntülenebilir |
| 🔄 Güncelleme Notları | Changelog kayıtları oyun içinden takip edilebilir |
| 🔑 Otomatik API Key | İlk açılışta güvenli anahtar otomatik oluşturulur |

---

## Gereksinimler

- **Paper** 1.21 veya üstü
- **Java** 21 veya üstü
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
4. `config.yml` dosyasını düzenleyin:
   ```yaml
   site-url: "https://siteniz.com"   # Web CMS adresiniz
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

Bağlantıyı test etmek için **Ping** butonunu kullanın. `{"ok":true,"plugin":"FyConnect"}` yanıtı geliyorsa kurulum tamamdır.

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

## config.yml Açıklaması

```yaml
# HTTP API portu — güvenlik duvarında bu porta panelin IP'sinden izin verin
port: 4580

# API anahtarı — ilk açılışta otomatik oluşturulur, değiştirmeye gerek yok
api-key: ""

# Web CMS adresiniz (Areofy Web CMS kullanan her site için geçerlidir)
site-url: "https://siteniz.com"

# Çevrimdışı oyuncuların komutlarını sıraya al, girişte teslim et
queue-offline-commands: true

# İçerik güncelleme sıklığı (saniye)
sync-interval-seconds: 120

# Ana menüde hangi bölümler görünsün
menu:
  market: true
  haberler: true
  duyurular: true
  oneriler: true
  guncelleme: true

messages:
  # Komut öneki — sunucu adınızla değiştirin
  prefix: "&6Web &8» &r"
  menu-title: "&8Site Menüsü"
  # ... diğer mesajlar config.yml içinde özelleştirilebilir
```

---

## Market Teslimati Nasıl Çalışır?

1. Oyuncu siteden ürün satın alır.
2. Panel, `POST http://SUNUCU_IP:4580/command` isteği gönderir:
   ```json
   { "command": "give SteveOyuncu diamond 64" }
   ```
   Header: `x-api-key: <api-key>`
3. Eklenti komutu konsol üzerinden çalıştırır.
4. Oyuncu çevrimdışıysa komut `plugins/FyConnect/queue.yml` dosyasına kaydedilir.
5. Oyuncu sunucuya girdiğinde kuyruk otomatik işlenir ve oyuncuya bilgi mesajı gönderilir.

---

## HTTP API Referansı

### `GET /ping`
Bağlantı testi. Header: `x-api-key: <key>`

**Yanıt:**
```json
{ "ok": true, "plugin": "FyConnect", "online": 12 }
```

### `POST /command`
Komut çalıştır. Header: `x-api-key: <key>`, `Content-Type: application/json`

**İstek:**
```json
{ "command": "eco give SteveOyuncu 100" }
```

**Yanıt:**
```json
{ "ok": true, "message": "Komut alindi." }
```

---

## Derleme (Kaynak Koddan)

```bash
# Gereksinimler: JDK 21+, Maven 3.8+
git clone https://github.com/areofy/fyconnect
cd fyconnect
mvn package
# Çıktı: target/FyConnect-1.0.0.jar
```

---

## Uyumluluk

| Platform | Durum |
|---|---|
| Paper 1.21.x | ✅ Tam uyumlu |
| Paper 1.20.x | ⚠️ Test edilmedi |
| Spigot | ❌ Desteklenmiyor |
| Folia | ❌ Desteklenmiyor |

---

## Lisans

MIT © [Areofy](https://areofy.net)
