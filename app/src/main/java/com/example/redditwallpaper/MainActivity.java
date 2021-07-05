package com.example.redditwallpaper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    Button backgroundButton;
    Button enterButton;
    TextView subreddit;
    CheckBox checkBoxHD;
    CheckBox checkBoxPortrait;
    CheckBox checkBoxAuto;
    SeekBar brightnessSeekBar;

    public static SharedPreferences sharedPreferences;

    public static final String preference = "pref";
    public static final String selectedSubreddit = "subreddit";

    private static int screenHeight;
    private static int screenWidth;

    private static Object context;
    private static Object view;

    private static boolean done_init;


    public static LinkedList<String> prevURLs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        done_init = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backgroundButton = findViewById(R.id.backgroundButton);
        enterButton = findViewById(R.id.enterButton);
        subreddit = findViewById(R.id.subreddit);
        checkBoxHD = findViewById(R.id.checkBoxHD);
        checkBoxPortrait =  findViewById(R.id.checkBoxPortrait);
        checkBoxAuto = findViewById(R.id.checkBoxAuto);

        brightnessSeekBar = findViewById(R.id.seekBarBright);

        context = getApplicationContext();
        view = getWindow().getDecorView().getRootView();

        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 fullWallpaper();
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
                        startService();
                        System.out.println("Started Service");
                    }
                    else {
                        stopService();
                        editor.putInt("hour", 12);
                        System.out.println("Removed Service");
                    }
                }
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

        sharedPreferences = getSharedPreferences(preference, Context.MODE_PRIVATE);

        subreddit.setText(sharedPreferences.getString(selectedSubreddit, "subreddit"));
        checkBoxHD.setChecked(sharedPreferences.getBoolean("boolHD", false));
        checkBoxPortrait.setChecked(sharedPreferences.getBoolean("boolPortrait", false));
        checkBoxAuto.setChecked(sharedPreferences.getBoolean("boolAuto", false));
        brightnessSeekBar.setProgress(sharedPreferences.getInt("brightness", 100));

        String prevURLsString = sharedPreferences.getString("prevURLsString", "");
        String[] prevURLsArray = prevURLsString.split(",");
        List<String> prevURLsList = Arrays.asList(prevURLsArray);
        prevURLs = new LinkedList<String>(prevURLsList);

        done_init = true;

    }

    public static Context getContext() {
        return (Context)context;
    }

    public static View getView() {
        return (View)view;
    }

    public static void setRedditWallpaper(Bitmap bitmap) {

        // dimensions of device
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        float scaledScreenHeight = screenHeight * (float)1.0;
        float scaledScreenWidth = screenWidth * (float)1.02;

        // gets raw image dimensions
        float imageHeight = bitmap.getHeight();
        float imageWidth = bitmap.getWidth();

        float heightRatio = (imageHeight / scaledScreenHeight);
        float widthRatio = (imageWidth / scaledScreenWidth);

        // new dimensions for scaled down image
        float newHeight;
        float newWidth;
        if (heightRatio < widthRatio){
            newHeight = scaledScreenHeight;
            newWidth = imageWidth / (heightRatio);
        } else{
            newWidth = scaledScreenWidth;
            newHeight = imageHeight / (widthRatio);
        }

//        System.out.println("screen width " + scaledScreenWidth + " screen height: " + scaledScreenHeight);
//        System.out.println("image width " + imageWidth + " image height: " + imageHeight);
//        System.out.println("new width " + newWidth + " new height: " + newHeight);

        // sets the image as background with correct dimenions

        final Context staticContext = getContext();

        Glide
                .with(staticContext)
                .asBitmap()
                .load(bitmap)
                .apply(new RequestOptions().override((int)newWidth, (int)newHeight))
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        WallpaperManager manager = WallpaperManager.getInstance(staticContext);
                        try{
                            if (Build.VERSION.SDK_INT >= 24) {
                                manager.setBitmap(resource, null, false, WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
                            }
                            manager.setBitmap(resource);
                        }
                        catch (IOException e){
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });



    }

    private void saveText() {
        String subR = subreddit.getText().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(selectedSubreddit, subR);
        editor.commit();
        Toast.makeText(getApplicationContext(), "Subreddit Updated!", Toast.LENGTH_SHORT).show();
    }

    private static String downloadFromReddit() throws SAXException, IOException, ParserConfigurationException {

        final Context staticContext = getContext();
        final View staticView = getView();

        CheckBox checkBoxHD = staticView.findViewById(R.id.checkBoxHD);
        CheckBox checkBoxPortrait = staticView.findViewById(R.id.checkBoxPortrait);

        boolean forceHD = checkBoxHD.isChecked();
        boolean forcePortrait = checkBoxPortrait.isChecked();

        if (prevURLs.size() > 14){
            prevURLs.removeFirst();
        }

        String download_path = staticContext.getFilesDir().getPath();
        TextView subreddit =  staticView.findViewById(R.id.subreddit);
        RedditImageGrabber redditImageGrabber = new RedditImageGrabber(
                subreddit.getText().toString(), download_path,
                forcePortrait, forceHD,
                screenHeight, screenWidth,
                prevURLs);
        String dl_path = redditImageGrabber.getImage();

        Object[] prevURLsArray = prevURLs.toArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prevURLsArray.length; i++) {
            sb.append(prevURLsArray[i]).append(",");
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("prevURLsString", sb.toString());
        editor.apply();

        return dl_path;
    }

    public static Bitmap changeBitmapFilter(Bitmap bmp){

        final View staticView = getView();

        SeekBar brightnessSeekBar = staticView.findViewById(R.id.seekBarBright);
        int brightness = brightnessSeekBar.getProgress();

        brightness = brightness - 50;

        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        1, 0, 0, 0, brightness,
                        0, 1, 0, 0, brightness,
                        0, 0, 1, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public static boolean isNetworkConnected(Context c) {
        WifiManager wifiMgr = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public static void fullWallpaper(){

        final Context staticContext = getContext();
        boolean connected = isNetworkConnected(staticContext);
        if (!connected){
            showNotification("RedditWallpaper", "No wifi connection.");
            Toast.makeText(staticContext, "No wifi connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    String dlPath = downloadFromReddit();

                    System.out.println("Finished downloading");

                    Bitmap bitmap;
                    File Image = new File(dlPath);

                    bitmap = BitmapFactory.decodeFile(Image.getAbsolutePath());
                    Bitmap bitmap_filtered = changeBitmapFilter(bitmap);
                    setRedditWallpaper(bitmap_filtered);

                    System.out.println("Set Background!");

                    showNotification("RedditWallpaper", "Wallpaper updated!");

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }
        });

        thread.start();

    }

    static void showNotification(String title, String message) {

        final Context staticContext = getContext();

        NotificationManager mNotificationManager =
                (NotificationManager) staticContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(staticContext, "YOUR_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(staticContext, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(staticContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

}
