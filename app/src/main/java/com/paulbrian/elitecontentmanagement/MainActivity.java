package com.paulbrian.elitecontentmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paulbrian.elitecontentmanagement.admin.AdminHomeActivity;
import com.paulbrian.elitecontentmanagement.admin.AdminLoginActivity;
import com.paulbrian.elitecontentmanagement.user.HomeActivity;

public class MainActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonSignUp, buttonAdmin;
    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonAdmin = findViewById(R.id.buttonToAdmin);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonLogin.setOnClickListener(v -> {
            login();
        });

        buttonSignUp.setOnClickListener(v -> {
            startActivity(new Intent(getBaseContext(), RegistrationActivity.class));
        });

        buttonAdmin.setOnClickListener(v -> {
            startActivity(new Intent(getBaseContext(), AdminLoginActivity.class));

        });

        if (firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(getBaseContext(), HomeActivity.class));
            finish();

        }


//        //TODO remove me am for testing
//        editTextEmail.setText("foo@gmail.com");
//        editTextPassword.setText("123456");
//        buttonLogin.performClick();



    }

    private void login() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        ProgressDialog progressDialog = new ProgressDialog(this);

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter email first...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password first...", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Login To App");
            progressDialog.setMessage("Please wait, while we are checking the credentials.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String currentUser = firebaseAuth.getCurrentUser().getUid();
                        Toast.makeText(getBaseContext(), "Logged In successfully", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                        startActivity(intent);
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(getBaseContext(), "ERROR: "+message, Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                }
            });


        }
    }


}