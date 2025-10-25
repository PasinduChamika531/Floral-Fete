package com.example.floralfete;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

public class FlingAnimationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_fling_animation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.theme_color));

        }

        ImageView imageLeft = findViewById(R.id.imageLeft);
        ImageView imageRight = findViewById(R.id.imageRight);


        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidth = displayMetrics.widthPixels;

        FlingAnimation flingLeft = new FlingAnimation(imageLeft, DynamicAnimation.TRANSLATION_X)
                .setStartVelocity(screenWidth*2)
                .setFriction(1f);


        FlingAnimation flingRight = new FlingAnimation(imageRight, DynamicAnimation.TRANSLATION_X)
                .setStartVelocity(-screenWidth*2)
                .setFriction(1f);

        flingLeft.start();
        flingRight.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(FlingAnimationActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();

            }
        }, 3000);
    }
}
