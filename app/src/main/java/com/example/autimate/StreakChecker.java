package com.example.autimate;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StreakChecker {

    /**
     * Check and update streak, return true if a new streak milestone was reached
     */
    public static boolean checkAndUpdateStreak(Context context) {
        SharedPreferences rewardPrefs = context.getSharedPreferences("RewardPrefs", Context.MODE_PRIVATE);
        SharedPreferences childPrefs = context.getSharedPreferences("ChildPrefs", Context.MODE_PRIVATE);

        String childId = childPrefs.getString("childId", "");
        if (childId.isEmpty()) {
            return false;
        }

        String childKey = childId + "_";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String today = sdf.format(new Date());

        String completedDates = rewardPrefs.getString(childKey + "completedDates", "");
        if (completedDates.isEmpty()) {
            return false;
        }

        // Calculate current streak
        int streak = calculateStreak(completedDates);

        // Get previous streak from prefs
        int previousStreak = rewardPrefs.getInt(childKey + "currentStreak", 0);

        // Update streak
        rewardPrefs.edit().putInt(childKey + "currentStreak", streak).apply();

        // Check if we hit a milestone (3, 7, 14, 30, 50, 100 days)
        boolean isMilestone = (streak > 0) && (
                streak == 3 ||
                        streak == 7 ||
                        streak == 14 ||
                        streak == 30 ||
                        streak == 50 ||
                        streak == 100
        );

        // Send notification if milestone reached
        if (isMilestone && streak > previousStreak) {
            NotificationHelper.showStreakNotification(context, streak);
            return true;
        }

        return false;
    }

    private static int calculateStreak(String completedDates) {
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
                if (date.trim().equals(dateStr)) {
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
}