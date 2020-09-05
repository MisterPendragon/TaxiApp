package com.example.taxiapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                sleep(4000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                startActivity(new Intent(SplashScreenActivity.this, Choose_Mode_Activity.class));
                    overridePendingTransition(R.anim.zoom_in,R.anim.static_anim);
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}