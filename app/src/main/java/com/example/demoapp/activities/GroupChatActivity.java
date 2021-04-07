package com.example.demoapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.demoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageEditText;
    private ScrollView scrollView;
    private TextView displayTextMessages;

    private String currentGroupName, currentUserID, currentUsername, currentDate, currentTime;
    private String[] members;

    private FirebaseAuth auth;
    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        currentGroupName = getIntent().getExtras().get("groupName").toString();

        members = (String[]) getIntent().getExtras().get("members");
        for (String x : members) {
            System.out.println(x);
        }

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

//        initializeFields();
        
////        getUserInfo();
//
//        sendMessageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendMessage();
//                userMessageEditText.setText("");
//                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

//        groupNameRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                if (snapshot.exists()) {
//                    displayMessages(snapshot);
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                if (snapshot.exists()) {
//                    displayMessages(snapshot);
//                }
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }
/*
    private void displayMessages(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatUsername = (String) ((DataSnapshot) iterator.next()).getValue();

            displayTextMessages.append(chatUsername + ":\n" + chatMessage + "\n" + chatTime + ", " + chatDate + "\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void sendMessage() {
        String userMessage = userMessageEditText.getText().toString();
        String messageKey = groupNameRef.push().getKey();
        if (!TextUtils.isEmpty(userMessage)) {
            Calendar calendarDate =  Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime =  Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            Map<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(messageKey);

            Map<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("username", currentUsername);
            messageInfoMap.put("message", userMessage);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void getUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUsername = snapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeFields() {
        toolbar = findViewById(R.id.groupChatBarLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        userMessageEditText = findViewById(R.id.userMessageEditText);
        scrollView = findViewById(R.id.scrollView);
        displayTextMessages = findViewById(R.id.groupChatTextDisplay);
    }*/
}