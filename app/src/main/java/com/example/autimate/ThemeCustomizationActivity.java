package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class ThemeCustomizationActivity extends AppCompatActivity {

    private CardView lightThemeCard, darkThemeCard;
    private Button btnApplyTheme;
    private String selectedTheme = "light";
    private SharedPreferences themePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_customization);

        // Initialize SharedPreferences
        themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        lightThemeCard = findViewById(R.id.lightThemeCard);
        darkThemeCard = findViewById(R.id.darkThemeCard);
        btnApplyTheme = findViewById(R.id.btnApplyTheme);

        // Load current theme preference
        loadCurrentTheme();

        // Light theme card click
        lightThemeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTheme = "light";
                highlightSelected(lightThemeCard, darkThemeCard);
            }
        });

        // Dark theme card click
        darkThemeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTheme = "dark";
                highlightSelected(darkThemeCard, lightThemeCard);
            }
        });

        // Apply theme button
        btnApplyTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyTheme();
            }
        });

        // Toolbar navigation
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadCurrentTheme() {
        String currentTheme = themePrefs.getString("theme", "light");
        selectedTheme = currentTheme;

        if (currentTheme.equals("dark")) {
            highlightSelected(darkThemeCard, lightThemeCard);
        } else {
            highlightSelected(lightThemeCard, darkThemeCard);
        }
    }

    private void highlightSelected(CardView selected, CardView unselected) {
        // Highlight selected card with border
        GradientDrawable selectedDrawable = new GradientDrawable();
        selectedDrawable.setShape(GradientDrawable.RECTANGLE);
        selectedDrawable.setCornerRadius(16);
        selectedDrawable.setStroke(4, ContextCompat.getColor(this, R.color.soft_blue));

        // Check if card has background color and set it
        Drawable currentBg = selected.getBackground();
        if (currentBg instanceof GradientDrawable) {
            int currentColor = ((GradientDrawable) currentBg).getColor().getDefaultColor();
            selectedDrawable.setColor(currentColor);
        } else {
            selectedDrawable.setColor(ContextCompat.getColor(this, R.color.white));
        }

        selected.setBackground(selectedDrawable);
        selected.setCardElevation(12f);

        // Reset unselected card
        GradientDrawable unselectedDrawable = new GradientDrawable();
        unselectedDrawable.setShape(GradientDrawable.RECTANGLE);
        unselectedDrawable.setCornerRadius(16);
        unselectedDrawable.setStroke(0, Color.TRANSPARENT);

        Drawable unselectedBg = unselected.getBackground();
        if (unselectedBg instanceof GradientDrawable) {
            int currentColor = ((GradientDrawable) unselectedBg).getColor().getDefaultColor();
            unselectedDrawable.setColor(currentColor);
        } else {
            unselectedDrawable.setColor(ContextCompat.getColor(this, R.color.white));
        }

        unselected.setBackground(unselectedDrawable);
        unselected.setCardElevation(4f);
    }

    private void applyTheme() {
        // Save theme preference
        themePrefs.edit().putString("theme", selectedTheme).apply();

        // Apply theme using AppCompatDelegate
        if (selectedTheme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toast.makeText(this, "Theme applied! Restarting app...", Toast.LENGTH_SHORT).show();

        // Restart the app to apply theme changes
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                restartApp();
            }
        }, 1000);
    }

    private void restartApp() {
        Intent intent = new Intent(this, ChildHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}