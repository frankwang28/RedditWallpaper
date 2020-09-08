package com.example.redditwallpaper;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    TextView subreddit;
    SharedPreferences sharedPreferences;
    public static final String preference = "pref";
    public static final String selectedSubreddit = "subreddit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button backgroundButton = (Button) findViewById(R.id.backgroundButton);
        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWallpaper();
            }
        });

        Button enterButton = (Button) findViewById(R.id.enterButton);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveText();
            }
        });

        subreddit = (TextView) findViewById(R.id.subreddit);
        sharedPreferences = getSharedPreferences(preference, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(selectedSubreddit)) {
            subreddit.setText(sharedPreferences.getString(selectedSubreddit, ""));

    }}

    private void setWallpaper() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // dimensions of device
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

        //background image
        int background = R.drawable.background2;

        // gets raw image dimensions
        BitmapFactory.Options realBitmap = new BitmapFactory.Options();
        realBitmap.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), background, realBitmap);
        int imageHeight = realBitmap.outHeight;
        int imageWidth = realBitmap.outWidth;

        // new dimensions for scaled down image
        float newHeight;
        float newWidth;
        if ((imageHeight / screenHeight) < (imageWidth / screenWidth)){
            newHeight = screenHeight;
            newWidth = (float) imageWidth / ((float)imageHeight/(float)screenHeight);
        } else{
            newWidth = screenWidth;
            newHeight = (float)imageHeight / ((float)imageWidth / (float)screenWidth);
        }

        // sets the image as background with correct dimenions
        Glide
                .with(this)
                .asBitmap()
                .load(background)
                .override((int)newWidth, (int)newHeight)
                .override(screenWidth, screenHeight)
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
                        try{
                            manager.setBitmap(resource);
                        } catch (IOException e){
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
    }


}
