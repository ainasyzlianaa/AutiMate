package com.example.autimate;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String theme = prefs.getString("theme", "light");

        if (theme.equals("dark")) {
            setTheme(R.style.Theme_AutiMate);
        } else {
            setTheme(R.style.Theme_AutiMate);
        }

        super.onCreate(savedInstanceState);
    }
}