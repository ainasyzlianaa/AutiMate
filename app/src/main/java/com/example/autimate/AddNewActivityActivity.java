package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;

public class AddNewActivityActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private LinearLayout activityListContainer;
    private Button btnAddNewActivity;
    private TextView tvCurrentActivities;

    private List<ActivityItem> existingActivities;
    private List<ActivityItem> allAvailableActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_activity);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        activityListContainer = findViewById(R.id.activityListContainer);
        btnAddNewActivity = findViewById(R.id.btnAddNewActivity);
        tvCurrentActivities = findViewById(R.id.tvCurrentActivities);

        // Setup drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Update header
        updateNavHeader();

        // Initialize available activities
        initAvailableActivities();

        // Load existing activities
        loadExistingActivities();

        btnAddNewActivity.setOnClickListener(v -> showAddActivityDialog());
    }

    private void initAvailableActivities() {
        allAvailableActivities = new ArrayList<>();
        allAvailableActivities.add(new ActivityItem("Brush Teeth", getDrawableId("brush")));
        allAvailableActivities.add(new ActivityItem("Eat Foods", getDrawableId("foods")));
        allAvailableActivities.add(new ActivityItem("Wash Hands", getDrawableId("hands")));
        allAvailableActivities.add(new ActivityItem("Sleep", getDrawableId("sleep")));
        allAvailableActivities.add(new ActivityItem("Pack School Bag", getDrawableId("bag")));
        allAvailableActivities.add(new ActivityItem("Wear Clothes", getDrawableId("clothes")));
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private void loadExistingActivities() {
        existingActivities = new ArrayList<>();

        String savedActivities = getSharedPreferences("RoutinePrefs", MODE_PRIVATE)
                .getString("activities", "");

        if (!savedActivities.isEmpty()) {
            String[] activities = savedActivities.split(",");
            for (String act : activities) {
                for (ActivityItem item : allAvailableActivities) {
                    if (item.name.equals(act)) {
                        existingActivities.add(item);
                        break;
                    }
                }
            }
        }

        if (existingActivities.isEmpty()) {
            existingActivities.add(new ActivityItem("Brush Teeth", getDrawableId("brush")));
            existingActivities.add(new ActivityItem("Eat Foods", getDrawableId("foods")));
            existingActivities.add(new ActivityItem("Wash Hands", getDrawableId("hands")));
            existingActivities.add(new ActivityItem("Sleep", getDrawableId("sleep")));
            saveActivities();
        }

        displayCurrentActivities();
    }

    private void displayCurrentActivities() {
        activityListContainer.removeAllViews();

        if (existingActivities.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No activities added yet.\nTap + to add activities.");
            emptyView.setTextSize(14);
            emptyView.setTextColor(getColor(R.color.light_brown));
            emptyView.setPadding(16, 32, 16, 32);
            emptyView.setGravity(android.view.Gravity.CENTER);
            activityListContainer.addView(emptyView);
            return;
        }

        for (ActivityItem activity : existingActivities) {
            View activityView = getLayoutInflater().inflate(R.layout.item_selected_activity, activityListContainer, false);

            ImageView ivIcon = activityView.findViewById(R.id.ivIcon);
            TextView tvName = activityView.findViewById(R.id.tvName);
            Button btnRemove = activityView.findViewById(R.id.btnRemove);

            if (activity.iconRes != 0) {
                ivIcon.setImageResource(activity.iconRes);
            }
            tvName.setText(activity.name);

            // UPDATED: Confirmation dialog for removing activity
            btnRemove.setOnClickListener(v -> {
                showRemoveConfirmationDialog(activity);
            });

            activityListContainer.addView(activityView);
        }

        if (tvCurrentActivities != null) {
            tvCurrentActivities.setText("Current Activities (" + existingActivities.size() + ")");
        }
    }

    // NEW: Remove confirmation dialog
    private void showRemoveConfirmationDialog(ActivityItem activity) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Activity")
                .setMessage("Are you sure you want to remove \"" + activity.name + "\"?\n\nThis will remove it from the daily routine list.")
                .setPositiveButton("YES, REMOVE", (dialog, which) -> {
                    existingActivities.remove(activity);
                    displayCurrentActivities();
                    saveActivities();
                    Toast.makeText(this, "Removed: " + activity.name, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("CANCEL", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showAddActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_activity, null);

        LinearLayout activitiesGrid = dialogView.findViewById(R.id.activitiesGrid);

        if (activitiesGrid == null) {
            Toast.makeText(this, "Error loading dialog", Toast.LENGTH_SHORT).show();
            return;
        }

        activitiesGrid.removeAllViews();

        List<ActivityItem> availableToAdd = new ArrayList<>();
        for (ActivityItem item : allAvailableActivities) {
            boolean alreadyExists = false;
            for (ActivityItem existing : existingActivities) {
                if (existing.name.equals(item.name)) {
                    alreadyExists = true;
                    break;
                }
            }
            if (!alreadyExists) {
                availableToAdd.add(item);
            }
        }

        if (availableToAdd.isEmpty()) {
            Toast.makeText(this, "All activities are already added!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (ActivityItem activity : availableToAdd) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);
            card.setRadius(20);
            card.setCardBackgroundColor(getColor(R.color.white));
            card.setElevation(4);
            card.setClickable(true);

            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
            innerLayout.setPadding(24, 24, 24, 24);
            innerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // BIGGER ICON
            ImageView iconImage = new ImageView(this);
            if (activity.iconRes != 0) {
                iconImage.setImageResource(activity.iconRes);
            }
            iconImage.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
            iconImage.setPadding(0, 0, 20, 0);
            iconImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // BIGGER TEXT
            TextView nameText = new TextView(this);
            nameText.setText(activity.name);
            nameText.setTextSize(18);
            nameText.setTextColor(getColor(R.color.text_dark));
            nameText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView arrowText = new TextView(this);
            arrowText.setText("+");
            arrowText.setTextSize(32);
            arrowText.setTextColor(getColor(R.color.soft_blue));

            innerLayout.addView(iconImage);
            innerLayout.addView(nameText);
            innerLayout.addView(arrowText);
            card.addView(innerLayout);

            // UPDATED: Confirmation dialog for adding activity
            card.setOnClickListener(v -> {
                showAddConfirmationDialog(activity);
            });

            activitiesGrid.addView(card);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    // NEW: Add confirmation dialog
    private void showAddConfirmationDialog(ActivityItem activity) {
        new AlertDialog.Builder(this)
                .setTitle("Add Activity")
                .setMessage("Are you sure you want to add \"" + activity.name + "\" to the routine list?")
                .setPositiveButton("YES, ADD", (dialog, which) -> {
                    existingActivities.add(activity);
                    displayCurrentActivities();
                    saveActivities();
                    Toast.makeText(this, "Added: " + activity.name, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("CANCEL", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void saveActivities() {
        StringBuilder sb = new StringBuilder();
        for (ActivityItem activity : existingActivities) {
            if (sb.length() > 0) sb.append(",");
            sb.append(activity.name);
        }
        getSharedPreferences("RoutinePrefs", MODE_PRIVATE)
                .edit()
                .putString("activities", sb.toString())
                .apply();

        sendBroadcast(new Intent("ACTIVITIES_UPDATED"));
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvParentName = headerView.findViewById(R.id.tvParentName);
        TextView tvParentEmail = headerView.findViewById(R.id.tvParentEmail);

        SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
        String parentName = prefs.getString("parentName", "Parent");

        if (tvParentName != null) tvParentName.setText(parentName);
        if (tvParentEmail != null) tvParentEmail.setText("Parent Account");
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
            // Already here
        } else if (id == R.id.nav_view_rewards) {
            startActivity(new Intent(this, RewardActivity.class));
        } else if (id == R.id.nav_theme) {
            startActivity(new Intent(this, ThemeCustomizationActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("YES", (dialog, which) -> {
                    // Navigate to GoodbyeActivity
                    Intent intent = new Intent(AddNewActivityActivity.this, GoodbyeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    static class ActivityItem {
        String name;
        int iconRes;

        ActivityItem(String name, int iconRes) {
            this.name = name;
            this.iconRes = iconRes;
        }
    }
}