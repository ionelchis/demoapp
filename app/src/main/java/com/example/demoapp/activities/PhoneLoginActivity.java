package com.example.demoapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class PhoneLoginActivity extends AppCompatActivity {
    //TODO fix this
    private Button sendVerificationCodeButton, verifyButton;
    private EditText phoneNumberInput, verificationCodeInput;
    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth auth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        auth = FirebaseAuth.getInstance();

        initializeFields();

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberInput.getText().toString();
                if (TextUtils.isEmpty(phoneNumber))
                    Toast.makeText(PhoneLoginActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                else {
                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("Please wait while we are authenticating your phone");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
//                    PhoneAuthOptions options =
//                            PhoneAuthOptions.newBuilder(auth)
//                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
//                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                                    .setActivity(PhoneLoginActivity.this)                 // Activity (for callback binding)
//                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
//                                    .build();
//                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneNumberInput.setVisibility(View.INVISIBLE);

                String verificationCode = verificationCodeInput.getText().toString();
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Enter verification code first", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Code verification");
                    loadingBar.setMessage("Please wait while we are verifying your code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number. Please enter a correct phone number", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                phoneNumberInput.setVisibility(View.VISIBLE);

                verificationCodeInput.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);

            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneNumberInput.setVisibility(View.INVISIBLE);

                verificationCodeInput.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congrats, you've signed in successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            String errorMessage = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void initializeFields() {
        sendVerificationCodeButton = findViewById(R.id.sendVerificationCodeButton);
        verifyButton = findViewById(R.id.verifyButton);
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        verificationCodeInput = findViewById(R.id.verificationCodeInput);
        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}