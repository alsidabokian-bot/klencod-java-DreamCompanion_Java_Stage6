package com.example.dreamcompanion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.app.Notification;
import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "dream_reminders";

    @Override public void onReceive(Context context, Intent intent) {
        android.content.SharedPreferences p = context.getSharedPreferences("dream_settings", Context.MODE_PRIVATE);
        if (!p.getBoolean("enabled", false)) return;
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int quietStart = p.getInt("quiet_start", 22);
        int quietEnd = p.getInt("quiet_end", 7);
        boolean quiet = quietStart > quietEnd
                ? (hour >= quietStart || hour < quietEnd)
                : (hour >= quietStart && hour < quietEnd);

        if (!quiet) {
            createChannel(context);
            Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? new Notification.Builder(context, CHANNEL)
                    : new Notification.Builder(context);
            Notification notification = builder
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("رفيق الأحلام الواعية 🌙")
                    .setContentText("هل توقفت اليوم لتتأكد أنك مستيقظ؟ خذ لحظة للانتباه إلى ما حولك.")
                    .setAutoCancel(true)
                    .build();
            try {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) manager.notify(4102, notification);
            } catch (SecurityException ignored) { }
        }
        ReminderScheduler.schedule(context);
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "تذكيرات الأحلام", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("تذكيرات لطيفة بفحوصات الواقع");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
