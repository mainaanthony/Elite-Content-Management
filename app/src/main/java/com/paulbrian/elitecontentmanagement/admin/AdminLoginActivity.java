package com.paulbrian.elitecontentmanagement.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.paulbrian.elitecontentmanagement.R;


public class AdminLoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

//        //TODO remove me am for testing
//        editTextEmail.setText("admin@gmail.com");
//        editTextPassword.setText("admin");

        buttonLogin.setOnClickListener(v -> login());

    }

    private void login() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter email first...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password first...", Toast.LENGTH_SHORT).show();
        }
        else {
            if (email.equalsIgnoreCase("admin@gmail.com") && password.equals("admin")){
                Intent intent = new Intent(getBaseContext(), AdminHomeActivity.class);
                startActivity(intent);
                Toast.makeText(getBaseContext(), "Logged In successfully", Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(getBaseContext(), "Wrong admin Credentials", Toast.LENGTH_LONG).show();

            }
        }
    }
}