package com.example.autimate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChildHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView childTitle, childSubtitle, progressStars, tvStreak, tvProgressText;
    private CardView cardRoutine, cardGame, cardRewards;
    private VideoView mascotVideo;
    private String childName = "";
    private String childId = "";
    private String parentId = "";

    private SharedPreferences childPrefs;
    private SharedPreferences routinePrefs;
    private SharedPreferences rewardPrefs;
    private SharedPreferences progressPrefs;
    private List<String> activityList = new ArrayList<>();

    private FirebaseFirestore db;
    private final SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private MediaPlayer helloMediaPlayer;
    private boolean isDataLoaded = false;

    private BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("PROFILE_UPDATED".equals(intent.getAction())) {
                String childName = intent.getStringExtra("childName");
                String childAvatar = intent.getStringExtra("childAvatar");
                if (childName != null) {
                    String greeting = getGreeting();
                    if (childTitle != null) {
                        childTitle.setText(greeting + ", " + childName + "!");
                    }
                    SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                    prefs.edit().putString("childName", childName).apply();
                    updateNavHeader();
                }
                if (childAvatar != null) {
                    updateNavHeader();
                }
            }
        }
    };

    private BroadcastReceiver activityUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTIVITIES_UPDATED".equals(intent.getAction())) {
                loadActivities();
                updateProgress();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        routinePrefs = getSharedPreferences("RoutinePrefs", MODE_PRIVATE);
        rewardPrefs = getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        progressPrefs = getSharedPreferences("ChildProgress", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        childName = childPrefs.getString("childName", "Friend");
        childId = childPrefs.getString("childId", "");
        parentId = getSharedPreferences("ParentPrefs", MODE_PRIVATE).getString("parentId", "");

        playHelloSound();
        loadActivities();

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

        childTitle = findViewById(R.id.childTitle);
        childSubtitle = findViewById(R.id.childSubtitle);
        progressStars = findViewById(R.id.progressStars);
        tvStreak = findViewById(R.id.tvStreak);
        tvProgressText = findViewById(R.id.tvProgressText);
        cardRoutine = findViewById(R.id.cardRoutine);
        cardGame = findViewById(R.id.cardGame);
        cardRewards = findViewById(R.id.cardRewards);
        mascotVideo = findViewById(R.id.mascotVideo);

        String greeting = getGreeting();
        if (childTitle != null) {
            childTitle.setText(greeting + ", " + childName + "!");
        }
        if (childSubtitle != null) {
            childSubtitle.setText("Let's have a great day!");
        }

        loadMascotVideo();

        // Load from SharedPreferences immediately (fast)
        updateProgress();
        updateStreak();

        // Then update from Firestore in background (slow)
        loadProgressFromFirestore();

        setupClickListeners();
    }

    private void loadProgressFromFirestore() {
        if (childId == null || childId.isEmpty()) {
            return;
        }

        db.collection("children").document(childId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateLocalPrefsFromFirestore(documentSnapshot);
                        // Update UI with fresh data
                        updateProgress();
                        updateStreak();
                    }
                })
                .addOnFailureListener(e -> {
                    // Silent fail - already showing cached data
                });
    }

    private void updateLocalPrefsFromFirestore(DocumentSnapshot document) {
        String childKey = childId + "_";
        SharedPreferences.Editor editor = rewardPrefs.edit();

        Long totalPoints = document.getLong("totalPoints");
        if (totalPoints != null) {
            editor.putInt(childKey + "totalPoints", totalPoints.intValue());
        }

        Long totalTasks = document.getLong("totalTasksCompleted");
        if (totalTasks != null) {
            editor.putInt(childKey + "totalTasksCompleted", totalTasks.intValue());
        }

        Long currentStreak = document.getLong("currentStreak");
        if (currentStreak != null) {
            editor.putInt(childKey + "currentStreak", currentStreak.intValue());
        }

        String taskHistory = document.getString("taskHistory");
        if (taskHistory != null && !taskHistory.isEmpty()) {
            editor.putString(childKey + "taskHistory", taskHistory);
        }

        String completedDates = document.getString("completedDates");
        if (completedDates != null && !completedDates.isEmpty()) {
            editor.putString(childKey + "completedDates", completedDates);
        }

        editor.apply();
    }

    private void setupClickListeners() {
        if (cardRoutine != null) {
            cardRoutine.setOnClickListener(v -> {
                Intent intent = new Intent(ChildHomeActivity.this, RoutineTimeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        if (cardGame != null) {
            cardGame.setOnClickListener(v -> {
                Intent intent = new Intent(ChildHomeActivity.this, GameSelectionActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        if (cardRewards != null) {
            cardRewards.setOnClickListener(v -> {
                Intent intent = new Intent(ChildHomeActivity.this, RewardActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void playHelloSound() {
        try {
            int rawResourceId = getResources().getIdentifier("hello_there", "raw", getPackageName());

            if (rawResourceId != 0) {
                helloMediaPlayer = MediaPlayer.create(this, rawResourceId);
                if (helloMediaPlayer != null) {
                    helloMediaPlayer.setOnPreparedListener(mp -> helloMediaPlayer.start());
                    helloMediaPlayer.setOnCompletionListener(mp -> releaseHelloMediaPlayer());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseHelloMediaPlayer() {
        if (helloMediaPlayer != null) {
            try {
                if (helloMediaPlayer.isPlaying()) {
                    helloMediaPlayer.stop();
                }
                helloMediaPlayer.release();
                helloMediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadActivities() {
        activityList.clear();

        String savedActivities = "";
        if (childId != null && !childId.isEmpty()) {
            savedActivities = routinePrefs.getString("activities_" + childId, "");
        }
        if (savedActivities.isEmpty()) {
            savedActivities = routinePrefs.getString("activities", "");
        }

        if (!savedActivities.isEmpty()) {
            String[] activities = savedActivities.split(",");
            for (String act : activities) {
                activityList.add(act.trim());
            }
            return;
        }

        if (childId != null && !childId.isEmpty() && parentId != null && !parentId.isEmpty()) {
            db.collection("activities")
                    .whereEqualTo("childId", childId)
                    .whereEqualTo("parentId", parentId)
                    .orderBy("order")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        activityList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String name = doc.getString("activityName");
                            if (name != null && !name.isEmpty()) {
                                activityList.add(name);
                            }
                        }
                        saveActivitiesToPrefs();
                        if (activityList.isEmpty()) {
                            addDefaultActivities();
                        }
                        updateProgress();
                    })
                    .addOnFailureListener(e -> {
                        addDefaultActivities();
                        updateProgress();
                    });
        } else {
            addDefaultActivities();
        }
    }

    private void addDefaultActivities() {
        activityList.clear();
        activityList.add("Brush Teeth");
        activityList.add("Eat Foods");
        activityList.add("Wash Hands");
        activityList.add("Sleep");
        activityList.add("Pack School Bag");
        saveActivitiesToPrefs();
    }

    private void saveActivitiesToPrefs() {
        StringBuilder sb = new StringBuilder();
        for (String activity : activityList) {
            if (sb.length() > 0) sb.append(",");
            sb.append(activity);
        }

        String key = (childId != null && !childId.isEmpty()) ? "activities_" + childId : "activities";
        routinePrefs.edit().putString(key, sb.toString()).apply();
    }

    private void loadMascotVideo() {
        try {
            int rawResourceId = getResources().getIdentifier("hello", "raw", getPackageName());

            if (rawResourceId != 0) {
                String videoPath = "android.resource://" + getPackageName() + "/" + rawResourceId;
                Uri videoUri = Uri.parse(videoPath);

                mascotVideo.setVideoURI(videoUri);
                mascotVideo.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mascotVideo.start();
                });

                mascotVideo.setOnErrorListener((mp, what, extra) -> {
                    mascotVideo.setVisibility(View.GONE);
                    return true;
                });
            } else {
                mascotVideo.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mascotVideo.setVisibility(View.GONE);
        }
    }

    private void updateNavHeader() {
        if (navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        TextView tvChildName = headerView.findViewById(R.id.tvChildName);
        ImageView headerProfileImage = headerView.findViewById(R.id.headerProfileImage);
        TextView headerProfileIcon = headerView.findViewById(R.id.headerProfileIcon);

        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String childName = prefs.getString("childName", "Friend");
        String avatar = prefs.getString("childAvatar", "👧");

        if (tvChildName != null) {
            tvChildName.setText(childName);
        }

        if (headerProfileImage != null && headerProfileIcon != null) {
            if (avatar != null && (avatar.startsWith("content://") || avatar.startsWith("file://") ||
                    avatar.startsWith("http://") || avatar.startsWith("https://"))) {
                try {
                    Glide.with(this)
                            .load(avatar)
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
                headerProfileIcon.setText(avatar != null ? avatar : "👧");
            }
        }
    }

    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Good morning";
        } else if (hour < 18) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }

    private Set<String> getCompletedTasksForToday() {
        Set<String> completedTasks = new HashSet<>();
        String todayKey = dateKeyFormat.format(new Date());
        String childKey = childId + "_";

        String history = rewardPrefs.getString(childKey + "taskHistory", "");
        if (!history.isEmpty()) {
            for (String entry : history.split("\\|")) {
                if (entry.isEmpty()) continue;
                String[] parts = entry.split(",");
                if (parts.length >= 2) {
                    String taskName = parts[0].trim();
                    String date = parts[1].trim();
                    if (date.equals(todayKey)) {
                        completedTasks.add(taskName);
                    }
                }
            }
        }

        // Legacy fallback
        String legacyHistory = rewardPrefs.getString("taskHistory", "");
        if (!legacyHistory.isEmpty()) {
            for (String entry : legacyHistory.split("\\|")) {
                if (entry.isEmpty()) continue;
                String[] parts = entry.split(",");
                if (parts.length >= 2) {
                    String taskName = parts[0].trim();
                    String date = parts[1].trim();
                    if (date.equals(todayKey)) {
                        completedTasks.add(taskName);
                    }
                }
            }
        }

        return completedTasks;
    }

    private void updateProgress() {
        if (progressStars != null && tvProgressText != null) {
            int totalActivities = activityList.size();

            Set<String> completedToday = getCompletedTasksForToday();
            int completedCount = 0;
            for (String activity : activityList) {
                if (completedToday.contains(activity)) {
                    completedCount++;
                }
            }

            tvProgressText.setText(completedCount + " / " + totalActivities + " Activities");

            int totalStars = Math.min(totalActivities, 5);
            int filledStars = Math.min(completedCount, totalStars);

            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < totalStars; i++) {
                if (i < filledStars) {
                    stars.append("⭐");
                } else {
                    stars.append("☆");
                }
            }
            progressStars.setText(stars.toString());
            progressStars.setTextColor(android.graphics.Color.BLACK);
        }
    }

    private void updateStreak() {
        if (tvStreak != null) {
            int streak = progressPrefs.getInt("streak", 0);

            String childKey = childId + "_";
            int rewardStreak = rewardPrefs.getInt(childKey + "currentStreak", 0);
            if (rewardStreak > streak) {
                streak = rewardStreak;
            }

            if (streak == 0) {
                tvStreak.setText("Start today! 🔥");
            } else {
                tvStreak.setText("🔥 " + streak + " days!");
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
        } else if (id == R.id.nav_view_rewards) {
            startActivity(new Intent(this, RewardActivity.class));
        } else if (id == R.id.nav_theme) {
            startActivity(new Intent(this, ThemeCustomizationActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else if (id == R.id.nav_logout) {
            logout();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    /**
     * Custom logout dialog with white buttons in dark mode
     */
    private void logout() {
        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Get buttons from the custom layout
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnYes = dialogView.findViewById(R.id.btnYes);

        // Check if dark mode is active
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        boolean isDarkMode = (nightMode == AppCompatDelegate.MODE_NIGHT_YES) ||
                (nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM &&
                        (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        // Set button colors based on theme
        if (isDarkMode) {
            // Dark mode: WHITE text
            btnCancel.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnYes.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            // Light mode: BLACK text
            btnCancel.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            btnYes.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }

        // Set click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(ChildHomeActivity.this, GoodbyeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter profileFilter = new IntentFilter("PROFILE_UPDATED");
        registerReceiver(profileUpdateReceiver, profileFilter);

        IntentFilter activityFilter = new IntentFilter("ACTIVITIES_UPDATED");
        registerReceiver(activityUpdateReceiver, activityFilter);

        childName = childPrefs.getString("childName", "Friend");
        childId = childPrefs.getString("childId", "");

        loadActivities();
        // Update from local cache instantly, then from Firestore in background
        updateProgress();
        updateStreak();
        loadProgressFromFirestore();

        if (mascotVideo != null && !mascotVideo.isPlaying()) {
            mascotVideo.start();
        }

        String greeting = getGreeting();
        if (childTitle != null) {
            childTitle.setText(greeting + ", " + childName + "!");
        }
        updateNavHeader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(profileUpdateReceiver);
        } catch (Exception e) {}

        try {
            unregisterReceiver(activityUpdateReceiver);
        } catch (Exception e) {}

        if (mascotVideo != null && mascotVideo.isPlaying()) {
            mascotVideo.pause();
        }

        releaseHelloMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mascotVideo != null) {
            mascotVideo.stopPlayback();
        }
        releaseHelloMediaPlayer();
    }
}