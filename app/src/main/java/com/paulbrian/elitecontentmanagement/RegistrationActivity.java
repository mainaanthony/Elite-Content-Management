package com.paulbrian.elitecontentmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paulbrian.elitecontentmanagement.user.HomeActivity;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {
    private EditText editTextFirstName, editTextLastName, editTextMobile, editTextEmail, editTextPassword, editTextPasswordConfirm;
    private Button buttonLogin, buttonSignUp;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editLastName);
        editTextMobile = findViewById(R.id.editTextMobile);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonSignUp.setOnClickListener(v -> {
            signUp();
        });

  
    }

    private void signUp() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String name = editTextFirstName.getText().toString();
        String mobile = editTextMobile.getText().toString();
        String passwordConfirm = editTextPasswordConfirm.getText().toString();

        ProgressDialog progressDialog = new ProgressDialog(this);

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter email first...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password first...", Toast.LENGTH_SHORT).show();
        }else if (!password.equals(passwordConfirm)){
            Toast.makeText(getBaseContext(), "Passwords don't match", Toast.LENGTH_LONG).show();

        }
        else {
            progressDialog.setTitle("Creating Account");
            progressDialog.setMessage("Please wait, while we are checking the credentials.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        addUserToDatabase(email, name, mobile);

                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                }
            });

        }
    }

    private void addUserToDatabase(String email, String name, String mobile) {
        final String uid = firebaseAuth.getCurrentUser().getUid();
        HashMap hashMap = new HashMap();
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("approve","not approved");
        hashMap.put("mobile",mobile);

        ProgressDialog progressDialog = new ProgressDialog(this);

        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("Adding User to database...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.child(uid).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                    startActivity(intent);
                    Toast.makeText(getBaseContext(), "Account created successfully", Toast.LENGTH_LONG).show();

                }else{
                    String message = task.getException().getMessage();
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }

}