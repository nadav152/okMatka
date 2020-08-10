package com.example.okmatka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class Activity_Register extends AppCompatActivity {


    private Button register_BTN_register;
    private EditText register_EDT_Repassword, register_EDT_password,
            register_EDT_email, register_EDT_enterName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findView();
        register_BTN_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent loginIntent = new Intent(Activity_Login.this, Activity_Register.class);
//                startActivity(loginIntent);
//                finish();
                checkRegister();
            }
        });
    }

    private void checkRegister() {
        if (checkName() && checkEmail() && checkPassword()) {
            Toast.makeText(getApplicationContext(), "User has been registered!", Toast.LENGTH_SHORT).show();
            //saveUser();
            //TODO add and function that saves the new user !
        } else {
            Toast.makeText(getApplicationContext(), "Please fix it", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkName() {
        if (!(register_EDT_enterName.length() > 0)) {
            Toast.makeText(getApplicationContext(), "Not a valid name", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkEmail() {
        if (!(isValidEmail(register_EDT_email.getText().toString()))) {
            Toast.makeText(getApplicationContext(), "Not a valid e-mail", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkPassword() {
        String pass = String.valueOf(register_EDT_password.getText());
        String rePass = String.valueOf(register_EDT_Repassword.getText());
        if (pass.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password must be at least 6 digits", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!(pass.equals(rePass))) {
            Toast.makeText(getApplicationContext(), "Password is not equal", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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

