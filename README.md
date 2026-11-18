# 📺 Mashad Launcher & Live Dock

> **Clean, Intelligent TV Home & Single-Event EPG Dock for Android TV**  
> Tailored for Indian Sports (Cricket, ISL Football, UEFA) and Regional TV (Malayalam, Tamil, Hindi).

---

## ✨ Features

- **⚡ Single-Event Compact Dock:** Highlights ONE match or show at a time with clean frosted glass styling (`glass-dock`).
- **🔴 Live Scores Badge:** Real-time live score updates (`⚡ IND 248/4 (42.3 ov)`, `⚡ KBFC 2 - 1 MBSG • 68'`).
- **🎯 Dual 1-Click Launching:**
  - **`Watch` Button:** Direct launch into primary broadcaster (**JioHotstar**, **SonyLIV**, **Sun NXT**).
  - **`Xstream Free` Button:** Direct 1-click launch into **Airtel Xstream Play** (`tv.airtel.xstream.tvapp`).
- **🎮 Full Remote D-Pad Navigation:** Built for Android TV remotes with smooth focus rings and `‹` / `›` Prev/Next switcher.
- **🌴 Multi-Region & Language Filter:** Malayalam, Tamil, Hindi, and International.
- **🔄 Free 24/7 Cloud Sync:** GitHub Actions automatically scrape official XMLTV feeds every 6 hours and update `live_guide.json`.

---

## 📂 Repository Structure

```
├── index.html                   # Web TV full launcher application
├── live_dock.html               # Single-Event compact dock widget (TV-Bro / WebApp)
├── live_guide.json              # Scraped live EPG & sports dataset
├── update_epg.py                # Automated Indian EPG & Sports scraper
├── MashadEpgEngine.kt           # Kotlin Android TV engine for Jetpack Compose & CleanMyTV
└── .github/
    └── workflows/
        └── sync.yml             # GitHub Action auto-syncing live EPG every 6 hours
```

---

## 🚀 Quick Start on Android TV

### 1. View Live Dock in Any TV Browser (TV-Bro / JioPages)
Simply open `index.html` or `live_dock.html` on your Android TV:
```bash
adb shell am start -a android.intent.action.VIEW -d "https://mashadcloud-art.github.io/mashadlauncher/" -n com.phlox.tvwebbrowser/.activity.main.MainActivity
```

### 2. Auto-Sync Schedule
To scrape fresh schedules locally:
```bash
python update_epg.py
```
This downloads the latest XMLTV guide from `mitthu786/tvepg`, parses active programmes, and regenerates `live_guide.json`.

---

## 📡 Broadcaster & Package Reference

| Network / Platform | Android TV Package | URL Scheme | Supported Content |
| :--- | :--- | :--- | :--- |
| **JioHotstar** | `in.startv.hotstar` | `hotstar://...` | Cricket, ISL Football, Asianet, Star Vijay |
| **SonyLIV** | `com.sonyliv` | `sonyliv://...` | UEFA Champions League, Sony Sports, Cricket |
| **Airtel Xstream** | `tv.airtel.xstream.tvapp` | `airtelxstream://...` | Manorama News, Live Channels Free |
| **Sun NXT** | `com.suntv.sunnxt` | `sunnxt://...` | Sun TV, Sun Surya HD, South Movies |

---

## 📄 License
MIT License. Built for the Android TV and Indian streaming community.
