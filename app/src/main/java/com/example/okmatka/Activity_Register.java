package com.example.okmatka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class Activity_Register extends AppCompatActivity {


    private Button register_BTN_register;
    private EditText register_EDT_Repassword, register_EDT_password,
            register_EDT_email, register_EDT_enterName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(), "Something went Wrong", Toast.LENGTH_SHORT).show();
        findView();
        register_BTN_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_login);
                //checkRegister();
            }
        });
    }

    private void checkRegister() {
        if (register_EDT_enterName.length() > 0 &&
                register_EDT_Repassword.equals(register_EDT_password)) {
            if (isValidEmail(register_EDT_email.getText().toString())) {
                Toast.makeText(getApplicationContext(), "User has been registered!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Something went Wrong", Toast.LENGTH_SHORT).show();
                Log.d("pttt", "Something went Wrong");
            }
        }else{
            Toast.makeText(getApplicationContext(), "Something went Wrong", Toast.LENGTH_SHORT).show();
            Log.d("pttt", "Something went Wrong");
        }
    }

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

    private void findView() {
        register_BTN_register = findViewById(R.id.register_BTN_register);
        register_EDT_Repassword = findViewById(R.id.register_EDT_Repassword);
        register_EDT_password = findViewById(R.id.register_EDT_password);
        register_EDT_email = findViewById(R.id.register_EDT_email);
        register_EDT_enterName = findViewById(R.id.register_EDT_enterName);
    }
}
