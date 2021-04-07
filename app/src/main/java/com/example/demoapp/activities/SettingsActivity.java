package com.example.demoapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateProfileButton;
    private TextView usernameTextView;
    private EditText fullNameEditText, infoEditText, usernameEditText;
    private CircleImageView userProfileImage;


    private String currentUserID;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    private static final int galleryPick = 1;
    private StorageReference usersProfileImagesRef;
    private ProgressDialog loadingBar;
    private Toolbar profileSettingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeFields();
        usernameTextView.setVisibility(View.INVISIBLE);
        usernameEditText.setVisibility(View.INVISIBLE);
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        retrieveUserProfileInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO fix cropper
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set profile picture");
                loadingBar.setMessage("Your profile picture is uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = usersProfileImagesRef.child(currentUserID + ".jpg");
//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(SettingsActivity.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
//                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
//                            System.out.println(downloadUrl);
//                            rootRef.child("Users").child(currentUserID).child("profile_image")
//                                    .setValue(downloadUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (!task.isSuccessful()) {
//                                                String errorMessage = task.getException().toString();
//                                                Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
//                                            }
//                                            loadingBar.dismiss();
//                                        }
//                                    });
//                        } else {
//                            String errorMessage = task.getException().toString();
//                            Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
//                            loadingBar.dismiss();
//                        }
//                    }
//                });
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                rootRef.child("Users").child(currentUserID).child("profile_image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingsActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                                }
                                                loadingBar.dismiss();
                                            }
                                        });
                            }
                        });
                    }
                });
            }

        }
    }

    private void updateProfile() {
        String fullName = fullNameEditText.getText().toString();
        String info = infoEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        if (TextUtils.isEmpty(fullName))
            Toast.makeText(this, "Enter your full name first", Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(username))
            Toast.makeText(this, "Enter your username first", Toast.LENGTH_SHORT).show();
        else {
            Map<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("username", username);
            profileMap.put("full_name", fullName);
            profileMap.put("info", info);

            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                sendUserToMainActivity();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void retrieveUserProfileInfo() {
        rootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("username")) {
                            String retrieveUsername = snapshot.child("username").getValue().toString();
                            String retrieveInfo = snapshot.child("info").getValue().toString();
                            String retrieveFullName = snapshot.child("full_name").getValue().toString();
                            if (snapshot.hasChild("profile_image")) {
                                String retrieveProfileImage = snapshot.child("profile_image").getValue().toString();
                                Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                            }
                            fullNameEditText.setText(retrieveFullName);
                            infoEditText.setText(retrieveInfo);
                            usernameTextView.setText(retrieveUsername);
                            usernameEditText.setText(retrieveUsername);
                            usernameTextView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(SettingsActivity.this, "Please set your profile", Toast.LENGTH_SHORT).show();
                            usernameEditText.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initializeFields() {
        profileSettingsToolbar = findViewById(R.id.profileSettingsToolbar);
        setSupportActionBar(profileSettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile Settings");
        updateProfileButton = findViewById(R.id.updateProfileButton);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        infoEditText = findViewById(R.id.infoEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        userProfileImage = findViewById(R.id.userProfileImage);
        usernameTextView = findViewById(R.id.usernameTextView);
        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}