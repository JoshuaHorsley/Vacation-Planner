package com.example.assignment1.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.assignment1.R;
import com.example.assignment1.activities.TripDetailsActivity;

public class TripReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "trip_reminder_channel";


    @Override
    public void onReceive(Context context, Intent intent) {
        String tripName = intent.getStringExtra("tripName");
        Log.d("TripReminderReceiver", "Received broadcast for trip: " + tripName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Trip Reminder";
            String description = "Channel for trip reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent activityIntent = new Intent(context, TripDetailsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_airplane_ticket_24)
                .setContentTitle("Trip Reminder")
                .setContentText("Your trip \"" + tripName + "\" is coming up!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(tripName.hashCode(), builder.build());
        } else {
            Log.w("TripReminderReceiver", "Notification permission not granted");
        }
    }

}
