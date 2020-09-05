package com.example.taxiapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DriverSignIn extends AppCompatActivity {
private static final String TAG ="DriverSignIn";


    private EditText email;

    private EditText password;
    private  EditText confirmPassword;
    private Button create;
    private TextView logIn;
    private boolean isLoginModeActive;
    private TextView theTopName;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_sign_in);
        auth=FirebaseAuth.getInstance();

        // Условие чтобы сразу войти в аккаунт
        if(auth.getCurrentUser()!= null) {
            startActivity(new Intent(DriverSignIn.this,DriverMapsActivity.class));
            overridePendingTransition(R.anim.zoom_in,R.anim.static_anim);
        }

        // НАХОДИМ ПО ID
        email=findViewById(R.id.editTextEmailAddress);

        password=findViewById(R.id.editTextPassword);
        confirmPassword=findViewById(R.id.editTextConfirmPassword);
        create=findViewById(R.id.createButton);
        logIn=findViewById(R.id.logInTextView);
        theTopName = findViewById(R.id.theName);

        auth=FirebaseAuth.getInstance();




    }

// МЕТОДЫ ДЛЯ СЧИТЫВАНИЯ ПОЛЕЙ ВВОДА
    private boolean validateEmail() {
        String emailInput = email.getText().toString().trim();
        if(emailInput.isEmpty()){
            email.setError("Please input your Email");
            return  false;
        } else return true;
    }

    private boolean validatePassword() {
        String passwordInput = password.getText().toString().trim();
        if(passwordInput.isEmpty()){
            password.setError("Please input your password");
            return  false;
        } else if(passwordInput.length()<6) {
            password.setError("Your password less than 6");
            return false;
        } else return true;

    }
    private boolean validateConfirmPassword() {
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = confirmPassword.getText().toString().trim();
        if(!passwordInput.equals(confirmPasswordInput)){
            confirmPassword.setError("Passwords must match");
            return false;
        }

        else return true;
    }

// МЕТОДЫ ДЛЯ СОЗДАНИЯ ИЛИ ЗАЛОГИНИВАНИЯ
    public void createNewAccount(View view) {
        if (!validateEmail()|!validatePassword()){
            return; // Если валидация не прошла то указываем на ошибки!
        }

        if(isLoginModeActive){
            codeFromFireBase2(); // метод для входа в аккаунт
            startActivity(new Intent(DriverSignIn.this,DriverMapsActivity.class)); // Переход в гугл карты
            overridePendingTransition(R.anim.zoom_in,R.anim.static_anim);
        } else if (!validateEmail()|!validatePassword()|!validateConfirmPassword()){
                return; // Если валидация не прошла то указываем на ошибки!
        } else codeFromFireBase1(); // метод для создания юзера в FireBase
        startActivity(new Intent(DriverSignIn.this,DriverMapsActivity.class)); // Переход в гугл карты
        overridePendingTransition(R.anim.zoom_in,R.anim.static_anim);

    }

    public void returnToLogin(View view) {
        if (isLoginModeActive) {
            isLoginModeActive =false;
            theTopName.setText("Create Account");
            logIn.setText("Log in");
            create.setText("Create");
            confirmPassword.setVisibility(View.VISIBLE);
        } else {
            theTopName.setText("Log in");
            isLoginModeActive =true;
            logIn.setText("Or Create");
            create.setText("Log in");

            confirmPassword.setVisibility(View.GONE);
        }


    }
                                              // ДРУГИЕ МЕТОДЫ
public void codeFromFireBase1 (){
    auth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = auth.getCurrentUser();
                        //updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(DriverSignIn.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        //updateUI(null);
                    }

                    // ...
                }
            });
}

public void codeFromFireBase2(){
    auth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = auth.getCurrentUser();
                        //updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(DriverSignIn.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        //updateUI(null);
                        // ...
                    }

                    // ...
                }
            });
}

}