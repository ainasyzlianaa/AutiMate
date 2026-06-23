package com.example.autimate;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    public static final String LIGHT_MODE = "light";
    public static final String DARK_MODE = "dark";

    public static void applyTheme(String themePref) {
        if (themePref.equals(DARK_MODE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void saveThemePreference(SharedPreferences prefs, String theme) {
        prefs.edit().putString("theme", theme).apply();
    }

    public static String getSavedTheme(SharedPreferences prefs) {
        return prefs.getString("theme", LIGHT_MODE);
    }
}