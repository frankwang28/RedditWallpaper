package com.fwang28.redditwallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button backgroundButton;
    Button resetButton;
    Button enterButton;

    ImageButton backgroundInfo;
    ImageButton resetInfo;

    TextView subreddit;

    CheckBox checkBoxHD;
    CheckBox checkBoxPortrait;
    CheckBox checkBoxAuto;

    ImageButton HDInfo;
    ImageButton portraitInfo;
    ImageButton autoInfo;

    SeekBar brightnessSeekBar;
    SeekBar contrastSeekbar;
    SeekBar saturationSeekbar;

    public static SharedPreferences sharedPreferences;

    public static final String preference = "pref";

    private static boolean done_init;


    public static LinkedList<String> prevURLs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        done_init = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backgroundButton = findViewById(R.id.backgroundButton);
        resetButton = findViewById(R.id.resetButton);
        enterButton = findViewById(R.id.enterButton);
        subreddit = findViewById(R.id.subreddit);
        checkBoxHD = findViewById(R.id.checkBoxHD);
        checkBoxPortrait =  findViewById(R.id.checkBoxPortrait);
        checkBoxAuto = findViewById(R.id.checkBoxAuto);

        backgroundInfo = findViewById(R.id.infoButtonBackground);
        resetInfo = findViewById(R.id.infoButtonReset);
        HDInfo = findViewById(R.id.infoButtonHD);
        portraitInfo = findViewById(R.id.infoButtonPortrait);
        autoInfo = findViewById(R.id.infoButtonAuto);


        brightnessSeekBar = findViewById(R.id.seekBarBright);
        contrastSeekbar = findViewById(R.id.seekBarContrast);
        saturationSeekbar = findViewById(R.id.seekBarSat);

        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WallpaperSetter wallpaperSetter = new WallpaperSetter(getApplicationContext());
                wallpaperSetter.fullWallpaper();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WallpaperSetter wallpaperSetter = new WallpaperSetter(getApplicationContext());
                wallpaperSetter.resetWallpaper();
            }
        });

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveText();
            }
        });

        subreddit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {
                    System.out.println("Enter pressed");
                    saveText();
                }
                return false;
            }

        });

        checkBoxHD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean checked = checkBoxHD.isChecked();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("boolHD", checked);
                editor.apply();
            }
        });

        checkBoxPortrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean checked = checkBoxPortrait.isChecked();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("boolPortrait", checked);
                editor.apply();
            }
        });


        checkBoxAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (done_init){
                    boolean checked = checkBoxAuto.isChecked();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("boolAuto", checked);
                    editor.apply();
                    if (checked){
                        startAuto();
                    }
                    else {
                        stopAuto();
                    }
                }
                }

        });

        backgroundInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backgroundInfo.performLongClick();
            }
        });
        resetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetInfo.performLongClick();
            }
        });
        HDInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HDInfo.performLongClick();
            }
        });
        portraitInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                portraitInfo.performLongClick();
            }
        });
        autoInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoInfo.performLongClick();
            }
        });

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int brightness = brightnessSeekBar.getProgress();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("brightness", brightness);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        contrastSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int contrast = contrastSeekbar.getProgress();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("contrast", contrast);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        saturationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int saturation = saturationSeekbar.getProgress();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("saturation", saturation);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sharedPreferences = getSharedPreferences(preference, Context.MODE_PRIVATE);

        subreddit.setText(sharedPreferences.getString("subreddit", "subreddit"));
        checkBoxHD.setChecked(sharedPreferences.getBoolean("boolHD", false));
        checkBoxPortrait.setChecked(sharedPreferences.getBoolean("boolPortrait", false));
        checkBoxAuto.setChecked(sharedPreferences.getBoolean("boolAuto", false));

        brightnessSeekBar.setProgress(sharedPreferences.getInt("brightness", 25));
        contrastSeekbar.setProgress(sharedPreferences.getInt("contrast", 25));
        saturationSeekbar.setProgress(sharedPreferences.getInt("saturation", 25));

        String prevURLsString = sharedPreferences.getString("prevURLsString", "");
        String[] prevURLsArray = prevURLsString.split(",");
        List<String> prevURLsList = Arrays.asList(prevURLsArray);
        prevURLs = new LinkedList<String>(prevURLsList);

        done_init = true;

    }


    private void saveText() {
        String subR = subreddit.getText().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("subreddit", subR);
        editor.commit();
        Toast.makeText(getApplicationContext(), "Subreddit Updated!", Toast.LENGTH_SHORT).show();
    }


    public void startAuto() {
//        Intent serviceIntent = new Intent(this, ForegroundService.class);
//        ContextCompat.startForegroundService(this, serviceIntent);
//        Intent serviceIntent = new Intent("WallpaperService");
//        serviceIntent.setClass(this, AlarmService.class);
//        startService(serviceIntent);
        Alarm alarm = new Alarm();
        alarm.setAlarm(this);

    }
    public void stopAuto() {
//        Intent serviceIntent = new Intent(this, ForegroundService.class);
//        stopService(serviceIntent);
//        Intent serviceIntent = new Intent("WallpaperService");
//        serviceIntent.setClass(this, AlarmService.class);
//        stopService(serviceIntent);
        Alarm alarm = new Alarm();
        alarm.cancelAlarm(this);

    }

}
