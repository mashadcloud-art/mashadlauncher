package com.mashad.launcher;

public class LiveItem {
    public String id;
    public String title;
    public String channel;
    public String platform;
    public String category;
    public String region;
    public String time;
    public String liveScore;
    public String venue;
    public String details;
    public String sportType;
    public String pkg;
    public String deepLink;
    public String xstreamUri;

    public LiveItem(String id, String title, String channel, String platform, String category,
                    String region, String time, String liveScore, String venue, String details,
                    String sportType, String pkg, String deepLink, String xstreamUri) {
        this.id = id;
        this.title = title;
        this.channel = channel;
        this.platform = platform;
        this.category = category;
        this.region = region;
        this.time = time != null && !time.isEmpty() ? time : "LIVE NOW";
        this.liveScore = liveScore != null ? liveScore : "";
        this.venue = venue != null ? venue : "";
        this.details = details != null ? details : "";
        this.sportType = sportType != null ? sportType : category;
        this.pkg = pkg != null && !pkg.isEmpty() ? pkg : "in.startv.hotstar";
        this.deepLink = deepLink != null && !deepLink.isEmpty() ? deepLink : "https://www.hotstar.com";
        this.xstreamUri = xstreamUri != null && !xstreamUri.isEmpty() ? xstreamUri : "https://www.airtelxstream.in";
    }
}
