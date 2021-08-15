package com.fwang28.redditwallpaper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;


public class ForegroundService extends Service {

    public static SharedPreferences sharedPreferences;
    public static final String preference = "pref";

    Timer timer = new Timer();
    int hour;
    int interval = 1000 * 60 * 60;

    private static final String TAG = "RedditWallpaper:wakelock";
    private PowerManager.WakeLock mWakeLock = null;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    @Override
    public void onCreate() {
        Context context = getApplicationContext();
        sharedPreferences = context.getSharedPreferences(preference, context.MODE_PRIVATE);
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        hour = sharedPreferences.getInt("hour", 12);
        final Notification notification = notification_builder("Wallpaper to be set in " + Integer.toString(hour) + " hours.");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (hour == 1) {
                    hour = 12;
                    WallpaperSetter wallpaperSetter = new WallpaperSetter(getApplicationContext());
                    wallpaperSetter.fullWallpaper();
                } else{
                    hour -= 1;
                }

                Context context = getApplicationContext();
                sharedPreferences = context.getSharedPreferences(preference, context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("hour", hour);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    editor.apply();
                }
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification1 = notification_builder("Wallpaper to be set in " + Integer.toString(hour) + " hours.");
                mNotificationManager.notify(1, notification1);

            }}, interval, interval);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            startForeground(1, notification);
        }

        return START_STICKY;
    }

    public Notification notification_builder(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Reddit Wallpaper")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }

    @Override
    public void onDestroy() {
        if (mWakeLock.isHeld())
            mWakeLock.release();
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
