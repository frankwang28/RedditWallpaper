package com.fwang28.redditwallpaper;

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
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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

public class WallpaperSetter {

    private Context mContext;
    public static final String preference = "pref";
    SharedPreferences sharedPreferences;
    private int screenHeight;
    private int screenWidth;
    private LinkedList<String> prevURLs;


    public WallpaperSetter(Context context) {
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(preference, Context.MODE_PRIVATE);
        String prevURLsString = sharedPreferences.getString("prevURLsString", "");
        String[] prevURLsArray = prevURLsString.split(",");
        List<String> prevURLsList = Arrays.asList(prevURLsArray);
        prevURLs = new LinkedList<String>(prevURLsList);

        // dimensions of device
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private String downloadFromReddit() throws SAXException, IOException, ParserConfigurationException {

        boolean forceHD = sharedPreferences.getBoolean("boolHD", false);
        boolean forcePortrait = sharedPreferences.getBoolean("boolPortrait", false);

        if (prevURLs.size() > 14){
            prevURLs.removeFirst();
        }

        String download_path = mContext.getFilesDir().getPath();
        String subreddit =  sharedPreferences.getString("subreddit", "subreddit");
        RedditImageGrabber redditImageGrabber = new RedditImageGrabber(
                subreddit, download_path,
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

    public void setRedditWallpaper(Bitmap bitmap) {

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

        Glide
                .with(mContext)
                .asBitmap()
                .load(bitmap)
                .apply(new RequestOptions().override((int)newWidth, (int)newHeight))
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        WallpaperManager manager = WallpaperManager.getInstance(mContext);
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

    public Bitmap changeBitmapFilter(Bitmap bmp){

        int brightness = sharedPreferences.getInt("brightness", 25);
        int contrast = sharedPreferences.getInt("contrast", 25);
        int saturation = sharedPreferences.getInt("saturation", 25);

        brightness = brightness - 25;
        contrast = contrast - 25;
        saturation = saturation - 25;

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();

        ColorFilterGenerator colorFilterGenerator = new ColorFilterGenerator();
        ColorFilter cmf = colorFilterGenerator.adjustColor(brightness, contrast, saturation);

        paint.setColorFilter(cmf);
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    void showNotification(String title, String message) {

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, "YOUR_CHANNEL_ID")
                .setSmallIcon(R.mipmap.baseline_collections_white_48) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // clear notification after click
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void fullWallpaper(){

        boolean connected = isNetworkConnected();
        if (!connected){
            showNotification("RedditWallpaper", "No wifi connection.");
            Toast.makeText(mContext, "No wifi connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    String dlPath = downloadFromReddit();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("bgFile", dlPath);
                    editor.apply();

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

    public void resetWallpaper(){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    String dlPath = sharedPreferences.getString("bgFile", "");

                    if (dlPath == ""){
                        return;
                    }

                    Bitmap bitmap;
                    File Image = new File(dlPath);

                    bitmap = BitmapFactory.decodeFile(Image.getAbsolutePath());
                    Bitmap bitmap_filtered = changeBitmapFilter(bitmap);
                    setRedditWallpaper(bitmap_filtered);

                    System.out.println("Reset Background!");

                    showNotification("RedditWallpaper", "Wallpaper reset!");

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }
        });

        thread.start();
    }
}
