package com.example.redditwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class StartReceiver extends BroadcastReceiver {
    public static SharedPreferences sharedPreferences;
    public static final String preference = "pref";

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        boolean foregroundStarted = sharedPreferences.getBoolean("boolAuto", false);

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && foregroundStarted){
            Intent foregroundIntent = new Intent(context, ForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(foregroundIntent);
                return;
            }
            context.startService(foregroundIntent);
        }
    }
}
