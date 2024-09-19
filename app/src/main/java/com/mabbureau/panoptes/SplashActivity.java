package com.mabbureau.panoptes;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize the ImageView for the logo
        ImageView logo = findViewById(R.id.logoImage);

        // Load the pulsating animation
        Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulsate);
        logo.startAnimation(pulseAnimation); // Start the animation on the logo

        // Set delay for 3 seconds before transitioning to MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start your Main Activity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // close the splash screen
            }
        }, 4000); // 4000ms = 4 seconds
    }
}
