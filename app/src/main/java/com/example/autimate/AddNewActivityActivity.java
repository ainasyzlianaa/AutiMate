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
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewActivityActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private LinearLayout activityListContainer;
    private Button btnAddNewActivity;
    private TextView tvCurrentActivities;

    private FirebaseFirestore db;
    private String parentId;
    private String childId;
    private String childName;
    private List<ActivityItem> existingActivities;
    private List<ActivityItem> allAvailableActivities;
    private boolean isLoading = false;

    // Store reference to the add dialog
    private AlertDialog addDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_activity);

        // Setup drawer
        setupDrawer();

        db = FirebaseFirestore.getInstance();
        SharedPreferences childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        childId = childPrefs.getString("childId", "");
        childName = childPrefs.getString("childName", "");
        parentId = getSharedPreferences("ParentPrefs", MODE_PRIVATE).getString("parentId", "");

        activityListContainer = findViewById(R.id.activityListContainer);
        btnAddNewActivity = findViewById(R.id.btnAddNewActivity);
        tvCurrentActivities = findViewById(R.id.tvCurrentActivities);

        initAvailableActivities();
        existingActivities = new ArrayList<>();

        btnAddNewActivity.setOnClickListener(v -> {
            try {
                showAddActivityDialog();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error opening dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        loadExistingActivities();
    }

    private void initAvailableActivities() {
        allAvailableActivities = new ArrayList<>();
        allAvailableActivities.add(new ActivityItem("Brush Teeth", getDrawableId("brush")));
        allAvailableActivities.add(new ActivityItem("Eat Foods", getDrawableId("foods")));
        allAvailableActivities.add(new ActivityItem("Wash Hands", getDrawableId("hands")));
        allAvailableActivities.add(new ActivityItem("Sleep", getDrawableId("sleep")));
        allAvailableActivities.add(new ActivityItem("Pack School Bag", getDrawableId("bag")));
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private void loadExistingActivities() {
        if (isLoading) return;
        isLoading = true;

        if (childId == null || childId.isEmpty() || parentId == null || parentId.isEmpty()) {
            existingActivities = new ArrayList<>();
            displayCurrentActivities();
            isLoading = false;
            return;
        }

        loadFromSharedPreferences();

        db.collection("activities")
                .whereEqualTo("childId", childId)
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ActivityItem> loadedActivities = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String activityName = doc.getString("activityName");
                        if (activityName == null || activityName.isEmpty()) continue;
                        ActivityItem item = findActivityItemByName(activityName);
                        item.docId = doc.getId();
                        Long order = doc.getLong("order");
                        if (order != null) {
                            item.order = order.intValue();
                        }
                        loadedActivities.add(item);
                    }

                    loadedActivities.sort((a, b) -> {
                        if (a.order != 0 || b.order != 0) {
                            return Integer.compare(a.order, b.order);
                        }
                        return a.name.compareTo(b.name);
                    });

                    existingActivities = loadedActivities;
                    saveActivitiesLocally();
                    displayCurrentActivities();
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                    displayCurrentActivities();
                });
    }

    private void loadFromSharedPreferences() {
        String savedActivities = getSharedPreferences("RoutinePrefs", MODE_PRIVATE)
                .getString(getActivityPrefKey(), "");

        if (!savedActivities.isEmpty()) {
            existingActivities = new ArrayList<>();
            String[] activities = savedActivities.split(",");
            for (String act : activities) {
                if (act.trim().isEmpty()) continue;
                ActivityItem item = findActivityItemByName(act.trim());
                if (item != null) {
                    existingActivities.add(item);
                }
            }
            displayCurrentActivities();
        } else {
            existingActivities = new ArrayList<>();
            displayCurrentActivities();
        }
    }

    private ActivityItem findActivityItemByName(String name) {
        for (ActivityItem item : allAvailableActivities) {
            if (item.name.equals(name)) {
                return new ActivityItem(item.name, item.iconRes);
            }
        }
        return new ActivityItem(name, 0);
    }

    private void saveActivitiesLocally() {
        StringBuilder sb = new StringBuilder();
        for (ActivityItem activity : existingActivities) {
            if (sb.length() > 0) sb.append(",");
            sb.append(activity.name);
        }

        String key = (childId != null && !childId.isEmpty()) ? "activities_" + childId : "activities";
        getSharedPreferences("RoutinePrefs", MODE_PRIVATE)
                .edit()
                .putString(key, sb.toString())
                .apply();

        sendBroadcast(new Intent("ACTIVITIES_UPDATED"));
    }

    private void displayCurrentActivities() {
        activityListContainer.removeAllViews();

        if (existingActivities == null || existingActivities.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No activities added yet.\nTap + to add activities.");
            emptyView.setTextSize(14);
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.light_brown));
            emptyView.setPadding(16, 32, 16, 32);
            emptyView.setGravity(android.view.Gravity.CENTER);
            activityListContainer.addView(emptyView);

            if (tvCurrentActivities != null) {
                tvCurrentActivities.setText("Current Activities (0)");
            }
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

            btnRemove.setOnClickListener(v -> {
                showRemoveConfirmationDialog(activity);
            });

            activityListContainer.addView(activityView);
        }

        if (tvCurrentActivities != null) {
            tvCurrentActivities.setText("Current Activities (" + existingActivities.size() + ")");
        }
    }

    private void showAddActivityDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_activity, null);

            LinearLayout activitiesGrid = dialogView.findViewById(R.id.activitiesGrid);

            if (activitiesGrid == null) {
                Toast.makeText(this, "Error: activitiesGrid not found", Toast.LENGTH_SHORT).show();
                return;
            }

            refreshAvailableActivities(activitiesGrid);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            builder.setPositiveButton("Close", (dialog, which) -> {
                dialog.dismiss();
                addDialog = null;
            });

            addDialog = builder.create();
            addDialog.show();

            Button closeButton = addDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (closeButton != null) {
                closeButton.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
                closeButton.setTypeface(null, android.graphics.Typeface.BOLD);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshAvailableActivities(LinearLayout activitiesGrid) {
        activitiesGrid.removeAllViews();

        loadExistingActivities();

        List<ActivityItem> availableToAdd = new ArrayList<>();
        for (ActivityItem item : allAvailableActivities) {
            boolean alreadyExists = false;
            if (existingActivities != null) {
                for (ActivityItem existing : existingActivities) {
                    if (existing.name.equals(item.name)) {
                        alreadyExists = true;
                        break;
                    }
                }
            }
            if (!alreadyExists) {
                availableToAdd.add(item);
            }
        }

        if (availableToAdd.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("All activities are already added! ✅");
            emptyView.setTextSize(16);
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.soft_blue));
            emptyView.setPadding(16, 32, 16, 32);
            emptyView.setGravity(android.view.Gravity.CENTER);
            activitiesGrid.addView(emptyView);
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
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            card.setElevation(4);
            card.setClickable(true);

            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
            innerLayout.setPadding(24, 24, 24, 24);
            innerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

            ImageView iconImage = new ImageView(this);
            if (activity.iconRes != 0) {
                iconImage.setImageResource(activity.iconRes);
            }
            iconImage.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
            iconImage.setPadding(0, 0, 20, 0);
            iconImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

            TextView nameText = new TextView(this);
            nameText.setText(activity.name);
            nameText.setTextSize(18);
            nameText.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            nameText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView arrowText = new TextView(this);
            arrowText.setText("+");
            arrowText.setTextSize(32);
            arrowText.setTextColor(ContextCompat.getColor(this, R.color.soft_blue));

            innerLayout.addView(iconImage);
            innerLayout.addView(nameText);
            innerLayout.addView(arrowText);
            card.addView(innerLayout);

            card.setOnClickListener(v -> {
                showAddConfirmationDialog(activity);
            });

            activitiesGrid.addView(card);
        }
    }

    private void showAddConfirmationDialog(ActivityItem activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Activity");
        builder.setMessage("Are you sure you want to add \"" + activity.name + "\" to the routine list?");
        builder.setPositiveButton("YES, ADD", (dialog, which) -> {
            if (childId != null && !childId.isEmpty() && parentId != null && !parentId.isEmpty()) {
                addActivityToFirestore(activity);
            } else {
                existingActivities.add(activity);
                displayCurrentActivities();
                saveActivitiesLocally();
                Toast.makeText(this, "Added: " + activity.name, Toast.LENGTH_SHORT).show();
            }

            refreshAddDialog();
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {});
        builder.setIcon(android.R.drawable.ic_dialog_info);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            negativeButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void refreshAddDialog() {
        if (addDialog != null && addDialog.isShowing()) {
            View dialogView = addDialog.getWindow().getDecorView().findViewById(android.R.id.content);
            if (dialogView != null) {
                LinearLayout activitiesGrid = dialogView.findViewById(R.id.activitiesGrid);
                if (activitiesGrid != null) {
                    refreshAvailableActivities(activitiesGrid);
                }
            }
        }
    }

    private void addActivityToFirestore(ActivityItem activity) {
        Map<String, Object> data = new HashMap<>();
        data.put("activityName", activity.name);
        data.put("childId", childId);
        data.put("parentId", parentId);
        data.put("order", existingActivities != null ? existingActivities.size() + 1 : 1);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("activities")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    activity.docId = documentReference.getId();
                    if (existingActivities == null) {
                        existingActivities = new ArrayList<>();
                    }
                    existingActivities.add(activity);

                    updateChildActivitiesArray(activity.name);

                    saveActivitiesLocally();
                    displayCurrentActivities();
                    Toast.makeText(this, "✅ Added: " + activity.name, Toast.LENGTH_SHORT).show();

                    refreshAddDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to add activity. Try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void updateChildActivitiesArray(String activityName) {
        db.collection("children").document(childId)
                .update("activities", FieldValue.arrayUnion(activityName))
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {
                    db.collection("children").document(childId)
                            .update("activities", FieldValue.arrayUnion(activityName));
                });
    }

    private void removeActivityFromChildArray(String activityName) {
        db.collection("children").document(childId)
                .update("activities", FieldValue.arrayRemove(activityName))
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});
    }

    private void showRemoveConfirmationDialog(ActivityItem activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Activity");
        builder.setMessage("Are you sure you want to remove \"" + activity.name + "\"?\n\nThis will remove it from the daily routine list.");
        builder.setPositiveButton("YES, REMOVE", (dialog, which) -> {
            if (activity.docId != null && !activity.docId.isEmpty()) {
                removeActivityFromFirestore(activity);
            } else {
                existingActivities.remove(activity);
                removeActivityFromChildArray(activity.name);
                displayCurrentActivities();
                saveActivitiesLocally();
                Toast.makeText(this, "Removed: " + activity.name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {});
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            negativeButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void removeActivityFromFirestore(ActivityItem activity) {
        db.collection("activities")
                .document(activity.docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    existingActivities.remove(activity);
                    removeActivityFromChildArray(activity.name);
                    displayCurrentActivities();
                    saveActivitiesLocally();
                    Toast.makeText(this, "Removed: " + activity.name, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to remove activity. Try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private String getActivityPrefKey() {
        if (childId != null && !childId.isEmpty()) {
            return "activities_" + childId;
        }
        return "activities";
    }

    static class ActivityItem {
        String name;
        int iconRes;
        String docId;
        int order = 0;

        ActivityItem(String name, int iconRes) {
            this.name = name;
            this.iconRes = iconRes;
        }
    }
}