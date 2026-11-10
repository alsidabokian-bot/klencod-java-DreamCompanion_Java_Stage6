package com.example.dreamcompanion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public final class ReminderScheduler {
    private ReminderScheduler() {}

    public static void schedule(Context context) {
        android.content.SharedPreferences p = context.getSharedPreferences("dream_settings", Context.MODE_PRIVATE);
        if (!p.getBoolean("enabled", false)) return;
        int hour = p.getInt("hour", 10);
        int minute = p.getInt("minute", 0);
        Calendar when = Calendar.getInstance();
        when.set(Calendar.HOUR_OF_DAY, hour);
        when.set(Calendar.MINUTE, minute);
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MILLISECOND, 0);
        if (when.getTimeInMillis() <= System.currentTimeMillis()) when.add(Calendar.DAY_OF_YEAR, 1);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 4102,
                new Intent(context, ReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
            } else {
                manager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
            }
        }
    }

    public static void cancel(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 4102,
                new Intent(context, ReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (manager != null) manager.cancel(pi);
    }
}
