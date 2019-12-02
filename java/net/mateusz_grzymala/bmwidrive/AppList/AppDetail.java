package net.mateusz_grzymala.bmwidrive.AppList;

import android.graphics.drawable.Drawable;

public class AppDetail implements Comparable<AppDetail> {
    CharSequence label;
    CharSequence name;
    Drawable icon;

    @Override
    public int compareTo(AppDetail another) {
        return this.label.toString().compareTo(another.label.toString());
    }
}