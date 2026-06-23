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
import androidx.core.content.ContextCompat;

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

    // Badge layouts
    private LinearLayout badgeFirstStar, badgeOneRoll, badgeSuperHelper, badgeStarMaster;
    private TextView badge1Progress, badge2Progress, badge3Progress, badge4Progress;

    // Badge ImageViews
    private ImageView ivBadgeFirstStar, ivBadgeOneRoll, ivBadgeSuperHelper, ivBadgeStarMaster;

    private SharedPreferences rewardPrefs;
    private int totalPoints = 0;
    private int totalTasksCompleted = 0;
    private int currentStreak = 0;
    private List<RewardRecord> recentRewards = new ArrayList<>();

    // Badge class
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

    private void showBadgeInfo(
            String title,
            String description) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        rewardPrefs = getSharedPreferences("RewardPrefs", MODE_PRIVATE);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        tvPoints = findViewById(R.id.tvPoints);
        tvRecentReward = findViewById(R.id.tvRecentReward);
        tvRecentPoints = findViewById(R.id.tvRecentPoints);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        recentRewardsContainer = findViewById(R.id.recentRewardsContainer);

        // Badge views
        badgeFirstStar = findViewById(R.id.badgeFirstStar);
        badgeOneRoll = findViewById(R.id.badgeOneRoll);
        badgeSuperHelper = findViewById(R.id.badgeSuperHelper);
        badgeStarMaster = findViewById(R.id.badgeStarMaster);

        badge1Progress = findViewById(R.id.badge1Progress);
        badge2Progress = findViewById(R.id.badge2Progress);
        badge3Progress = findViewById(R.id.badge3Progress);
        badge4Progress = findViewById(R.id.badge4Progress);

        // Badge ImageViews
        ivBadgeFirstStar = findViewById(R.id.ivBadgeFirstStar);
        ivBadgeOneRoll = findViewById(R.id.ivBadgeOneRoll);
        ivBadgeSuperHelper = findViewById(R.id.ivBadgeSuperHelper);
        ivBadgeStarMaster = findViewById(R.id.ivBadgeStarMaster);

        // Initialize badges
        initializeBadges();

        // Load data
        loadRewardData();
        loadRecentRewards();

        // Update displays
        updatePointsDisplay();
        updateBadges();
        updateRecentRewards();

        // Check for new rewards
        checkAndShowNewReward();

        // Badge click listeners
        badgeFirstStar.setOnClickListener(v ->
                showBadgeInfo(
                        "⭐ First Star",
                        "Complete 2 activities (10 points)"));

        badgeOneRoll.setOnClickListener(v ->
                showBadgeInfo(
                        "🔥 One Roll",
                        "Maintain a 3 day streak"));

        badgeSuperHelper.setOnClickListener(v ->
                showBadgeInfo(
                        "💪 Super Helper",
                        "Complete all 5 activities"));

        badgeStarMaster.setOnClickListener(v ->
                showBadgeInfo(
                        "👑 Star Master",
                        "Earn 50 points"));

        // Button click listeners
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RewardActivity.this,
                    ChildHomeActivity.class
            );
            startActivity(intent);
            finish();
        });

        btnBackToHome.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            RewardActivity.this,
                            ChildHomeActivity.class));

            finish();

        });
    }

    private void initializeBadges() {
        // UPDATED: Super Helper now requires 5 activities (all activities)
        badges.put("first_star", new Badge("First Star", "Complete 2 activities", 10, "⭐"));
        badges.put("one_roll", new Badge("One Roll", "3 days streak", 3, "🔥"));
        badges.put("super_helper", new Badge("Super Helper", "Complete all 5 activities", 5, "💪"));
        badges.put("star_master", new Badge("Star Master", "Earn 50 points", 50, "👑"));
    }

    private void loadRewardData() {
        totalPoints = rewardPrefs.getInt("totalPoints", 0);
        totalTasksCompleted = rewardPrefs.getInt("totalTasksCompleted", 0);
        currentStreak = calculateCurrentStreak();

        // Load badge progress
        for (Map.Entry<String, Badge> entry : badges.entrySet()) {
            String key = entry.getKey();
            Badge badge = entry.getValue();
            badge.current = rewardPrefs.getInt(key + "_progress", 0);
            badge.isUnlocked = rewardPrefs.getBoolean(key + "_unlocked", false);
        }

        // Update task-specific counts
        updateTaskSpecificCounts();
    }

    private void updateTaskSpecificCounts() {
        // Load task-specific counts for badges like brush teeth count
        String taskHistory = rewardPrefs.getString("taskHistory", "");
        if (!taskHistory.isEmpty()) {
            String[] entries = taskHistory.split("\\|");
            int brushTeethCount = 0;

            for (String entry : entries) {
                if (!entry.isEmpty()) {
                    String[] parts = entry.split(",");
                    if (parts.length >= 1) {
                        String taskName = parts[0];
                        if (taskName.equals("Brush Teeth")) {
                            brushTeethCount++;
                        }
                    }
                }
            }

            // Store brush teeth count for potential future badges
            rewardPrefs.edit().putInt("brushTeethCount", brushTeethCount).apply();
        }
    }

    private int calculateCurrentStreak() {
        String completedDates = rewardPrefs.getString("completedDates", "");
        if (completedDates.isEmpty()) return 0;

        String[] dates = completedDates.split(",");
        if (dates.length == 0) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String today = sdf.format(new Date());

        int streak = 0;
        Calendar checkDate = Calendar.getInstance();

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
        String historyJson = rewardPrefs.getString("taskHistory", "");
        if (!historyJson.isEmpty()) {
            String[] entries = historyJson.split("\\|");
            // Get last 3 entries
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
        tvPoints.setText(
                String.valueOf(totalPoints)
        );
    }

    private void updateBadges() {
        // Update First Star badge
        updateBadgeDisplay(badgeFirstStar, badge1Progress, badges.get("first_star"), ivBadgeFirstStar);

        // Update One Roll badge
        badges.get("one_roll").current = currentStreak;
        updateBadgeDisplay(badgeOneRoll, badge2Progress, badges.get("one_roll"), ivBadgeOneRoll);

        // Update Super Helper badge
        badges.get("super_helper").current = totalTasksCompleted;
        updateBadgeDisplay(badgeSuperHelper, badge3Progress, badges.get("super_helper"), ivBadgeSuperHelper);

        // Update Star Master badge
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
            // Make icon brighter when unlocked
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
            // Dim icon when locked
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
            tvDateTime.setText(reward.date + " - " + reward.time);
            tvPoints.setText("+" + reward.points);

            rewardView.setOnClickListener(v -> {

                Toast.makeText(
                        RewardActivity.this,
                        reward.taskName +
                                "\n+" +
                                reward.points +
                                " points",
                        Toast.LENGTH_SHORT
                ).show();

            });

            recentRewardsContainer.addView(rewardView);
        }
    }

    private void checkAndShowNewReward() {
        boolean hasNewReward = false;
        StringBuilder newRewardsMessage = new StringBuilder();

        // Check each badge for new unlocks
        for (Map.Entry<String, Badge> entry : badges.entrySet()) {
            Badge badge = entry.getValue();
            boolean wasUnlocked = rewardPrefs.getBoolean(entry.getKey() + "_unlocked", false);
            boolean shouldBeUnlocked = badge.current >= badge.target;

            if (!wasUnlocked && shouldBeUnlocked) {
                badge.isUnlocked = true;
                rewardPrefs.edit().putBoolean(entry.getKey() + "_unlocked", true).apply();
                hasNewReward = true;
                newRewardsMessage.append("🏆 ").append(badge.name).append(" - ").append(badge.description).append("\n\n");
            }
        }

        if (hasNewReward) {
            showNewBadgeDialog(newRewardsMessage.toString());
        } else {
            // Show the most recent completed task as the "reward"
            if (!recentRewards.isEmpty()) {
                RewardRecord latest = recentRewards.get(0);
                tvRecentReward.setText("Great job! You completed \"" + latest.taskName + "\"");
                tvRecentPoints.setText("+" + latest.points);
            }
        }
    }

    private void showNewBadgeDialog(String badgesMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_badge, null);

        TextView tvBadgesMessage = dialogView.findViewById(R.id.tvBadgesMessage);
        Button btnAwesome = dialogView.findViewById(R.id.btnAwesome);

        tvBadgesMessage.setText("Congratulations! You've earned:\n\n" + badgesMessage);

        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnAwesome.setOnClickListener(v -> {
            dialog.dismiss();
            updateBadges(); // Refresh badges display
        });
    }

    // Static methods for adding progress from other activities
    public static void addTaskProgress(Context context, String taskName, int points) {
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);

        // Update total points
        int currentPoints = prefs.getInt("totalPoints", 0);
        prefs.edit().putInt("totalPoints", currentPoints + points).apply();

        // Update total tasks completed
        int totalTasks = prefs.getInt("totalTasksCompleted", 0);
        prefs.edit().putInt("totalTasksCompleted", totalTasks + 1).apply();

        // Update badge progress for First Star and Star Master (based on points)
        int firstStarProgress = prefs.getInt("first_star_progress", 0);
        prefs.edit().putInt("first_star_progress", firstStarProgress + points).apply();

        int starMasterProgress = prefs.getInt("star_master_progress", 0);
        prefs.edit().putInt("star_master_progress", starMasterProgress + points).apply();

        // Update Super Helper progress (based on tasks) - target is 5
        int superHelperProgress = prefs.getInt("super_helper_progress", 0);
        prefs.edit().putInt("super_helper_progress", superHelperProgress + 1).apply();

        // Update streak (for One Roll badge)
        updateStreak(context);

        // Add to task history
        addTaskHistory(context, taskName, points);

        // Check for badge unlocks
        checkBadgeUnlocks(context);
    }

    private static void updateStreak(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String today = sdf.format(new Date());

        String lastCompletedDate = prefs.getString("lastCompletedDate", "");
        String completedDates = prefs.getString("completedDates", "");

        if (!completedDates.contains(today)) {
            String newDates = completedDates.isEmpty() ? today : completedDates + "," + today;
            prefs.edit().putString("completedDates", newDates).apply();
            prefs.edit().putString("lastCompletedDate", today).apply();
        }
    }

    private static void checkBadgeUnlocks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);

        // Check First Star (10 points)
        int points = prefs.getInt("totalPoints", 0);
        if (points >= 10 && !prefs.getBoolean("first_star_unlocked", false)) {
            prefs.edit().putBoolean("first_star_unlocked", true).apply();
        }

        // Check Star Master (50 points)
        if (points >= 50 && !prefs.getBoolean("star_master_unlocked", false)) {
            prefs.edit().putBoolean("star_master_unlocked", true).apply();
        }

        // Check Super Helper (5 tasks - all activities completed)
        int tasks = prefs.getInt("totalTasksCompleted", 0);
        if (tasks >= 5 && !prefs.getBoolean("super_helper_unlocked", false)) {
            prefs.edit().putBoolean("super_helper_unlocked", true).apply();
        }

        // Check One Roll (3 day streak)
        int streak = calculateStreakFromPrefs(prefs);
        if (streak >= 3 && !prefs.getBoolean("one_roll_unlocked", false)) {
            prefs.edit().putBoolean("one_roll_unlocked", true).apply();
        }
    }

    private static int calculateStreakFromPrefs(SharedPreferences prefs) {
        String completedDates = prefs.getString("completedDates", "");
        if (completedDates.isEmpty()) return 0;

        String[] dates = completedDates.split(",");
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

    private static void addTaskHistory(Context context, String taskName, int points) {
        SharedPreferences prefs = context.getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String today = sdfDate.format(new Date());
        String now = sdfTime.format(new Date());

        String historyEntry = taskName + "," + today + "," + now + "," + points;
        String existingHistory = prefs.getString("taskHistory", "");
        String newHistory = existingHistory.isEmpty() ? historyEntry : existingHistory + "|" + historyEntry;
        prefs.edit().putString("taskHistory", newHistory).apply();
    }
}