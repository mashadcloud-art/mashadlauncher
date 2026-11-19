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
    private TextView tvLiveBadge, tvEventCounter, tvChannelBadge, tvRegionTag, tvLiveScore;
    private TextView tvEventTitle, tvEventSubtitle;
    private Button btnPrev, btnNext, btnWatch, btnXstream;
    private RecyclerView rvApps;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<LiveItem> liveItems = new ArrayList<>();
    private int currentEventIndex = 0;

    private final List<String> assetWallpapers = new ArrayList<>();
    private final List<File> sdcardWallpapers = new ArrayList<>();
    private int currentWallpaperIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        startClock();
        loadRealLiveGuide();
        setupLiveDockControls();
        loadInstalledApps();
        setupFamilyWallpaperEngine();
    }

    private void bindViews() {
        ivWallpaper = findViewById(R.id.ivWallpaper);
        tvClock = findViewById(R.id.tvClock);
        tvDate = findViewById(R.id.tvDate);
        tvLiveStatus = findViewById(R.id.tvLiveStatus);

        tvLiveBadge = findViewById(R.id.tvLiveBadge);
        tvEventCounter = findViewById(R.id.tvEventCounter);
        tvChannelBadge = findViewById(R.id.tvChannelBadge);
        tvRegionTag = findViewById(R.id.tvRegionTag);
        tvLiveScore = findViewById(R.id.tvLiveScore);

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

    private void loadRealLiveGuide() {
        liveItems.clear();
        try {
            InputStream is = getAssets().open("live_guide.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONArray array = new JSONArray(sb.toString());
            List<LiveItem> featuredAlerts = new ArrayList<>();
            List<LiveItem> regularShows = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.optString("id", "evt-" + i);
                String channel = obj.optString("channel", "Indian TV");
                String title = obj.optString("title", "Live Broadcast");
                String timeRange = obj.optString("time_range", "LIVE NOW");
                String region = obj.optString("region", "National");
                String category = obj.optString("category", "General");
                String ottName = obj.optString("ott_name", "JioHotstar");
                String pkgName = obj.optString("package_name", "in.startv.hotstar");
                String deepLink = obj.optString("deep_link", "https://www.hotstar.com");
                String xstreamUri = obj.optString("xstream_uri", "https://www.airtelxstream.in/livetv-channels");
                boolean isAlert = obj.optBoolean("is_featured_alert", false);

                // Check for live scores or sport matchups
                String score = "";
                String teamA = null;
                String teamB = null;
                if (category.equalsIgnoreCase("Cricket") || title.toLowerCase(Locale.ROOT).contains(" vs ") || title.toLowerCase(Locale.ROOT).contains(" v ")) {
                    if (title.contains(" vs ")) {
                        String[] parts = title.split(" vs ");
                        teamA = parts[0].trim();
                        teamB = parts[1].trim();
                    }
                    score = isAlert ? "MATCH LIVE" : "LIVE CRICKET";
                } else if (category.equalsIgnoreCase("Football")) {
                    score = "LIVE MATCH";
                }

                LiveItem item = new LiveItem(id, title, channel, ottName, category, region, timeRange, score, teamA, teamB, pkgName, deepLink, xstreamUri);

                if (isAlert) {
                    featuredAlerts.add(item);
                } else {
                    regularShows.add(item);
                }
            }

            // Featured alerts first (Matches & Big Shows), then the rest of the 529 channels
            liveItems.addAll(featuredAlerts);
            liveItems.addAll(regularShows);

            if (tvLiveStatus != null) {
                tvLiveStatus.setText("● " + liveItems.size() + " Live Regional & Sports Guides Active");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        renderEvent(0);
    }

    private void renderEvent(int index) {
        if (liveItems.isEmpty()) return;
        currentEventIndex = Math.max(0, Math.min(index, liveItems.size() - 1));
        LiveItem item = liveItems.get(currentEventIndex);

        tvEventCounter.setText((currentEventIndex + 1) + " / " + liveItems.size());
        tvChannelBadge.setText(item.channel.toUpperCase(Locale.ROOT));
        tvRegionTag.setText(item.region.toUpperCase(Locale.ROOT) + " • " + item.category.toUpperCase(Locale.ROOT));

        tvEventTitle.setText(item.title);
        tvEventSubtitle.setText(item.channel + " • " + item.platform + " • " + item.time);

        if (item.liveScore != null && !item.liveScore.trim().isEmpty()) {
            tvLiveScore.setVisibility(View.VISIBLE);
            tvLiveScore.setText("⚡ " + item.liveScore);
        } else {
            tvLiveScore.setVisibility(View.GONE);
        }

        btnWatch.setText("▶ " + item.platform.toUpperCase(Locale.ROOT));
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

        btnWatch.requestFocus();
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
            Toast.makeText(this, "Opening channel broadcast...", Toast.LENGTH_SHORT).show();
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

    private void setupFamilyWallpaperEngine() {
        // 1. Check embedded wallpapers from assets
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Also check external sdcard
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

            // Rotate every 20 seconds
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
