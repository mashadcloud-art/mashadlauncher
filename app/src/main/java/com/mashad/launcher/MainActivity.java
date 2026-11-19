package com.mashad.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private ImageView ivWallpaper;
    private TextView tvClock, tvDate, tvLiveStatus, tvWeather;
    private TextView tvLiveBadge, tvEventCounter, tvChannelBadge, tvLiveScore, tvVenueTag;
    private TextView tvEventTitle, tvEventSubtitle, tvEventDetails;
    private TextView tvAppsCount;
    private Button btnPrev, btnNext, btnWatch, btnXstream;
    private Button btnFilterAll, btnFilterCricket, btnFilterFootball, btnFilterOtherSports, btnFilterMalayalam, btnFilterTamil, btnFilterHindi;
    private RecyclerView rvApps;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<LiveItem> allLiveItems = new ArrayList<>();
    private final List<LiveItem> filteredLiveItems = new ArrayList<>();
    private int currentEventIndex = 0;
    private String activeFilter = "ALL";

    private final List<String> assetWallpapers = new ArrayList<>();
    private final List<File> sdcardWallpapers = new ArrayList<>();
    private int currentWallpaperIndex = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("mashad_launcher_prefs", MODE_PRIVATE);
        activeFilter = prefs.getString("active_filter", "ALL");

        bindViews();
        startClockAndWeather();
        loadMultiSportsAndEpgData();
        setupFilterStrip();
        setupLiveDockControls();
        loadAllInstalledApps();
        setupFamilyWallpaperEngine();
    }

    private void bindViews() {
        ivWallpaper = findViewById(R.id.ivWallpaper);
        tvClock = findViewById(R.id.tvClock);
        tvDate = findViewById(R.id.tvDate);
        tvLiveStatus = findViewById(R.id.tvLiveStatus);
        tvWeather = findViewById(R.id.tvWeather);

        tvLiveBadge = findViewById(R.id.tvLiveBadge);
        tvEventCounter = findViewById(R.id.tvEventCounter);
        tvChannelBadge = findViewById(R.id.tvChannelBadge);
        tvLiveScore = findViewById(R.id.tvLiveScore);
        tvVenueTag = findViewById(R.id.tvVenueTag);

        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventSubtitle = findViewById(R.id.tvEventSubtitle);
        tvEventDetails = findViewById(R.id.tvEventDetails);
        tvAppsCount = findViewById(R.id.tvAppsCount);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnWatch = findViewById(R.id.btnWatch);
        btnXstream = findViewById(R.id.btnXstream);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterCricket = findViewById(R.id.btnFilterCricket);
        btnFilterFootball = findViewById(R.id.btnFilterFootball);
        btnFilterOtherSports = findViewById(R.id.btnFilterOtherSports);
        btnFilterMalayalam = findViewById(R.id.btnFilterMalayalam);
        btnFilterTamil = findViewById(R.id.btnFilterTamil);
        btnFilterHindi = findViewById(R.id.btnFilterHindi);

        rvApps = findViewById(R.id.rvApps);
        rvApps.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void startClockAndWeather() {
        final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());

        Runnable clockRunnable = new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                if (tvClock != null) tvClock.setText(timeFormat.format(now));
                if (tvDate != null) tvDate.setText(dateFormat.format(now));
                mainHandler.postDelayed(this, 1000);
            }
        };
        mainHandler.post(clockRunnable);

        if (tvWeather != null) {
            tvWeather.setText("⛅ 28°C Partly Cloudy");
        }
    }

    private void loadMultiSportsAndEpgData() {
        allLiveItems.clear();

        // 1. REAL CURRENT FIXTURE: India vs New Zealand (1st Test Wellington Live Today)
        allLiveItems.add(new LiveItem(
                "ind-nz-1st-test",
                "India vs New Zealand — 1st Test (Live Today)",
                "Sony Ten 1 HD / Star Sports",
                "SonyLIV, Hotstar & Airtel Xstream",
                "Cricket",
                "National",
                "03:30 AM IST (LIVE)",
                "⚡ 1st Test · Day 1",
                "📍 Cello Basin Reserve, Wellington, NZ",
                "India's Tour of New Zealand 2026. Live coverage from Wellington. Streaming on Airtel Xstream Play & SonyLIV.",
                "Cricket",
                "com.sonyliv",
                "https://www.sonyliv.com/custompage/sports",
                "https://www.airtelxstream.in/livetv-channels/sony-ten-1-hd/SONY_TEN_1_HD"
        ));

        // 2. UPCOMING FIXTURE: 2nd Test Christchurch
        allLiveItems.add(new LiveItem(
                "ind-nz-2nd-test",
                "India vs New Zealand — 2nd Test (Upcoming)",
                "Sony Ten 1 HD / Star Sports",
                "SonyLIV & Airtel Xstream",
                "Cricket",
                "National",
                "Nov 27 – Dec 1",
                "📅 UPCOMING",
                "📍 Hagley Oval, Christchurch",
                "2nd Test Match. Watch live on Airtel Xstream Play and SonyLIV.",
                "Cricket",
                "com.sonyliv",
                "https://www.sonyliv.com/custompage/sports",
                "https://www.airtelxstream.in/livetv-channels/sony-ten-1-hd/SONY_TEN_1_HD"
        ));

        // 3. UPCOMING: India vs Sri Lanka ODI Series
        allLiveItems.add(new LiveItem(
                "ind-sl-odi",
                "India vs Sri Lanka — 1st ODI (Upcoming)",
                "Star Sports 1 HD / JioHotstar",
                "JioHotstar & Airtel Xstream",
                "Cricket",
                "National",
                "December 2026",
                "📅 UPCOMING",
                "📍 Arun Jaitley Stadium, Delhi",
                "Sri Lanka Tour of India 2026 (3 ODIs, 3 T20Is). Live on JioHotstar & Airtel Xstream Play.",
                "Cricket",
                "in.startv.hotstar",
                "https://www.hotstar.com/in/sports/cricket",
                "https://www.airtelxstream.in/livetv-channels/star-sports-1-hd/STAR_SPORTS_1_HD"
        ));

        // 4. UPCOMING: Border-Gavaskar Trophy 2027
        allLiveItems.add(new LiveItem(
                "bgt-2027",
                "Australia vs India — 1st Test (Border-Gavaskar Trophy)",
                "Star Sports 1 HD",
                "JioHotstar & Airtel Xstream",
                "Cricket",
                "National",
                "Starts Jan 21, 2027",
                "🏆 BGT 2027",
                "📍 VCA Stadium, Nagpur",
                "5-Test World Test Championship Series. Live on Star Sports & Airtel Xstream Play.",
                "Cricket",
                "in.startv.hotstar",
                "https://www.hotstar.com/in/sports/cricket",
                "https://www.airtelxstream.in/livetv-channels/star-sports-1-hd/STAR_SPORTS_1_HD"
        ));

        // 5. FOOTBALL (ISL & UEFA)
        allLiveItems.add(new LiveItem(
                "sport-isl-kbfc",
                "Kerala Blasters vs Mohun Bagan Super Giant",
                "Star Sports 3 / Sports18",
                "JioHotstar & Airtel Xstream",
                "Football",
                "Malayalam",
                "TONIGHT 07:30 PM",
                "⚽ ISL MATCH",
                "📍 JLN Stadium, Kochi",
                "ISL Blockbuster clash. Live with Malayalam commentary on Airtel Xstream Play & JioHotstar.",
                "Football",
                "in.startv.hotstar",
                "https://www.hotstar.com/in/sports/football",
                "https://www.airtelxstream.in/livetv-channels"
        ));

        allLiveItems.add(new LiveItem(
                "sport-uefa-germany",
                "UEFA Nations League: Germany vs Netherlands",
                "Sony Ten 2 HD",
                "SonyLIV & Airtel Xstream",
                "Football",
                "International",
                "LIVE NOW",
                "⚽ UEFA",
                "📍 Allianz Arena, Munich",
                "European championship showdown. Live on SonyLIV & Airtel Xstream Play.",
                "Football",
                "com.sonyliv",
                "https://www.sonyliv.com/custompage/sports",
                "https://www.airtelxstream.in/livetv-channels/sony-ten-2-hd/SONY_TEN_2_HD"
        ));

        // 6. OTHER SPORTS (PKL, Tennis, F1)
        allLiveItems.add(new LiveItem(
                "sport-pkl-puneri",
                "Pro Kabaddi: Puneri Paltan vs Patna Pirates",
                "Star Sports 2 HD",
                "JioHotstar",
                "Kabaddi",
                "National",
                "TONIGHT 08:30 PM",
                "🥊 PKL",
                "📍 Gachibowli Stadium, Hyderabad",
                "PKL Season 11. Super Raid clash. Live on Star Sports & JioHotstar.",
                "Kabaddi",
                "in.startv.hotstar",
                "https://www.hotstar.com/in/sports/kabaddi",
                "https://www.airtelxstream.in/livetv-channels"
        ));

        allLiveItems.add(new LiveItem(
                "sport-tennis-finals",
                "ATP Masters Finals: Alcaraz vs Sinner",
                "Sony Ten 5 HD",
                "SonyLIV & Airtel Xstream",
                "Tennis",
                "International",
                "LIVE NOW",
                "🎾 ATP",
                "📍 Inalpi Arena, Turin",
                "Championship Title Decider. Live on Sony Ten 5 HD & Airtel Xstream Play.",
                "Tennis",
                "com.sonyliv",
                "https://www.sonyliv.com/custompage/sports",
                "https://www.airtelxstream.in/livetv-channels/sony-ten-5-hd/SONY_TEN_5_HD"
        ));

        // 7. REGIONAL PRIME SHOWS
        allLiveItems.add(new LiveItem(
                "reg-asianet-bb",
                "Bigg Boss Malayalam",
                "Asianet HD",
                "JioHotstar & Airtel Xstream",
                "Entertainment",
                "Malayalam",
                "09:30 PM",
                "🌴 MALAYALAM",
                "📍 Asianet Studio",
                "Daily Prime Episode. Mohanlal hosts. Live on JioHotstar & Airtel Xstream Play.",
                "Malayalam",
                "in.startv.hotstar",
                "https://www.hotstar.com/in/channels/asianet",
                "https://www.airtelxstream.in/livetv-channels/asianet-hd/ASIANET_HD"
        ));

        allLiveItems.add(new LiveItem(
                "reg-mazhavil-bumper",
                "Oru Chiri Iru Chiri Bumper Chiri",
                "Mazhavil Manorama HD",
                "ManoramaMAX & Airtel Xstream",
                "Entertainment",
                "Malayalam",
                "08:00 PM",
                "🌴 MALAYALAM",
                "📍 Manorama Studios",
                "Comedy Championship. Live on Airtel Xstream Play & ManoramaMAX.",
                "Malayalam",
                "tv.airtel.xstream.tvapp",
                "https://www.manoramamax.com",
                "https://www.airtelxstream.in/livetv-channels/mazhavil-manorama-hd/MAZHAVIL_MANORAMA_HD"
        ));

        allLiveItems.add(new LiveItem(
                "reg-vijay-singer",
                "Super Singer Season 10",
                "Star Vijay HD",
                "JioHotstar & Airtel Xstream",
                "Music Reality",
                "Tamil",
                "08:00 PM",
                "🎬 TAMIL",
                "📍 Chennai Studios",
                "Musical Championship. Live on JioHotstar & Airtel Xstream Play.",
                "Tamil",
                "in.startv.hotstar",
                "https://www.hotstar.com/in/channels/star-vijay",
                "https://www.airtelxstream.in/livetv-channels/star-vijay-hd/STAR_VIJAY_HD"
        ));

        // Also append live_guide.json if present
        try {
            InputStream is = getAssets().open("live_guide.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.optString("id", "json-" + i);
                String channel = obj.optString("channel", "Indian TV");
                String title = obj.optString("title", "Live Broadcast");
                String timeRange = obj.optString("time_range", "LIVE NOW");
                String region = obj.optString("region", "National");
                String category = obj.optString("category", "General");
                String ottName = obj.optString("ott_name", "JioHotstar");
                String pkgName = obj.optString("package_name", "in.startv.hotstar");
                String deepLink = obj.optString("deep_link", "https://www.hotstar.com");
                String xstreamUri = obj.optString("xstream_uri", "https://www.airtelxstream.in/livetv-channels");

                String sport = category;
                if (title.toLowerCase(Locale.ROOT).contains("cricket")) sport = "Cricket";
                else if (title.toLowerCase(Locale.ROOT).contains("football") || title.toLowerCase(Locale.ROOT).contains("soccer")) sport = "Football";

                allLiveItems.add(new LiveItem(id, title, channel, ottName, category, region, timeRange, "", "📍 Live TV", title + " on " + channel + ".", sport, pkgName, deepLink, xstreamUri));
            }
        } catch (Exception ignored) {}

        applyFilter(activeFilter);
    }

    private void setupFilterStrip() {
        btnFilterAll.setOnClickListener(v -> applyFilter("ALL"));
        btnFilterCricket.setOnClickListener(v -> applyFilter("CRICKET"));
        btnFilterFootball.setOnClickListener(v -> applyFilter("FOOTBALL"));
        btnFilterOtherSports.setOnClickListener(v -> applyFilter("OTHER_SPORTS"));
        btnFilterMalayalam.setOnClickListener(v -> applyFilter("MALAYALAM"));
        btnFilterTamil.setOnClickListener(v -> applyFilter("TAMIL"));
        btnFilterHindi.setOnClickListener(v -> applyFilter("HINDI"));

        updateFilterChipStyles();
    }

    private void applyFilter(String filterKey) {
        activeFilter = filterKey;
        prefs.edit().putString("active_filter", activeFilter).apply();

        filteredLiveItems.clear();
        for (LiveItem item : allLiveItems) {
            if (filterKey.equals("ALL")) {
                filteredLiveItems.add(item);
            } else if (filterKey.equals("CRICKET")) {
                if ("Cricket".equalsIgnoreCase(item.sportType) || item.title.toLowerCase(Locale.ROOT).contains("cricket") || item.title.toLowerCase(Locale.ROOT).contains("ind vs") || item.title.toLowerCase(Locale.ROOT).contains("india vs") || item.title.toLowerCase(Locale.ROOT).contains("bgt")) {
                    filteredLiveItems.add(item);
                }
            } else if (filterKey.equals("FOOTBALL")) {
                if ("Football".equalsIgnoreCase(item.sportType) || item.title.toLowerCase(Locale.ROOT).contains("football") || item.title.toLowerCase(Locale.ROOT).contains("soccer") || item.title.toLowerCase(Locale.ROOT).contains("kbfc") || item.title.toLowerCase(Locale.ROOT).contains("uefa") || item.title.toLowerCase(Locale.ROOT).contains("isl")) {
                    filteredLiveItems.add(item);
                }
            } else if (filterKey.equals("OTHER_SPORTS")) {
                if ("Kabaddi".equalsIgnoreCase(item.sportType) || "Tennis".equalsIgnoreCase(item.sportType) || "F1".equalsIgnoreCase(item.sportType) || "Badminton".equalsIgnoreCase(item.sportType)) {
                    filteredLiveItems.add(item);
                }
            } else if (filterKey.equals("MALAYALAM")) {
                if ("Malayalam".equalsIgnoreCase(item.region)) {
                    filteredLiveItems.add(item);
                }
            } else if (filterKey.equals("TAMIL")) {
                if ("Tamil".equalsIgnoreCase(item.region)) {
                    filteredLiveItems.add(item);
                }
            } else if (filterKey.equals("HINDI")) {
                if ("Hindi".equalsIgnoreCase(item.region)) {
                    filteredLiveItems.add(item);
                }
            }
        }

        if (filteredLiveItems.isEmpty()) {
            filteredLiveItems.addAll(allLiveItems);
        }

        currentEventIndex = 0;
        renderEvent(0);
        updateFilterChipStyles();

        if (tvLiveStatus != null) {
            tvLiveStatus.setText("● Showing " + filteredLiveItems.size() + " matches/shows for [" + activeFilter + "]");
        }
    }

    private void updateFilterChipStyles() {
        btnFilterAll.setSelected("ALL".equals(activeFilter));
        btnFilterCricket.setSelected("CRICKET".equals(activeFilter));
        btnFilterFootball.setSelected("FOOTBALL".equals(activeFilter));
        btnFilterOtherSports.setSelected("OTHER_SPORTS".equals(activeFilter));
        btnFilterMalayalam.setSelected("MALAYALAM".equals(activeFilter));
        btnFilterTamil.setSelected("TAMIL".equals(activeFilter));
        btnFilterHindi.setSelected("HINDI".equals(activeFilter));
    }

    private void renderEvent(int index) {
        if (filteredLiveItems.isEmpty()) return;
        currentEventIndex = Math.max(0, Math.min(index, filteredLiveItems.size() - 1));
        LiveItem item = filteredLiveItems.get(currentEventIndex);

        tvEventCounter.setText((currentEventIndex + 1) + " / " + filteredLiveItems.size());
        tvChannelBadge.setText(item.channel.toUpperCase(Locale.ROOT));

        tvEventTitle.setText(item.title);
        tvEventSubtitle.setText(item.channel + " • " + item.platform + " • " + item.time);
        tvEventDetails.setText(item.details.isEmpty() ? item.title + " streaming on " + item.platform + "." : item.details);

        if (item.liveScore != null && !item.liveScore.trim().isEmpty()) {
            tvLiveScore.setVisibility(View.VISIBLE);
            tvLiveScore.setText(item.liveScore);
        } else {
            tvLiveScore.setVisibility(View.GONE);
        }

        if (item.venue != null && !item.venue.trim().isEmpty()) {
            tvVenueTag.setVisibility(View.VISIBLE);
            tvVenueTag.setText(item.venue);
        } else {
            tvVenueTag.setVisibility(View.GONE);
        }

        btnWatch.setText("▶ " + (item.platform.contains("&") ? "WATCH LIVE" : item.platform.toUpperCase(Locale.ROOT)));
    }

    private void setupLiveDockControls() {
        btnPrev.setOnClickListener(v -> {
            int newIdx = currentEventIndex - 1;
            if (newIdx < 0) newIdx = filteredLiveItems.size() - 1;
            renderEvent(newIdx);
        });

        btnNext.setOnClickListener(v -> {
            int newIdx = currentEventIndex + 1;
            if (newIdx >= filteredLiveItems.size()) newIdx = 0;
            renderEvent(newIdx);
        });

        btnWatch.setOnClickListener(v -> {
            if (filteredLiveItems.isEmpty()) return;
            LiveItem item = filteredLiveItems.get(currentEventIndex);
            launchAppOrUrl(item.pkg, item.deepLink);
        });

        btnXstream.setOnClickListener(v -> {
            if (filteredLiveItems.isEmpty()) return;
            LiveItem item = filteredLiveItems.get(currentEventIndex);
            launchAirtelXstreamDirect(item.xstreamUri);
        });

        btnWatch.requestFocus();
    }

    private void launchAirtelXstreamDirect(String xstreamUri) {
        try {
            Intent xstreamIntent = new Intent();
            xstreamIntent.setComponent(new ComponentName("tv.airtel.xstream.tvapp", "tv.airtel.xstream.splash.SplashActivity"));
            xstreamIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(xstreamIntent);
        } catch (Exception e) {
            try {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(xstreamUri));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(webIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Could not open Airtel Xstream", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchAppOrUrl(String packageName, String fallbackUrl) {
        try {
            Intent launchIntent = null;
            PackageManager pm = getPackageManager();
            if (packageName != null && !packageName.isEmpty()) {
                launchIntent = pm.getLeanbackLaunchIntentForPackage(packageName);
                if (launchIntent == null) {
                    launchIntent = pm.getLaunchIntentForPackage(packageName);
                }
            }
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
            } else if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Opening channel...", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllInstalledApps() {
        PackageManager pm = getPackageManager();
        List<AppModel> appList = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        String myPkg = getPackageName();

        // Specific high-priority TV streaming and media apps
        String[] priorityApps = new String[] {
                "com.google.android.youtube.tv",
                "com.netflix.ninja",
                "com.amazon.amazonvideo.livingroom",
                "in.startv.hotstar",
                "com.sonyliv",
                "tv.airtel.xstream.tvapp",
                "com.nathnetwork.xciptv",
                "com.yablio.sendfilestotv",
                "com.alphainventor.filemanager",
                "com.android.vending",
                "com.google.android.play.games",
                "com.apps.usbmediaplayer",
                "com.apps.livetv",
                "com.clean.pairbluetooth",
                "com.gathin.tvhome"
        };

        for (String pkg : priorityApps) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                seen.add(pkg);
                String label = ai.loadLabel(pm).toString();
                if ("com.google.android.youtube.tv".equals(pkg)) label = "YouTube";
                else if ("com.netflix.ninja".equals(pkg)) label = "Netflix";
                else if ("com.amazon.amazonvideo.livingroom".equals(pkg)) label = "Prime Video";
                else if ("in.startv.hotstar".equals(pkg)) label = "JioHotstar";
                else if ("tv.airtel.xstream.tvapp".equals(pkg)) label = "Airtel Xstream";
                else if ("com.nathnetwork.xciptv".equals(pkg)) label = "XCIPTV";
                else if ("com.yablio.sendfilestotv".equals(pkg)) label = "Send Files";
                else if ("com.alphainventor.filemanager".equals(pkg)) label = "File Manager";
                else if ("com.android.vending".equals(pkg)) label = "Play Store";

                appList.add(new AppModel(pkg, label, ai.loadIcon(pm)));
            } catch (Exception ignored) {}
        }

        // Add any other installed launcher apps
        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN, null);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> lb = pm.queryIntentActivities(leanbackIntent, 0);
        if (lb != null) {
            for (ResolveInfo ri : lb) {
                String pkg = ri.activityInfo.packageName;
                if (!pkg.equals(myPkg) && !seen.contains(pkg)) {
                    seen.add(pkg);
                    appList.add(new AppModel(pkg, ri.loadLabel(pm).toString(), ri.loadIcon(pm)));
                }
            }
        }

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> std = pm.queryIntentActivities(mainIntent, 0);
        if (std != null) {
            for (ResolveInfo ri : std) {
                String pkg = ri.activityInfo.packageName;
                if (!pkg.equals(myPkg) && !seen.contains(pkg)) {
                    seen.add(pkg);
                    appList.add(new AppModel(pkg, ri.loadLabel(pm).toString(), ri.loadIcon(pm)));
                }
            }
        }

        rvApps.setAdapter(new AppsAdapter(this, appList));

        if (tvAppsCount != null) {
            tvAppsCount.setText("ALL INSTALLED APPS (" + appList.size() + " APPS)");
        }
    }

    private void setupFamilyWallpaperEngine() {
        try {
            String[] files = getAssets().list("wallpapers");
            if (files != null && files.length > 0) {
                for (String f : files) {
                    String lower = f.toLowerCase(Locale.ROOT);
                    if (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                        assetWallpapers.add(f);
                    }
                }
            }
        } catch (Exception ignored) {}

        File[] candidateDirs = new File[] {
                new File("/sdcard/wallpapers"),
                new File("/sdcard/Pictures"),
                new File("/sdcard/Android/data/com.gathin.tvhome/files/wallpapers")
        };
        for (File dir : candidateDirs) {
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName().toLowerCase(Locale.ROOT);
                        if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".webp")) {
                            sdcardWallpapers.add(f);
                        }
                    }
                }
            }
        }

        if (!assetWallpapers.isEmpty()) {
            Collections.shuffle(assetWallpapers);
            showNextAssetWallpaper();

            Runnable ticker = new Runnable() {
                @Override
                public void run() {
                    showNextAssetWallpaper();
                    mainHandler.postDelayed(this, 20000);
                }
            };
            mainHandler.postDelayed(ticker, 20000);
        } else if (!sdcardWallpapers.isEmpty()) {
            Collections.shuffle(sdcardWallpapers);
            showNextSdcardWallpaper();

            Runnable ticker = new Runnable() {
                @Override
                public void run() {
                    showNextSdcardWallpaper();
                    mainHandler.postDelayed(this, 20000);
                }
            };
            mainHandler.postDelayed(ticker, 20000);
        }
    }

    private void showNextAssetWallpaper() {
        if (assetWallpapers.isEmpty()) return;
        currentWallpaperIndex = (currentWallpaperIndex + 1) % assetWallpapers.size();
        String assetPath = "wallpapers/" + assetWallpapers.get(currentWallpaperIndex);

        try {
            InputStream is = getAssets().open(assetPath);
            final Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            if (bitmap != null) {
                ivWallpaper.animate().alpha(0.25f).setDuration(400).withEndAction(() -> {
                    ivWallpaper.setImageBitmap(bitmap);
                    ivWallpaper.animate().alpha(1.0f).setDuration(400).start();
                }).start();
            }
        } catch (Exception ignored) {}
    }

    private void showNextSdcardWallpaper() {
        if (sdcardWallpapers.isEmpty()) return;
        currentWallpaperIndex = (currentWallpaperIndex + 1) % sdcardWallpapers.size();
        File f = sdcardWallpapers.get(currentWallpaperIndex);

        try {
            final Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            if (bitmap != null) {
                ivWallpaper.animate().alpha(0.25f).setDuration(400).withEndAction(() -> {
                    ivWallpaper.setImageBitmap(bitmap);
                    ivWallpaper.animate().alpha(1.0f).setDuration(400).start();
                }).start();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            btnWatch.requestFocus();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
