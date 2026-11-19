package com.mashad.launcher;

import android.app.Activity;
import android.content.Intent;
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
    private TextView tvClock, tvDate, tvLiveStatus;
    private TextView tvLiveBadge, tvEventCounter, tvLiveScore, tvCategoryTag;
    private TextView tvEventTitle, tvEventSubtitle;
    private Button btnPrev, btnNext, btnWatch, btnXstream;
    private RecyclerView rvApps;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<LiveItem> liveItems = new ArrayList<>();
    private int currentEventIndex = 0;

    private final List<File> wallpaperFiles = new ArrayList<>();
    private int currentWallpaperIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        startClock();
        loadLiveGuide();
        setupLiveDockControls();
        loadInstalledApps();
        setupWallpaperRotation();
    }

    private void bindViews() {
        ivWallpaper = findViewById(R.id.ivWallpaper);
        tvClock = findViewById(R.id.tvClock);
        tvDate = findViewById(R.id.tvDate);
        tvLiveStatus = findViewById(R.id.tvLiveStatus);

        tvLiveBadge = findViewById(R.id.tvLiveBadge);
        tvEventCounter = findViewById(R.id.tvEventCounter);
        tvLiveScore = findViewById(R.id.tvLiveScore);
        tvCategoryTag = findViewById(R.id.tvCategoryTag);

        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventSubtitle = findViewById(R.id.tvEventSubtitle);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnWatch = findViewById(R.id.btnWatch);
        btnXstream = findViewById(R.id.btnXstream);

        rvApps = findViewById(R.id.rvApps);
        rvApps.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void startClock() {
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
    }

    private void loadLiveGuide() {
        try {
            InputStream is = getAssets().open("live_guide.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONObject root = new JSONObject(sb.toString());
            JSONArray array = root.optJSONArray("programs");
            if (array == null) array = root.optJSONArray("events");
            if (array == null && root.length() > 0) {
                // Try top-level array or first array key
                String firstKey = root.keys().next();
                array = root.optJSONArray(firstKey);
            }

            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String id = obj.optString("id", "id-" + i);
                    String title = obj.optString("title", "Live Event");
                    String channel = obj.optString("channel", "Indian TV");
                    String platform = obj.optString("platform", "JioHotstar");
                    String category = obj.optString("category", "Live");
                    String region = obj.optString("region", "National");
                    String time = obj.optString("time", "LIVE NOW");
                    String score = obj.optString("liveScore", obj.optString("score", ""));
                    String teamA = obj.optString("teamA", null);
                    String teamB = obj.optString("teamB", null);
                    String pkg = obj.optString("pkg", "in.startv.hotstar");
                    String deepLink = obj.optString("deepLink", "https://www.hotstar.com/in/sports");
                    String xstream = obj.optString("xstreamUri", "https://www.airtelxstream.in/livetv-channels");

                    liveItems.add(new LiveItem(id, title, channel, platform, category, region, time, score, teamA, teamB, pkg, deepLink, xstream));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (liveItems.isEmpty()) {
            // High quality fallback presets
            liveItems.add(new LiveItem("1", "India vs Australia — 2nd Test", "Star Sports 1 HD", "JioHotstar", "Cricket", "National", "LIVE NOW", "IND 248/4 (62.3 ov)", "India", "Australia", "in.startv.hotstar", "https://www.hotstar.com/in/sports", "https://www.airtelxstream.in/livetv-channels"));
            liveItems.add(new LiveItem("2", "Kerala Blasters vs Mohun Bagan", "Star Sports 3", "JioHotstar", "Football", "ISL", "LIVE NOW", "KBFC 2 - 1 MB", "Kerala Blasters", "Mohun Bagan", "in.startv.hotstar", "https://www.hotstar.com/in/sports", "https://www.airtelxstream.in/livetv-channels"));
            liveItems.add(new LiveItem("3", "Bumper Chiri Aaghosham", "Mazhavil Manorama", "ManoramaMAX", "Entertainment", "Malayalam", "LIVE NOW", "Prime Time Show", null, null, "com.quickplay.android.multiscreen.manorama", "https://www.manoramamax.com", "https://www.airtelxstream.in/livetv-channels/mazhavil-manorama-hd/449"));
            liveItems.add(new LiveItem("4", "Bigg Boss Malayalam", "Asianet HD", "JioHotstar", "Reality", "Malayalam", "LIVE NOW", "Daily Episode", null, null, "in.startv.hotstar", "https://www.hotstar.com", "https://www.airtelxstream.in/livetv-channels/asianet-hd/384"));
            liveItems.add(new LiveItem("5", "Super Singer Season 10", "Star Vijay HD", "JioHotstar", "Music", "Tamil", "LIVE NOW", "Grand Finale", null, null, "in.startv.hotstar", "https://www.hotstar.com", "https://www.airtelxstream.in/livetv-channels/star-vijay-hd/386"));
        }

        renderEvent(0);
    }

    private void renderEvent(int index) {
        if (liveItems.isEmpty()) return;
        currentEventIndex = Math.max(0, Math.min(index, liveItems.size() - 1));
        LiveItem item = liveItems.get(currentEventIndex);

        tvEventCounter.setText((currentEventIndex + 1) + " / " + liveItems.size());
        tvEventTitle.setText(item.title);
        tvEventSubtitle.setText(item.channel + " • " + item.platform + " • " + item.category + " (" + item.region + ")");

        if (item.liveScore != null && !item.liveScore.trim().isEmpty()) {
            tvLiveScore.setVisibility(View.VISIBLE);
            tvLiveScore.setText("⚡ " + item.liveScore);
        } else {
            tvLiveScore.setVisibility(View.GONE);
        }

        tvCategoryTag.setText(item.category.toUpperCase(Locale.ROOT));
    }

    private void setupLiveDockControls() {
        btnPrev.setOnClickListener(v -> {
            int newIdx = currentEventIndex - 1;
            if (newIdx < 0) newIdx = liveItems.size() - 1;
            renderEvent(newIdx);
        });

        btnNext.setOnClickListener(v -> {
            int newIdx = currentEventIndex + 1;
            if (newIdx >= liveItems.size()) newIdx = 0;
            renderEvent(newIdx);
        });

        btnWatch.setOnClickListener(v -> {
            if (liveItems.isEmpty()) return;
            LiveItem item = liveItems.get(currentEventIndex);
            launchAppOrUrl(item.pkg, item.deepLink);
        });

        btnXstream.setOnClickListener(v -> {
            if (liveItems.isEmpty()) return;
            LiveItem item = liveItems.get(currentEventIndex);
            launchAppOrUrl("tv.airtel.xstream.tvapp", item.xstreamUri);
        });

        // Focus watch button by default on load
        btnWatch.requestFocus();
    }

    private void launchAppOrUrl(String packageName, String fallbackUrl) {
        try {
            Intent launchIntent = null;
            if (packageName != null && !packageName.isEmpty()) {
                PackageManager pm = getPackageManager();
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
            Toast.makeText(this, "Could not open channel", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN, null);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

        List<ResolveInfo> activities = pm.queryIntentActivities(leanbackIntent, 0);
        if (activities == null || activities.isEmpty()) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            activities = pm.queryIntentActivities(mainIntent, 0);
        }

        List<AppModel> appList = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        String myPkg = getPackageName();

        if (activities != null) {
            for (ResolveInfo ri : activities) {
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(myPkg) || seen.contains(pkg)) continue;
                seen.add(pkg);

                String label = ri.loadLabel(pm).toString();
                appList.add(new AppModel(pkg, label, ri.loadIcon(pm)));
            }
        }

        Collections.sort(appList, (a, b) -> a.label.compareToIgnoreCase(b.label));
        rvApps.setAdapter(new AppsAdapter(this, appList));
    }

    private void setupWallpaperRotation() {
        File[] candidateDirs = new File[] {
                new File("/sdcard/Android/data/com.gathin.tvhome/files/wallpapers"),
                new File("/sdcard/wallpapers"),
                new File("/sdcard/Pictures")
        };

        for (File dir : candidateDirs) {
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName().toLowerCase(Locale.ROOT);
                        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) {
                            wallpaperFiles.add(f);
                        }
                    }
                }
            }
        }

        if (!wallpaperFiles.isEmpty()) {
            Collections.shuffle(wallpaperFiles);
            showNextWallpaper();

            // Rotate every 30 seconds
            Runnable wallpaperTicker = new Runnable() {
                @Override
                public void run() {
                    showNextWallpaper();
                    mainHandler.postDelayed(this, 30000);
                }
            };
            mainHandler.postDelayed(wallpaperTicker, 30000);
        }
    }

    private void showNextWallpaper() {
        if (wallpaperFiles.isEmpty()) return;
        currentWallpaperIndex = (currentWallpaperIndex + 1) % wallpaperFiles.size();
        File nextFile = wallpaperFiles.get(currentWallpaperIndex);

        try {
            final Bitmap bitmap = BitmapFactory.decodeFile(nextFile.getAbsolutePath());
            if (bitmap != null) {
                ivWallpaper.animate().alpha(0.2f).setDuration(400).withEndAction(() -> {
                    ivWallpaper.setImageBitmap(bitmap);
                    ivWallpaper.animate().alpha(1.0f).setDuration(400).start();
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Prevent accidental back exit from home launcher
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            btnWatch.requestFocus();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
