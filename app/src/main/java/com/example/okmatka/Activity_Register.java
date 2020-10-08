package com.example.okmatka;


import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;


public class Activity_Register extends AppCompatActivity {


    private MaterialButton register_BTN_register;
    private TextInputEditText register_EDT_Repassword, register_EDT_password,
            register_EDT_email, register_EDT_name;
    private User currentUser;
    private ImageView register_IMG_appLogo;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViews();
        setFireBase();
        glide(R.drawable.img_mainlogo_jpg,register_IMG_appLogo);
        register_BTN_register.setOnClickListener(registerButton);
    }

    private void setFireBase() {
        firebaseAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance()
                .getReference("USERS_LIST");

    }

    private View.OnClickListener registerButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MySignal.getInstance().addSound(R.raw.click);
            if (isValidUser())
                saveUser(currentUser.getName(),currentUser.getEmail(),currentUser.getPassword());
            else
                MySignal.getInstance().showToast("Please fix it");
        }
    };

    private boolean isValidUser() {
        if (checkEmail(register_EDT_email.getText().toString()) && checkPassword()) {
            currentUser = new User(String.valueOf(register_EDT_email.getText()),
                    String.valueOf(register_EDT_password.getText()), String.valueOf(register_EDT_name.getText()));
            return true;
        }
        return false;
    }

    private void saveUser(final String name, String email, final String password) {
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(saveUserCompleteListener());
    }

    private OnCompleteListener<AuthResult> saveUserCompleteListener() {
        return new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Registration
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    assert firebaseUser != null;
                    String userId = firebaseUser.getUid();
                    FirebaseAuth.getInstance().signOut();

                    //Open Activity Login after saving the User
                    currentUser.setId(userId);
                    myRef.child(userId).setValue(currentUser).addOnCompleteListener(setValueCompleteListener());
                }else {
                    //todo check if the intenet is the problem
                    MySignal.getInstance().showToast("Task was not completed\n user was not saved");
                }
            }
        };
    }

    private OnCompleteListener<Void> setValueCompleteListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    MySignal.getInstance().showToast("User has been registered!\nPlease Login");
                    moveToLogin();
                }
            }
        };
    }

    private void moveToLogin() {
        finish();
    }


    private boolean checkPassword() {
        String pass = String.valueOf(register_EDT_password.getText());
        String rePass = String.valueOf(register_EDT_Repassword.getText());
        if (pass.length() < 6) {
            MySignal.getInstance().showToast("Password must be at least 6 digits");
            return false;
        } else if (!(pass.equals(rePass))) {
            MySignal.getInstance().showToast("Password is not equal");
            return false;
        }
        return true;
    }

    private void glide(int img, ImageView into) {
        Glide.with(this).load(img).into(into);
    }

    private void findViews() {
        register_BTN_register = findViewById(R.id.register_BTN_register);
        register_EDT_Repassword = findViewById(R.id.register_EDT_Repassword);
        register_EDT_password = findViewById(R.id.register_EDT_password);
        register_EDT_email = findViewById(R.id.register_EDT_email);
        register_EDT_name = findViewById(R.id.register_EDT_name);
        register_IMG_appLogo = findViewById(R.id.register_IMG_appLogo);

    }
    // todo change to roll select to spinner button  -> https://www.youtube.com/watch?v=on_OrrX7Nw4

    private boolean isValidEmail(String email) {

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();

    }

    private boolean checkEmail(String email) {
        if (!(isValidEmail(email))) {
            MySignal.getInstance().showToast("Not a valid e-mail\nTry Again");
            return false;
        }
        return true;
    }
}

