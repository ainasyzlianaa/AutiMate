package com.example.autimate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class RoutineScheduler {

    /**
     * Schedule routine reminders at specific times
     */
    public static void scheduleRoutineReminders(Context context) {
        scheduleAlarm(context, 7, 10, "Morning routine reminder", 1);     // 7:00 AM
        scheduleAlarm(context, 12, 30, "Afternoon routine reminder", 2);  // 12:30 PM
        scheduleAlarm(context, 20, 02, "Evening routine reminder", 3);     // 8:00 PM (CHANGED from 18:00 to 20:00)
    }

    private static void scheduleAlarm(Context context, int hour, int minute, String label, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, RoutineNotificationReceiver.class);
        intent.putExtra("label", label);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule repeating alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    /**
     * Cancel all routine reminders
     */
    public static void cancelRoutineReminders(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (int i = 1; i <= 3; i++) {
            Intent intent = new Intent(context, RoutineNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        }
    }
}