package com.mashad.launcher;

import android.graphics.drawable.Drawable;

public class AppModel {
    public String packageName;
    public String label;
    public Drawable icon;

    public AppModel(String packageName, String label, Drawable icon) {
        this.packageName = packageName;
        this.label = label;
        this.icon = icon;
    }
}
