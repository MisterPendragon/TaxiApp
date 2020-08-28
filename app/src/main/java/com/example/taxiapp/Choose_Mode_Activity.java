package com.example.taxiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Choose_Mode_Activity extends AppCompatActivity {
    ImageView passengerImageView;
    ImageView carImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);
        // Находим по Id
        passengerImageView=findViewById(R.id.passengerImageView);
        carImageView=findViewById(R.id.carImageView);


    }

    public void goToPassenger(View view) throws InterruptedException {
       Animation animation=AnimationUtils.loadAnimation(this,R.anim.click_animation);
       passengerImageView.startAnimation(animation);

        startActivity(new Intent(Choose_Mode_Activity.this,PassengerSignIn.class));
    }

    public void goToDriver(View view) {
        Animation animation=AnimationUtils.loadAnimation(this,R.anim.click_animation);
        carImageView.startAnimation(animation);


        startActivity(new Intent(Choose_Mode_Activity.this,DriverSignIn.class));
    }
}