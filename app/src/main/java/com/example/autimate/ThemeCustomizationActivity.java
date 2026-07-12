package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class ThemeCustomizationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private CardView lightThemeCard, darkThemeCard;
    private ImageView lightCheckmark, darkCheckmark;
    private ImageView ivLightIcon, ivDarkIcon;
    private TextView tvLightTitle, tvLightDesc, tvDarkTitle, tvDarkDesc;
    private Button btnApplyTheme;
    private String selectedTheme = "light";
    private SharedPreferences themePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_customization);

        // Initialize SharedPreferences
        themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        // Setup drawer
        setupDrawer();

        lightThemeCard = findViewById(R.id.lightThemeCard);
        darkThemeCard = findViewById(R.id.darkThemeCard);
        lightCheckmark = findViewById(R.id.lightCheckmark);
        darkCheckmark = findViewById(R.id.darkCheckmark);
        ivLightIcon = findViewById(R.id.ivLightIcon);
        ivDarkIcon = findViewById(R.id.ivDarkIcon);
        tvLightTitle = findViewById(R.id.tvLightTitle);
        tvLightDesc = findViewById(R.id.tvLightDesc);
        tvDarkTitle = findViewById(R.id.tvDarkTitle);
        tvDarkDesc = findViewById(R.id.tvDarkDesc);
        btnApplyTheme = findViewById(R.id.btnApplyTheme);

        // Load current theme preference
        loadCurrentTheme();

        // Light theme card click
        lightThemeCard.setOnClickListener(v -> {
            selectedTheme = "light";
            highlightSelected(lightThemeCard, darkThemeCard);
        });

        // Dark theme card click
        darkThemeCard.setOnClickListener(v -> {
            selectedTheme = "dark";
            highlightSelected(darkThemeCard, lightThemeCard);
        });

        // Apply theme button
        btnApplyTheme.setOnClickListener(v -> applyTheme());

        // Force dark mode text colors
        forceTextColors();
    }

    private void setupDrawer() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        updateNavHeader();
    }

    private void updateNavHeader() {
        if (navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        TextView tvParentName = headerView.findViewById(R.id.tvParentName);
        TextView tvParentEmail = headerView.findViewById(R.id.tvParentEmail);

        SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
        String parentName = prefs.getString("parentName", "Parent");
        String parentEmail = prefs.getString("parentEmail", "");

        if (tvParentName != null) tvParentName.setText(parentName);
        if (tvParentEmail != null) tvParentEmail.setText(parentEmail);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            startActivity(new Intent(this, AccountActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ChildProfileActivity.class));
        } else if (id == R.id.nav_progress_tracker) {
            startActivity(new Intent(this, ProgressTrackerActivity.class));
        } else if (id == R.id.nav_add_activity) {
            startActivity(new Intent(this, AddNewActivityActivity.class));
        } else if (id == R.id.nav_view_rewards) {
            startActivity(new Intent(this, RewardActivity.class));
        } else if (id == R.id.nav_theme) {
            // Already here
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", (dialog, which) -> {
            Intent intent = new Intent(ThemeCustomizationActivity.this, GoodbyeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void forceTextColors() {
        // Always set dark mode text to white
        tvDarkTitle.setTextColor(Color.WHITE);
        tvDarkDesc.setTextColor(Color.parseColor("#D3D3D3"));

        // Always set light mode text to dark
        tvLightTitle.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        tvLightDesc.setTextColor(ContextCompat.getColor(this, R.color.light_brown));
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
        float density = getResources().getDisplayMetrics().density;
        int borderWidth = (int) (2.5f * density);
        float cornerRadius = 16f * density;

        // Selected card styling
        GradientDrawable selectedDrawable = new GradientDrawable();
        selectedDrawable.setShape(GradientDrawable.RECTANGLE);
        selectedDrawable.setCornerRadius(cornerRadius);
        selectedDrawable.setStroke(borderWidth, ContextCompat.getColor(this, R.color.soft_blue));

        if (selected == lightThemeCard) {
            selectedDrawable.setColor(ContextCompat.getColor(this, R.color.white));
        } else {
            selectedDrawable.setColor(Color.parseColor("#2C2C2C"));
        }

        selected.setBackground(selectedDrawable);
        selected.setCardElevation(8f);

        // Unselected card styling
        GradientDrawable unselectedDrawable = new GradientDrawable();
        unselectedDrawable.setShape(GradientDrawable.RECTANGLE);
        unselectedDrawable.setCornerRadius(cornerRadius);
        unselectedDrawable.setStroke(0, Color.TRANSPARENT);

        if (unselected == lightThemeCard) {
            unselectedDrawable.setColor(ContextCompat.getColor(this, R.color.white));
        } else {
            unselectedDrawable.setColor(Color.parseColor("#2C2C2C"));
        }

        unselected.setBackground(unselectedDrawable);
        unselected.setCardElevation(2f);

        // Show/hide checkmarks
        lightCheckmark.setVisibility(selected == lightThemeCard ? View.VISIBLE : View.GONE);
        darkCheckmark.setVisibility(selected == darkThemeCard ? View.VISIBLE : View.GONE);

        // Force text colors again after selection
        forceTextColors();
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

        Toast.makeText(this, "✅ Theme applied successfully!", Toast.LENGTH_SHORT).show();

        // Restart the app to apply theme changes
        new android.os.Handler().postDelayed(() -> {
            restartApp();
        }, 800);
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Force text colors when activity resumes
        forceTextColors();
    }
}