package com.example.autimate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    // Notification Channel IDs
    public static final String CHANNEL_ROUTINE = "routine_channel";
    public static final String CHANNEL_STREAK = "streak_channel";
    public static final String CHANNEL_ACHIEVEMENT = "achievement_channel";

    // Notification IDs
    public static final int NOTIFICATION_ROUTINE = 1001;
    public static final int NOTIFICATION_STREAK = 1002;
    public static final int NOTIFICATION_ACHIEVEMENT = 1003;

    /**
     * Create notification channels
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Routine Channel
            NotificationChannel routineChannel = new NotificationChannel(
                    CHANNEL_ROUTINE,
                    "Routine Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            routineChannel.setDescription("Daily routine reminders for your child");
            routineChannel.enableVibration(true);
            routineChannel.enableLights(true);
            routineChannel.setLightColor(ContextCompat.getColor(context, R.color.soft_blue));
            notificationManager.createNotificationChannel(routineChannel);

            // Streak Channel
            NotificationChannel streakChannel = new NotificationChannel(
                    CHANNEL_STREAK,
                    "Streak Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            streakChannel.setDescription("Streak milestones and achievements");
            streakChannel.enableVibration(true);
            streakChannel.enableLights(true);
            streakChannel.setLightColor(ContextCompat.getColor(context, R.color.khaki));
            notificationManager.createNotificationChannel(streakChannel);

            // Achievement Channel
            NotificationChannel achievementChannel = new NotificationChannel(
                    CHANNEL_ACHIEVEMENT,
                    "Achievement Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            achievementChannel.setDescription("Badges and achievements unlocked");
            achievementChannel.enableVibration(true);
            achievementChannel.enableLights(true);
            achievementChannel.setLightColor(ContextCompat.getColor(context, R.color.soft_pink));
            notificationManager.createNotificationChannel(achievementChannel);
        }
    }

    /**
     * Show a routine reminder notification
     */
    public static void showRoutineNotification(Context context, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent to open ChildHomeActivity when notification is tapped
        Intent intent = new Intent(context, ChildHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ROUTINE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ROUTINE)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 500, 200, 500});

        notificationManager.notify(NOTIFICATION_ROUTINE, builder.build());
    }

    /**
     * Show a streak notification
     */
    public static void showStreakNotification(Context context, int streakCount) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Different messages based on streak count
        String title, message;
        int icon = R.drawable.ic_notification;

        if (streakCount == 3) {
            title = "🔥 3-Day Streak!";
            message = "You're on fire! Keep going!";
            icon = R.drawable.ic_fire;
        } else if (streakCount == 7) {
            title = "🌟 7-Day Streak!";
            message = "A whole week! You're amazing!";
            icon = R.drawable.ic_star;
        } else if (streakCount == 14) {
            title = "🏆 14-Day Streak!";
            message = "Two weeks of awesomeness!";
            icon = R.drawable.ic_trophy;
        } else if (streakCount == 30) {
            title = "👑 30-Day Streak!";
            message = "You're a routine champion!";
            icon = R.drawable.ic_crown;
        } else {
            title = "🔥 " + streakCount + " Day Streak!";
            message = "Keep up the great work!";
            icon = R.drawable.ic_notification;
        }

        Intent intent = new Intent(context, ChildHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_STREAK,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STREAK)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 500, 300, 300, 300, 500});

        notificationManager.notify(NOTIFICATION_STREAK, builder.build());
    }

    /**
     * Show an achievement notification (for badge unlocks)
     */
    public static void showAchievementNotification(Context context, String badgeName, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, RewardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ACHIEVEMENT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENT)
                .setContentTitle("🏆 New Badge: " + badgeName)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_badge)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 300, 200, 300, 200, 500});

        notificationManager.notify(NOTIFICATION_ACHIEVEMENT, builder.build());
    }
}