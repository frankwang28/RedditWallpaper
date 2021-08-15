package com.fwang28.redditwallpaper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;

public class Alarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF , "RedditWallpaper:WifiLock");
        wifiLock.acquire();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RedditWallpaper:WakeLock");
        wakeLock.acquire();

        WallpaperSetter wallpaperSetter = new WallpaperSetter(context);
        wallpaperSetter.fullWallpaper();

        setAlarm(context);

        wifiLock.release();
        wakeLock.release();

    }

    public void setAlarm(Context context)
    {
        AlarmManager alarmManager =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("WallpaperAlarm");
        intent.setClass(context, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 12 * 60 * 60 * 1000,
                    pendingIntent);
            System.out.println("Exact and Allow Idle Alarm Set");
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 12 * 60 * 60 * 1000,
                    pendingIntent);
            System.out.println("Exact Alarm Set");
        }
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        System.out.println("Alarm Removed");
    }
}
