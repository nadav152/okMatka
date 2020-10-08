package com.example.okmatka;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Activity_Login extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private MaterialButton Login_BTN_login, Login_BTN_signup;
    private TextInputEditText Login_EDT_password, Login_EDT_email;
    private ImageView Login_IMG_appLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findView();
        setFireBase();
        setListeners();
        glide(R.drawable.img_mainlogo_jpg, Login_IMG_appLogo);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfUserAlreadyLoggedOn();
    }

    private void checkIfUserAlreadyLoggedOn() {
        //checking if a user is already logged in
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null) {
            Intent intent = new Intent(Activity_Login.this, Activity_Main.class);
            startActivity(intent);
            finish();
        }
    }

    private void setFireBase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    private View.OnClickListener signUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MySignal.getInstance().addSound(R.raw.click);
            Intent loginIntent = new Intent(Activity_Login.this, Activity_Register.class);
            startActivity(loginIntent);
        }
    };
    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MySignal.getInstance().addSound(R.raw.click);
            String email = String.valueOf(Login_EDT_email.getText());
            String password = String.valueOf(Login_EDT_password.getText());
            checkDetails(email, password);
        }
    };

    private void checkDetails(final String email, final String password) {
        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(completeLoginListener());
        else
            MySignal.getInstance().showToast("Please Fill Up All Details");
    }

    private OnCompleteListener<AuthResult> completeLoginListener() {
        return new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    moveToActivityProfiles();
                }else
                    //todo check if the intenet is the problem
                    MySignal.getInstance().showToast("Wrong Credentials");
            }
        };
    }


    private void moveToActivityProfiles() {
        Intent loginIntent = new Intent(Activity_Login.this, Activity_Main.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void setListeners() {
        Login_BTN_login.setOnClickListener(loginListener);
        Login_BTN_signup.setOnClickListener(signUpListener);
    }

    private void glide(int img, ImageView into) {
        Glide.with(this).load(img).into(into);
    }

    private void findView() {
        Login_BTN_login = findViewById(R.id.Login_BTN_login);
        Login_EDT_email = findViewById(R.id.Login_EDT_email);
        Login_EDT_password = findViewById(R.id.Login_EDT_password);
        Login_BTN_signup = findViewById(R.id.Login_BTN_signup);
        Login_IMG_appLogo = findViewById(R.id.Login_IMG_appLogo);
    }
}
