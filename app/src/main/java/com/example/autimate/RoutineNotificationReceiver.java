package com.example.autimate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RoutineNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get child info
        SharedPreferences childPrefs = context.getSharedPreferences("ChildPrefs", Context.MODE_PRIVATE);
        String childId = childPrefs.getString("childId", "");
        String childName = childPrefs.getString("childName", "Child");

        if (childId.isEmpty()) {
            return;
        }

        // Load activities
        SharedPreferences routinePrefs = context.getSharedPreferences("RoutinePrefs", Context.MODE_PRIVATE);
        String savedActivities = routinePrefs.getString("activities_" + childId, "");
        List<String> activityList = new ArrayList<>();

        if (!savedActivities.isEmpty()) {
            String[] activities = savedActivities.split(",");
            for (String act : activities) {
                activityList.add(act.trim());
            }
        }

        if (activityList.isEmpty()) {
            return;
        }

        // Check what to remind based on time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Get today's completed activities
        SharedPreferences rewardPrefs = context.getSharedPreferences("RewardPrefs", Context.MODE_PRIVATE);
        String childKey = childId + "_";
        SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayKey = dateKeyFormat.format(new Date());

        Set<String> completedToday = new HashSet<>();
        String history = rewardPrefs.getString(childKey + "taskHistory", "");
        if (!history.isEmpty()) {
            for (String entry : history.split("\\|")) {
                if (entry.isEmpty()) continue;
                String[] parts = entry.split(",");
                if (parts.length >= 2) {
                    String taskName = parts[0].trim();
                    String date = parts[1].trim();
                    if (date.equals(todayKey)) {
                        completedToday.add(taskName);
                    }
                }
            }
        }

        // Count completed activities
        int completedCount = 0;
        for (String activity : activityList) {
            if (completedToday.contains(activity)) {
                completedCount++;
            }
        }

        String routineMessage = "";
        String routineTitle = "";

        // Morning routine (7-9 AM)
        if (hour >= 7 && hour < 9) {
            if (completedCount == 0) {
                routineTitle = "🌅 Good Morning, " + childName + "!";
                routineMessage = "Time to start your morning routine! 🌟";
            } else if (completedCount < activityList.size()) {
                routineTitle = "🌅 Keep Going, " + childName + "!";
                routineMessage = "You've completed " + completedCount + " activities. " +
                        (activityList.size() - completedCount) + " more to go!";
            }
        }
        // Lunch/Afternoon (12-2 PM)
        else if (hour >= 12 && hour < 14) {
            if (completedCount < activityList.size() / 2) {
                routineTitle = "☀️ Afternoon Check-in!";
                routineMessage = "Don't forget your afternoon routine! 🦷";
            }
        }
        // Evening (8-10 PM)
        else if (hour >= 20 && hour < 22) {
            if (completedCount < activityList.size()) {
                routineTitle = "🌙 Good Evening, " + childName + "!";
                routineMessage = "Time to complete your evening routine! 🌙";
            }
        }

        // Show notification if there's a message
        if (!routineMessage.isEmpty()) {
            NotificationHelper.showRoutineNotification(context, routineTitle, routineMessage);
        }
    }
}