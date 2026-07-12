package com.example.autimate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView tvTitle;
    private RecyclerView recyclerViewChildren;
    private Button btnConfirm, btnDeleteAccount;
    private View headerView;

    private FirebaseFirestore db;
    private final List<ChildProfile> childList = new ArrayList<>();
    private ChildSelectionAdapter adapter;
    private int selectedPosition = -1;

    private BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("PROFILE_UPDATED".equals(intent.getAction())) {
                String childName = intent.getStringExtra("childName");
                String childAvatar = intent.getStringExtra("childAvatar");
                if (childName != null) {
                    updateNavHeader(childName, childAvatar);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

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

        headerView = navigationView.getHeaderView(0);

        tvTitle = findViewById(R.id.tvTitle);
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        tvTitle.setText("Choose account:");

        db = FirebaseFirestore.getInstance();
        adapter = new ChildSelectionAdapter(childList);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChildren.setAdapter(adapter);

        loadChildren();

        btnConfirm.setOnClickListener(v -> {
            if (selectedPosition != -1 && selectedPosition < childList.size()) {
                ChildProfile selectedChild = childList.get(selectedPosition);
                SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("childId", selectedChild.id)
                        .putString("childName", selectedChild.name)
                        .putString("childAvatar", selectedChild.avatar != null ? selectedChild.avatar : "👧")
                        .apply();

                Toast.makeText(this, "Selected: " + selectedChild.name, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteAccount.setOnClickListener(v -> {
            if (selectedPosition != -1 && selectedPosition < childList.size()) {
                ChildProfile selectedChild = childList.get(selectedPosition);
                showDeleteConfirmationDialog(selectedChild);
            } else {
                Toast.makeText(this, "Please select an account to delete", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String savedChildName = prefs.getString("childName", "");
        String savedChildAvatar = prefs.getString("childAvatar", "👧");
        updateNavHeader(savedChildName, savedChildAvatar);
    }

    private void showDeleteConfirmationDialog(ChildProfile child) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete \"" + child.name + "\"?\n\nThis action cannot be undone. All data for this child will be permanently removed.");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("DELETE", (dialog, which) -> {
            deleteChildAccount(child);
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set button colors based on theme
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        int nightMode = AppCompatDelegate.getDefaultNightMode();
        boolean isDarkMode = (nightMode == AppCompatDelegate.MODE_NIGHT_YES) ||
                (nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM &&
                        (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        if (positiveButton != null) {
            if (isDarkMode) {
                positiveButton.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                positiveButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        if (negativeButton != null) {
            if (isDarkMode) {
                negativeButton.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                negativeButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
            negativeButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void deleteChildAccount(ChildProfile child) {
        if (child == null || child.id == null || child.id.isEmpty()) {
            Toast.makeText(this, "Error: Invalid child account", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get parent ID
        SharedPreferences parentPrefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
        String parentId = parentPrefs.getString("parentId", "");

        if (parentId.isEmpty()) {
            Toast.makeText(this, "Error: Parent not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress indicator (disable buttons)
        btnDeleteAccount.setEnabled(false);
        btnDeleteAccount.setText("DELETING...");

        // Step 1: Delete child document from "children" collection
        db.collection("children").document(child.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Step 2: Remove child ID from parent's children array
                    db.collection("parents").document(parentId)
                            .update("children", FieldValue.arrayRemove(child.id))
                            .addOnSuccessListener(aVoid2 -> {
                                // Step 3: Delete all activities for this child
                                deleteChildActivities(child.id, parentId);
                            })
                            .addOnFailureListener(e -> {
                                btnDeleteAccount.setEnabled(true);
                                btnDeleteAccount.setText("DELETE ACCOUNT");
                                Toast.makeText(this, "Failed to update parent: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnDeleteAccount.setEnabled(true);
                    btnDeleteAccount.setText("DELETE ACCOUNT");
                    Toast.makeText(this, "Failed to delete child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteChildActivities(String childId, String parentId) {
        // Delete all activities for this child
        db.collection("activities")
                .whereEqualTo("childId", childId)
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    // After deleting activities, clear SharedPreferences and navigate
                    onChildDeleted();
                })
                .addOnFailureListener(e -> {
                    // Even if activity deletion fails, continue
                    onChildDeleted();
                });
    }

    private void onChildDeleted() {
        // Clear child preferences if the deleted child was the current one
        SharedPreferences childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String currentChildId = childPrefs.getString("childId", "");

        // Check if the deleted child was the currently selected one
        // We need to check if the child was deleted from the list
        boolean wasCurrentChild = false;
        for (ChildProfile child : childList) {
            if (child.id.equals(currentChildId)) {
                wasCurrentChild = true;
                break;
            }
        }

        // If the deleted child was the current one, clear ChildPrefs
        if (wasCurrentChild) {
            childPrefs.edit().clear().apply();
        }

        // Reset UI
        btnDeleteAccount.setEnabled(true);
        btnDeleteAccount.setText("DELETE ACCOUNT");
        selectedPosition = -1;

        Toast.makeText(this, "✅ Child account deleted successfully!", Toast.LENGTH_SHORT).show();

        // Reload the list
        loadChildren();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(profileUpdateReceiver, new IntentFilter("PROFILE_UPDATED"));
        loadChildren();

        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String childName = prefs.getString("childName", "");
        String childAvatar = prefs.getString("childAvatar", "👧");
        updateNavHeader(childName, childAvatar);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(profileUpdateReceiver);
        } catch (Exception ignored) {
        }
    }

    private void loadChildren() {
        SharedPreferences parentPrefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
        String parentId = parentPrefs.getString("parentId", "");

        if (parentId.isEmpty()) {
            Toast.makeText(this, "Unable to load accounts. Parent not signed in.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childList.clear();

                    // Use a Set to track unique child names
                    Set<String> childNames = new HashSet<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String childName = doc.getString("childName");

                        // Skip if child name is null or empty
                        if (childName == null || childName.isEmpty()) continue;

                        // Skip duplicates
                        if (childNames.contains(childName)) continue;
                        childNames.add(childName);

                        ChildProfile child = new ChildProfile();
                        child.id = doc.getId();
                        child.name = childName;
                        child.age = doc.getString("age");
                        child.gender = doc.getString("gender");
                        child.avatar = doc.getString("avatar");

                        if (child.avatar == null || child.avatar.isEmpty()) {
                            child.avatar = "👧";
                        }
                        childList.add(child);
                    }
                    adapter.notifyDataSetChanged();

                    if (childList.isEmpty()) {
                        Toast.makeText(this, "No children found. Please add a child.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading accounts: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateNavHeader(String childName, String childAvatar) {
        if (headerView == null) return;

        TextView tvChildName = headerView.findViewById(R.id.tvChildName);
        ImageView headerProfileImage = headerView.findViewById(R.id.headerProfileImage);
        TextView headerProfileIcon = headerView.findViewById(R.id.headerProfileIcon);

        if (tvChildName != null) {
            tvChildName.setText(childName != null && !childName.isEmpty() ? childName : "Child");
        }

        if (headerProfileImage != null && headerProfileIcon != null) {
            if (childAvatar != null && !childAvatar.isEmpty() &&
                    (childAvatar.startsWith("content://") || childAvatar.startsWith("file://") ||
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
            // Already in account page
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ChildProfileActivity.class));
        } else if (id == R.id.nav_progress_tracker) {
            startActivity(new Intent(this, ProgressTrackerActivity.class));
        } else if (id == R.id.nav_add_activity) {
            startActivity(new Intent(this, AddNewActivityActivity.class));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", (dialog, which) -> {
            Intent intent = new Intent(AccountActivity.this, GoodbyeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private static class ChildProfile {
        String id;
        String name;
        String age;
        String gender;
        String avatar;
    }

    private class ChildSelectionAdapter extends RecyclerView.Adapter<ChildSelectionAdapter.ViewHolder> {
        private final List<ChildProfile> children;

        public ChildSelectionAdapter(List<ChildProfile> children) {
            this.children = children;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_child_profile, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChildProfile child = children.get(position);

            if (child != null) {
                String avatar = child.avatar != null && !child.avatar.isEmpty() ? child.avatar : "👧";
                holder.tvAvatar.setText(avatar);
                holder.tvChildName.setText(child.name != null ? child.name : "Child");

                String ageText = child.age != null && !child.age.isEmpty() ? child.age : "-";
                holder.tvChildAge.setText("Age: " + ageText);
                holder.radioSelect.setChecked(position == selectedPosition);

                holder.itemView.setOnClickListener(v -> {
                    selectedPosition = position;
                    notifyDataSetChanged();
                });

                holder.radioSelect.setOnClickListener(v -> {
                    selectedPosition = position;
                    notifyDataSetChanged();
                });
            }
        }

        @Override
        public int getItemCount() {
            return children != null ? children.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvChildName, tvChildAge;
            RadioButton radioSelect;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAvatar = itemView.findViewById(R.id.tvAvatar);
                tvChildName = itemView.findViewById(R.id.tvChildName);
                tvChildAge = itemView.findViewById(R.id.tvChildAge);
                radioSelect = itemView.findViewById(R.id.radioSelect);
            }
        }
    }
}