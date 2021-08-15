package com.fwang28.redditwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StartReceiver extends BroadcastReceiver {
    public static SharedPreferences sharedPreferences;
    public static final String preference = "pref";

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        boolean autoStarted = sharedPreferences.getBoolean("boolAuto", false);

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && autoStarted){
//            Intent foregroundIntent = new Intent(context, ForegroundService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(foregroundIntent);
//                return;
//            }
//            context.startService(foregroundIntent);
//            Intent serviceIntent = new Intent("WallpaperService");
//            serviceIntent.setClass(context, AlarmService.class);
//            context.startService(serviceIntent);

            Alarm alarm = new Alarm();
            alarm.setAlarm(context);

        }
    }
}
