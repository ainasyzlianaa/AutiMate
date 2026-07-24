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

import com.bumptech.glide.Glide;
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
        TextView tvChildName = headerView.findViewById(R.id.tvChildName);
        TextView tvChildStatus = headerView.findViewById(R.id.tvChildStatus);
        ImageView headerProfileImage = headerView.findViewById(R.id.headerProfileImage);
        TextView headerProfileIcon = headerView.findViewById(R.id.headerProfileIcon);

        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String childName = prefs.getString("childName", "Child");
        String childAvatar = prefs.getString("childAvatar", "👧");

        if (tvChildName != null) {
            tvChildName.setText(childName);
        }

        if (tvChildStatus != null) {
            tvChildStatus.setText("● Active");
        }

        if (headerProfileImage != null && headerProfileIcon != null) {
            if (childAvatar != null && (childAvatar.startsWith("content://") || childAvatar.startsWith("file://") ||
                    childAvatar.startsWith("http://") || childAvatar.startsWith("https://"))) {
                try {
                    Glide.with(this)
                            .load(childAvatar)
                            .placeholder(R.drawable.circle_bg)
                            .into(headerProfileImage);
                    headerProfileImage.setVisibility(View.VISIBLE);
                    headerProfileIcon.setVisibility(View.GONE);
                } catch (Exception e) {
                    headerProfileImage.setVisibility(View.GONE);
                    headerProfileIcon.setVisibility(View.VISIBLE);
                    headerProfileIcon.setText("👧");
                }
            } else {
                headerProfileImage.setVisibility(View.GONE);
                headerProfileIcon.setVisibility(View.VISIBLE);
                headerProfileIcon.setText(childAvatar != null && !childAvatar.isEmpty() ? childAvatar : "👧");
            }
        }
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
        tvDarkTitle.setTextColor(Color.WHITE);
        tvDarkDesc.setTextColor(Color.parseColor("#D3D3D3"));

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

        lightCheckmark.setVisibility(selected == lightThemeCard ? View.VISIBLE : View.GONE);
        darkCheckmark.setVisibility(selected == darkThemeCard ? View.VISIBLE : View.GONE);

        forceTextColors();
    }

    private void applyTheme() {
        themePrefs.edit().putString("theme", selectedTheme).apply();

        if (selectedTheme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toast.makeText(this, "✅ Theme applied successfully!", Toast.LENGTH_SHORT).show();

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
        forceTextColors();
    }
}