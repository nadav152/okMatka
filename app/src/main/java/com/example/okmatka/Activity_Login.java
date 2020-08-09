package com.example.okmatka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class Activity_Login extends AppCompatActivity {

    private Button Login_BTN_login,Login_BTN_register;
    private EditText Login_EDT_password, Login_EDT_enterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findView();

        Login_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Login_EDT_enterName.getText().toString().equals("alma") &&
                        Login_EDT_password.getText().toString().equals("lauer")) {
                    Toast.makeText(getApplicationContext(),
                            "Redirecting...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Login_BTN_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_register);
            }
        });
    }

    private void findView() {
        Login_BTN_login = findViewById(R.id.Login_BTN_login);
        Login_EDT_enterName = findViewById(R.id.Login_EDT_enterName);
        Login_EDT_password = findViewById(R.id.Login_EDT_password);
        Login_BTN_register = findViewById(R.id.Login_BTN_register);
    }
}
