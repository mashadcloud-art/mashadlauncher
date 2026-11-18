"""
Mashad Launcher — Automated Indian EPG & Live Sports Scraper
Fetches live XMLTV schedules, maps Indian regional & sports channels,
and outputs a high-performance live_guide.json for Android TV.
"""

import gzip
import json
import urllib.request
import xml.etree.ElementTree as ET
from datetime import datetime, timezone, timedelta

EPG_URL = "https://mitthu786.github.io/tvepg/jiotv/epg.xml.gz"

CHANNEL_MAP = {
    "443": {
        "channel": "Asianet HD",
        "region": "Malayalam",
        "category": "Entertainment",
        "ott_name": "JioHotstar",
        "package_name": "in.startv.hotstar",
        "deep_link": "https://www.hotstar.com/in/channels/asianet",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/asianet-hd/ASIANET_HD",
        "satellite": "Tata Play",
        "is_featured_alert": False
    },
    "459": {
        "channel": "Asianet Movies HD",
        "region": "Malayalam",
        "category": "Movies",
        "ott_name": "JioHotstar",
        "package_name": "in.startv.hotstar",
        "deep_link": "https://www.hotstar.com/in/channels/asianet-movies",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/asianet-movies/ASIANET_MOVIES",
        "satellite": "Tata Play",
        "is_featured_alert": False
    },
    "180": {
        "channel": "Asianet News",
        "region": "Malayalam",
        "category": "News",
        "ott_name": "JioHotstar",
        "package_name": "in.startv.hotstar",
        "deep_link": "https://www.hotstar.com/in/channels/asianet-news",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/asianet-news/ASIANET_NEWS",
        "satellite": "Tata Play",
        "is_featured_alert": False
    },
    "844": {
        "channel": "Mazhavil Manorama HD",
        "region": "Malayalam",
        "category": "Entertainment",
        "ott_name": "JioHotstar",
        "package_name": "in.startv.hotstar",
        "deep_link": "https://www.hotstar.com/in/channels/mazhavil-manorama",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/mazhavil-manorama-hd/MAZHAVIL_MANORAMA_HD",
        "satellite": "Tata Play",
        "is_featured_alert": False
    },
    "662": {
        "channel": "Manorama News",
        "region": "Malayalam",
        "category": "News",
        "ott_name": "Airtel Xstream",
        "package_name": "tv.airtel.xstream.tvapp",
        "deep_link": "https://www.airtelxstream.in/livetv-channels/manorama-news/MANORAMA_NEWS",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/manorama-news/MANORAMA_NEWS",
        "satellite": "Airtel DTH",
        "is_featured_alert": False
    },
    "900": {
        "channel": "Sun Surya HD",
        "region": "Malayalam",
        "category": "Entertainment",
        "ott_name": "Sun NXT",
        "package_name": "com.suntv.sunnxt",
        "deep_link": "https://www.sunnxt.com/live-tv",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/surya-tv-hd/SURYA_TV_HD",
        "satellite": "Sun Direct",
        "is_featured_alert": False
    },
    "896": {
        "channel": "Sun TV HD",
        "region": "Tamil",
        "category": "Entertainment",
        "ott_name": "Sun NXT",
        "package_name": "com.suntv.sunnxt",
        "deep_link": "https://www.sunnxt.com/live-tv",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/sun-tv-hd/SUN_TV_HD",
        "satellite": "Sun Direct",
        "is_featured_alert": False
    },
    "368": {
        "channel": "Star Vijay HD",
        "region": "Tamil",
        "category": "Entertainment",
        "ott_name": "JioHotstar",
        "package_name": "in.startv.hotstar",
        "deep_link": "https://www.hotstar.com/in/channels/star-vijay",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/star-vijay-hd/STAR_VIJAY_HD",
        "satellite": "Tata Play",
        "is_featured_alert": False
    },
    "162": {
        "channel": "Sony Ten 1 HD",
        "region": "International",
        "category": "Football",
        "ott_name": "SonyLIV",
        "package_name": "com.sonyliv",
        "deep_link": "https://www.sonyliv.com/custompage/sports",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/sony-ten-1-hd/SONY_TEN_1_HD",
        "satellite": "Tata Play",
        "is_featured_alert": True
    },
    "891": {
        "channel": "Sony Ten 2 HD",
        "region": "International",
        "category": "Football",
        "ott_name": "SonyLIV",
        "package_name": "com.sonyliv",
        "deep_link": "https://www.sonyliv.com/custompage/sports",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/sony-ten-2-hd/SONY_TEN_2_HD",
        "satellite": "Tata Play",
        "is_featured_alert": True
    },
    "892": {
        "channel": "Sony Ten 3 HD Hindi",
        "region": "Hindi",
        "category": "Cricket",
        "ott_name": "SonyLIV",
        "package_name": "com.sonyliv",
        "deep_link": "https://www.sonyliv.com/custompage/sports",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/sony-ten-3-hd/SONY_TEN_3_HD",
        "satellite": "Tata Play",
        "is_featured_alert": True
    },
    "1772": {
        "channel": "Sony Ten 4 HD Tamil",
        "region": "Tamil",
        "category": "Cricket",
        "ott_name": "SonyLIV",
        "package_name": "com.sonyliv",
        "deep_link": "https://www.sonyliv.com/custompage/sports",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/sony-ten-4-hd/SONY_TEN_4_HD",
        "satellite": "Tata Play",
        "is_featured_alert": True
    },
    "155": {
        "channel": "Sony Ten 5 HD",
        "region": "International",
        "category": "Sports",
        "ott_name": "SonyLIV",
        "package_name": "com.sonyliv",
        "deep_link": "https://www.sonyliv.com/custompage/sports",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/sony-ten-5-hd/SONY_TEN_5_HD",
        "satellite": "Tata Play",
        "is_featured_alert": True
    },
    "1984": {
        "channel": "Star Sports 2 Hindi HD",
        "region": "Hindi",
        "category": "Cricket",
        "ott_name": "JioHotstar",
        "package_name": "in.startv.hotstar",
        "deep_link": "https://www.hotstar.com/in/sports/cricket",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/star-sports-1-hd/STAR_SPORTS_1_HD",
        "satellite": "Tata Play",
        "is_featured_alert": True
    },
    "1132": {
        "channel": "Star Plus HD",
        "region": "Hindi",
        "category": "Entertainment",
        "ott_name": "JioHotstar",
        "package_name": "in.startv.hotstar",
        "deep_link": "https://www.hotstar.com/in/channels/star-plus",
        "xstream_uri": "https://www.airtelxstream.in/livetv-channels/star-plus-hd/STAR_PLUS_HD",
        "satellite": "Tata Play",
        "is_featured_alert": False
    }
}

def parse_xmltv_time(ts: str) -> int:
    ts = ts.strip()
    try:
        dt = datetime.strptime(ts, "%Y%m%d%H%M%S %z")
        return int(dt.timestamp() * 1000)
    except Exception:
        return 0

def generate_live_guide():
    req = urllib.request.Request(EPG_URL, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req) as response:
        with gzip.GzipFile(fileobj=response) as gz:
            tree = ET.parse(gz)
            root = tree.getroot()

    now_ms = int(datetime.now(timezone.utc).timestamp() * 1000)
    program_list = []

    for prog in root.findall("programme"):
        ch_id = prog.get("channel")
        if ch_id not in CHANNEL_MAP:
            continue

        info = CHANNEL_MAP[ch_id]
        start_ms = parse_xmltv_time(prog.get("start", ""))
        stop_ms = parse_xmltv_time(prog.get("stop", ""))

        title_node = prog.find("title")
        title = title_node.text.strip() if title_node is not None and title_node.text else info["channel"]

        desc_node = prog.find("desc")
        desc = desc_node.text.strip() if desc_node is not None and desc_node.text else ""

        cat = info["category"]
        is_alert = info["is_featured_alert"]

        upper_title = title.upper()
        if any(w in upper_title for w in ["ISL", "KERALA BLASTERS", "MOHUN BAGAN", "BENGALURU FC", "FOOTBALL", "UEFA", "PREMIER LEAGUE"]):
            cat = "ISL Football" if "ISL" in upper_title or "BLASTERS" in upper_title else "Football"
            is_alert = True
        elif any(w in upper_title for w in ["CRICKET", "IND VS", "TEST", "T20", "ODI", "BGT", "IPL", "ASIA CUP"]):
            cat = "Cricket"
            is_alert = True
        elif "BIGG BOSS" in upper_title:
            is_alert = True

        st_dt = datetime.fromtimestamp(start_ms / 1000, tz=timezone(timedelta(hours=5, minutes=30)))
        et_dt = datetime.fromtimestamp(stop_ms / 1000, tz=timezone(timedelta(hours=5, minutes=30)))

        program_list.append({
            "id": f"{ch_id}_{start_ms}",
            "channel": info["channel"],
            "title": title,
            "time_range": f"{st_dt.strftime('%I:%M %p')} - {et_dt.strftime('%I:%M %p')}",
            "desc": desc,
            "region": info["region"],
            "category": cat,
            "satellite": info["satellite"],
            "ott_name": info["ott_name"],
            "package_name": info["package_name"],
            "deep_link": info["deep_link"],
            "xstream_uri": info["xstream_uri"],
            "start_time": start_ms,
            "end_time": stop_ms,
            "is_featured_alert": is_alert
        })

    program_list.sort(key=lambda x: x["start_time"])

    with open("live_guide.json", "w", encoding="utf-8") as f:
        json.dump(program_list, f, indent=2, ensure_ascii=False)

    print(f"Successfully generated live_guide.json ({len(program_list)} programs).")

if __name__ == "__main__":
    generate_live_guide()
