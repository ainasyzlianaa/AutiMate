package com.example.autimate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RewardActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvPoints, tvRecentReward, tvRecentPoints;
    private Button btnBackToHome;
    private LinearLayout recentRewardsContainer;
    private GridLayout badgesGrid;

    private LinearLayout badgeFirstStar, badgeOneRoll, badgeSuperHelper, badgeStarMaster;
    private TextView badge1Progress, badge2Progress, badge3Progress, badge4Progress;
    private ImageView ivBadgeFirstStar, ivBadgeOneRoll, ivBadgeSuperHelper, ivBadgeStarMaster;
    private TextView tvBadgeNameFirstStar, tvBadgeNameOneRoll, tvBadgeNameSuperHelper, tvBadgeNameStarMaster;

    private SharedPreferences rewardPrefs;
    private FirebaseFirestore db;
    private String childId;
    private String childName;
    private int totalPoints = 0;
    private int totalTasksCompleted = 0;
    private int currentStreak = 0;
    private List<RewardRecord> recentRewards = new ArrayList<>();

    static class Badge {
        String name;
        String description;
        int target;
        int current;
        boolean isUnlocked;
        String icon;

        Badge(String name, String description, int target, String icon) {
            this.name = name;
            this.description = description;
            this.target = target;
            this.icon = icon;
            this.isUnlocked = false;
            this.current = 0;
        }
    }

    static class RewardRecord {
        String taskName;
        String date;
        String time;
        int points;

        RewardRecord(String taskName, String date, String time, int points) {
            this.taskName = taskName;
            this.date = date;
            this.time = time;
            this.points = points;
        }
    }

    private Map<String, Badge> badges = new HashMap<>();

    private void showBadgeInfo(String title, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(description);
        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (okButton != null) {
            int nightMode = AppCompatDelegate.getDefaultNightMode();
            boolean isDarkMode = (nightMode == AppCompatDelegate.MODE_NIGHT_YES) ||
                    (nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM &&
                            (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES);

            if (isDarkMode) {
                okButton.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                okButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
            okButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        rewardPrefs = getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        SharedPreferences childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        childId = childPrefs.getString("childId", "default");
        childName = childPrefs.getString("childName", "Child");

        initViews();
        initializeBadges();

        // Load from SharedPreferences
        loadRewardData();
        updateUI();

        // Update from Firestore
        loadDataFromFirestore();

        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPoints = findViewById(R.id.tvPoints);
        tvRecentReward = findViewById(R.id.tvRecentReward);
        tvRecentPoints = findViewById(R.id.tvRecentPoints);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        recentRewardsContainer = findViewById(R.id.recentRewardsContainer);

        badgeFirstStar = findViewById(R.id.badgeFirstStar);
        badgeOneRoll = findViewById(R.id.badgeOneRoll);
        badgeSuperHelper = findViewById(R.id.badgeSuperHelper);
        badgeStarMaster = findViewById(R.id.badgeStarMaster);

        badge1Progress = findViewById(R.id.badge1Progress);
        badge2Progress = findViewById(R.id.badge2Progress);
        badge3Progress = findViewById(R.id.badge3Progress);
        badge4Progress = findViewById(R.id.badge4Progress);

        ivBadgeFirstStar = findViewById(R.id.ivBadgeFirstStar);
        ivBadgeOneRoll = findViewById(R.id.ivBadgeOneRoll);
        ivBadgeSuperHelper = findViewById(R.id.ivBadgeSuperHelper);
        ivBadgeStarMaster = findViewById(R.id.ivBadgeStarMaster);

        tvBadgeNameFirstStar = findViewById(R.id.tvBadgeNameFirstStar);
        tvBadgeNameOneRoll = findViewById(R.id.tvBadgeNameOneRoll);
        tvBadgeNameSuperHelper = findViewById(R.id.tvBadgeNameSuperHelper);
        tvBadgeNameStarMaster = findViewById(R.id.tvBadgeNameStarMaster);
    }

    private void setupClickListeners() {
        badgeFirstStar.setOnClickListener(v ->
                showBadgeInfo("⭐ First Star", "Complete 2 activities (10 points)"));

        badgeOneRoll.setOnClickListener(v ->
                showBadgeInfo("🔥 One Roll", "Maintain a 3 day streak"));

        badgeSuperHelper.setOnClickListener(v ->
                showBadgeInfo("💪 Super Helper", "Complete all 5 activities"));

        badgeStarMaster.setOnClickListener(v ->
                showBadgeInfo("👑 Star Master", "Earn 50 points"));

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RewardActivity.this, ChildHomeActivity.class);
            startActivity(intent);
            finish();
        });

        btnBackToHome.setOnClickListener(v -> {
            startActivity(new Intent(RewardActivity.this, ChildHomeActivity.class));
            finish();
        });
    }

    private void initializeBadges() {
        badges.put("first_star", new Badge("First Star", "Complete 2 activities", 10, "⭐"));
        badges.put("one_roll", new Badge("One Roll", "3 days streak", 3, "🔥"));
        badges.put("super_helper", new Badge("Super Helper", "Complete all 5 activities", 5, "💪"));
        badges.put("star_master", new Badge("Star Master", "Earn 50 points", 50, "👑"));
    }

    private void loadDataFromFirestore() {
        if (childId == null || childId.isEmpty() || childId.equals("default")) {
            return;
        }

        db.collection("children").document(childId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        loadFromFirestore(documentSnapshot);
                        saveToSharedPreferences();
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    // Silent fail - already showing cached data
                });
    }

    private void loadFromFirestore(DocumentSnapshot document) {
        Long points = document.getLong("totalPoints");
        totalPoints = points != null ? points.intValue() : 0;

        Long tasks = document.getLong("totalTasksCompleted");
        totalTasksCompleted = tasks != null ? tasks.intValue() : 0;

        Long streak = document.getLong("currentStreak");
        currentStreak = streak != null ? streak.intValue() : 0;

        for (Map.Entry<String, Badge> entry : badges.entrySet()) {
            String key = entry.getKey();
            Badge badge = entry.getValue();

            Long current = document.getLong(key + "_progress");
            badge.current = current != null ? current.intValue() : 0;

            Boolean unlocked = document.getBoolean(key + "_unlocked");
            badge.isUnlocked = unlocked != null ? unlocked : false;
        }

        recentRewards.clear();
        String history = document.getString("taskHistory");
        if (history != null && !history.isEmpty()) {
            String[] entries = history.split("\\|");
            int start = Math.max(0, entries.length - 3);
            for (int i = entries.length - 1; i >= start; i--) {
                if (!entries[i].isEmpty()) {
                    String[] parts = entries[i].split(",");
                    if (parts.length >= 4) {
                        RewardRecord record = new RewardRecord(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]));
                        recentRewards.add(record);
                    }
                }
            }
        }
    }

    private void saveToSharedPreferences() {
        String childKey = childId + "_";
        SharedPreferences.Editor editor = rewardPrefs.edit();
        editor.putInt(childKey + "totalPoints", totalPoints);
        editor.putInt(childKey + "totalTasksCompleted", totalTasksCompleted);
        editor.putInt(childKey + "currentStreak", currentStreak);

        for (Map.Entry<String, Badge> entry : badges.entrySet()) {
            String key = entry.getKey();
            Badge badge = entry.getValue();
            editor.putInt(childKey + key + "_progress", badge.current);
            editor.putBoolean(childKey + key + "_unlocked", badge.isUnlocked);
        }
        editor.apply();
    }

    private String getChildKey(String key) {
        return childId + "_" + key;
    }

    private void loadRewardData() {
        totalPoints = rewardPrefs.getInt(getChildKey("totalPoints"), 0);
        totalTasksCompleted = rewardPrefs.getInt(getChildKey("totalTasksCompleted"), 0);
        currentStreak = calculateCurrentStreak();

        for (Map.Entry<String, Badge> entry : badges.entrySet()) {
            String key = entry.getKey();
            Badge badge = entry.getValue();
            badge.current = rewardPrefs.getInt(getChildKey(key + "_progress"), 0);
            badge.isUnlocked = rewardPrefs.getBoolean(getChildKey(key + "_unlocked"), false);
        }

        loadRecentRewards();
    }

    private void updateUI() {
        updatePointsDisplay();
        updateBadges();
        updateRecentRewards();
        checkAndShowNewReward();
    }

    private int calculateCurrentStreak() {
        String completedDates = rewardPrefs.getString(getChildKey("completedDates"), "");
        if (completedDates.isEmpty()) return 0;

        String[] dates = completedDates.split(",");
        if (dates.length == 0) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        Calendar checkDate = Calendar.getInstance();

        int streak = 0;
        for (int i = 0; i < 30; i++) {
            String dateStr = sdf.format(checkDate.getTime());
            boolean found = false;
            for (String date : dates) {
                if (date.equals(dateStr)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                streak++;
                checkDate.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }

        return streak;
    }

    private void loadRecentRewards() {
        recentRewards.clear();
        String historyJson = rewardPrefs.getString(getChildKey("taskHistory"), "");
        if (!historyJson.isEmpty()) {
            String[] entries = historyJson.split("\\|");
            int start = Math.max(0, entries.length - 3);
            for (int i = entries.length - 1; i >= start; i--) {
                if (!entries[i].isEmpty()) {
                    String[] parts = entries[i].split(",");
                    if (parts.length >= 4) {
                        RewardRecord record = new RewardRecord(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]));
                        recentRewards.add(record);
                    }
                }
            }
        }
    }

    private void updatePointsDisplay() {
        tvPoints.setText(String.valueOf(totalPoints));
    }

    private void updateBadges() {
        updateBadgeDisplay(badgeFirstStar, badge1Progress, badges.get("first_star"), ivBadgeFirstStar);
        badges.get("one_roll").current = currentStreak;
        updateBadgeDisplay(badgeOneRoll, badge2Progress, badges.get("one_roll"), ivBadgeOneRoll);
        badges.get("super_helper").current = totalTasksCompleted;
        updateBadgeDisplay(badgeSuperHelper, badge3Progress, badges.get("super_helper"), ivBadgeSuperHelper);
        badges.get("star_master").current = totalPoints;
        updateBadgeDisplay(badgeStarMaster, badge4Progress, badges.get("star_master"), ivBadgeStarMaster);
    }

    private void updateBadgeDisplay(LinearLayout badgeLayout, TextView progressText, Badge badge, ImageView badgeIcon) {
        if (badge.isUnlocked) {
            try {
                badgeLayout.setBackgroundResource(R.drawable.badge_background_unlocked);
            } catch (Exception e) {
                badgeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.cream));
            }
            progressText.setText("✓ UNLOCKED!");
            progressText.setTextColor(ContextCompat.getColor(this, R.color.soft_blue));
            badgeLayout.setAlpha(1f);
            badgeIcon.setAlpha(1.0f);
        } else {
            try {
                badgeLayout.setBackgroundResource(R.drawable.badge_background_locked);
            } catch (Exception e) {
                badgeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.light_brown));
            }
            progressText.setText(badge.current + "/" + badge.target);
            progressText.setTextColor(ContextCompat.getColor(this, R.color.khaki));
            badgeLayout.setAlpha(0.6f);
            badgeIcon.setAlpha(0.5f);
        }
    }

    private void updateRecentRewards() {
        recentRewardsContainer.removeAllViews();

        if (recentRewards.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Complete your first task to see rewards! 🌟");
            emptyView.setTextSize(13);
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.light_brown));
            emptyView.setPadding(16, 32, 16, 32);
            emptyView.setGravity(android.view.Gravity.CENTER);
            recentRewardsContainer.addView(emptyView);
            return;
        }

        for (RewardRecord reward : recentRewards) {
            View rewardView = LayoutInflater.from(this).inflate(R.layout.item_reward_history, recentRewardsContainer, false);

            TextView tvTaskName = rewardView.findViewById(R.id.tvTaskName);
            TextView tvDateTime = rewardView.findViewById(R.id.tvDateTime);
            TextView tvPoints = rewardView.findViewById(R.id.tvPoints);

            tvTaskName.setText("• " + reward.taskName);
            tvTaskName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));

            tvDateTime.setText(reward.date + " - " + reward.time);
            tvDateTime.setTextColor(ContextCompat.getColor(this, R.color.light_brown));

            tvPoints.setText("+" + reward.points);
            tvPoints.setTextColor(ContextCompat.getColor(this, R.color.khaki));

            rewardView.setOnClickListener(v -> {
                Toast.makeText(RewardActivity.this,
                        reward.taskName + "\n+" + reward.points + " points",
                        Toast.LENGTH_SHORT).show();
            });

            recentRewardsContainer.addView(rewardView);
        }
    }

    private void checkAndShowNewReward() {
        boolean hasNewReward = false;
        StringBuilder newRewardsMessage = new StringBuilder();

        for (Map.Entry<String, Badge> entry : badges.entrySet()) {
            Badge badge = entry.getValue();
            boolean wasUnlocked = rewardPrefs.getBoolean(getChildKey(entry.getKey() + "_unlocked"), false);
            boolean shouldBeUnlocked = badge.current >= badge.target;

            if (!wasUnlocked && shouldBeUnlocked) {
                badge.isUnlocked = true;
                rewardPrefs.edit().putBoolean(getChildKey(entry.getKey() + "_unlocked"), true).apply();
                hasNewReward = true;
                newRewardsMessage.append("🏆 ").append(badge.name).append(" - ").append(badge.description).append("\n\n");
            }
        }

        if (hasNewReward) {
            showNewBadgeDialog(newRewardsMessage.toString());

            // Send notification for new badge
            for (Map.Entry<String, Badge> entry : badges.entrySet()) {
                Badge badge = entry.getValue();
                if (badge.isUnlocked) {
                    NotificationHelper.showAchievementNotification(
                            this,
                            badge.name,
                            "You earned the " + badge.name + " badge! 🎉"
                    );
                    break; // Only send one notification for the latest badge
                }
            }
        } else {
            if (!recentRewards.isEmpty()) {
                RewardRecord latest = recentRewards.get(0);
                tvRecentReward.setText("Great job! You completed \"" + latest.taskName + "\"");
                tvRecentReward.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
                tvRecentPoints.setText("+" + latest.points);
                tvRecentPoints.setTextColor(ContextCompat.getColor(this, R.color.khaki));
            }
        }
    }

    private void showNewBadgeDialog(String badgesMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_badge, null);

        TextView tvBadgesMessage = dialogView.findViewById(R.id.tvBadgesMessage);
        Button btnAwesome = dialogView.findViewById(R.id.btnAwesome);

        tvBadgesMessage.setText("Congratulations! You've earned:\n\n" + badgesMessage);
        tvBadgesMessage.setTextColor(ContextCompat.getColor(this, R.color.text_dark));

        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnAwesome.setOnClickListener(v -> {
            dialog.dismiss();
            updateBadges();
        });
    }

    public static void addTaskProgress(Context context, String taskName, int points) {
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        SharedPreferences childPrefs = context.getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String childId = childPrefs.getString("childId", "default");

        if (childId == null || childId.isEmpty()) {
            childId = "default";
        }

        String childKey = childId + "_";

        int currentPoints = prefs.getInt(childKey + "totalPoints", 0);
        prefs.edit().putInt(childKey + "totalPoints", currentPoints + points).apply();

        int totalTasks = prefs.getInt(childKey + "totalTasksCompleted", 0);
        prefs.edit().putInt(childKey + "totalTasksCompleted", totalTasks + 1).apply();

        int firstStarProgress = prefs.getInt(childKey + "first_star_progress", 0);
        prefs.edit().putInt(childKey + "first_star_progress", firstStarProgress + points).apply();

        int starMasterProgress = prefs.getInt(childKey + "star_master_progress", 0);
        prefs.edit().putInt(childKey + "star_master_progress", starMasterProgress + points).apply();

        int superHelperProgress = prefs.getInt(childKey + "super_helper_progress", 0);
        prefs.edit().putInt(childKey + "super_helper_progress", superHelperProgress + 1).apply();

        updateStreak(context, childId);
        addTaskHistory(context, taskName, points, childId);
        checkBadgeUnlocks(context, childId);
        saveToFirestore(context, childId);

        // Send notification if streak milestone reached
        StreakChecker.checkAndUpdateStreak(context);
    }

    private static void saveToFirestore(Context context, String childId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        String childKey = childId + "_";

        int totalPoints = prefs.getInt(childKey + "totalPoints", 0);
        int totalTasks = prefs.getInt(childKey + "totalTasksCompleted", 0);
        int currentStreak = prefs.getInt(childKey + "currentStreak", 0);

        boolean firstStarUnlocked = prefs.getBoolean(childKey + "first_star_unlocked", false);
        boolean oneRollUnlocked = prefs.getBoolean(childKey + "one_roll_unlocked", false);
        boolean superHelperUnlocked = prefs.getBoolean(childKey + "super_helper_unlocked", false);
        boolean starMasterUnlocked = prefs.getBoolean(childKey + "star_master_unlocked", false);

        int firstStarProgress = prefs.getInt(childKey + "first_star_progress", 0);
        int oneRollProgress = prefs.getInt(childKey + "one_roll_progress", 0);
        int superHelperProgress = prefs.getInt(childKey + "super_helper_progress", 0);
        int starMasterProgress = prefs.getInt(childKey + "star_master_progress", 0);

        String taskHistory = prefs.getString(childKey + "taskHistory", "");
        String completedDates = prefs.getString(childKey + "completedDates", "");

        Map<String, Object> data = new HashMap<>();
        data.put("totalPoints", totalPoints);
        data.put("totalTasksCompleted", totalTasks);
        data.put("currentStreak", currentStreak);
        data.put("first_star_unlocked", firstStarUnlocked);
        data.put("one_roll_unlocked", oneRollUnlocked);
        data.put("super_helper_unlocked", superHelperUnlocked);
        data.put("star_master_unlocked", starMasterUnlocked);
        data.put("first_star_progress", firstStarProgress);
        data.put("one_roll_progress", oneRollProgress);
        data.put("super_helper_progress", superHelperProgress);
        data.put("star_master_progress", starMasterProgress);
        data.put("taskHistory", taskHistory);
        data.put("completedDates", completedDates);

        db.collection("children").document(childId)
                .update(data)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {
                    db.collection("children").document(childId)
                            .set(data)
                            .addOnSuccessListener(aVoid -> {})
                            .addOnFailureListener(error -> {});
                });
    }

    private static void updateStreak(Context context, String childId) {
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        String childKey = childId + "_";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String today = sdf.format(new Date());

        String completedDates = prefs.getString(childKey + "completedDates", "");

        int currentStreak = 0;
        Calendar checkDate = Calendar.getInstance();
        for (int i = 0; i < 30; i++) {
            String dateStr = sdf.format(checkDate.getTime());
            if (completedDates.contains(dateStr)) {
                currentStreak++;
                checkDate.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        prefs.edit().putInt(childKey + "currentStreak", currentStreak).apply();

        if (!completedDates.contains(today)) {
            String newDates = completedDates.isEmpty() ? today : completedDates + "," + today;
            prefs.edit().putString(childKey + "completedDates", newDates).apply();
        }
    }

    private static void checkBadgeUnlocks(Context context, String childId) {
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        String childKey = childId + "_";

        int points = prefs.getInt(childKey + "totalPoints", 0);
        if (points >= 10 && !prefs.getBoolean(childKey + "first_star_unlocked", false)) {
            prefs.edit().putBoolean(childKey + "first_star_unlocked", true).apply();
        }

        if (points >= 50 && !prefs.getBoolean(childKey + "star_master_unlocked", false)) {
            prefs.edit().putBoolean(childKey + "star_master_unlocked", true).apply();
        }

        int tasks = prefs.getInt(childKey + "totalTasksCompleted", 0);
        if (tasks >= 5 && !prefs.getBoolean(childKey + "super_helper_unlocked", false)) {
            prefs.edit().putBoolean(childKey + "super_helper_unlocked", true).apply();
        }

        int streak = prefs.getInt(childKey + "currentStreak", 0);
        if (streak >= 3 && !prefs.getBoolean(childKey + "one_roll_unlocked", false)) {
            prefs.edit().putBoolean(childKey + "one_roll_unlocked", true).apply();
        }
    }

    private static void addTaskHistory(Context context, String taskName, int points, String childId) {
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        String childKey = childId + "_";
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String today = sdfDate.format(new Date());
        String now = sdfTime.format(new Date());

        String historyEntry = taskName + "," + today + "," + now + "," + points;
        String existingHistory = prefs.getString(childKey + "taskHistory", "");
        String newHistory = existingHistory.isEmpty() ? historyEntry : existingHistory + "|" + historyEntry;
        prefs.edit().putString(childKey + "taskHistory", newHistory).apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}