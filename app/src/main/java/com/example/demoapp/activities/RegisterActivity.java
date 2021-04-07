package com.example.demoapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.service.MyFirebaseMessagingService;
import com.example.demoapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private Button createAccountButton;
    private EditText passwordEditText, reEnterPasswordEditText, emailEditText;
    private TextView loginLink;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String reEnterPassword = reEnterPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(email))
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
        if (!password.equals(reEnterPassword))
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
        else {
            progressDialog.setTitle("Creating new account");
            progressDialog.setMessage("Please wait, account is creating");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String deviceToken = MyFirebaseMessagingService.getToken(RegisterActivity.this);
                                String currentUserID = auth.getCurrentUser().getUid();
                                rootRef.child("Users").child(currentUserID).setValue("");

                                rootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);

                                Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
//                                sendUserToLoginActivity();
                                sendUserToMainActivity();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    private void initializeFields() {
        createAccountButton = findViewById(R.id.createAccountButton);
        passwordEditText = findViewById(R.id.passwordEditText);
        reEnterPasswordEditText = findViewById(R.id.reEnterPasswordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        loginLink = findViewById(R.id.loginLink);

        progressDialog = new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}