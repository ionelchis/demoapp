package com.example.demoapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.demoapp.R;
import com.example.demoapp.model.CurrentState;
import com.example.demoapp.model.FriendshipRequestType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID; //receiver - visited profile user; sender - current user
    private CurrentState currentState;

    private CircleImageView userProfileImage;
    private TextView fullNameTextView, infoTextView;
    private Button sendFriendshipRequestButton, sendMessageRequestButton, declineFriendshipRequestButton;

    private DatabaseReference usersRef, friendshipRequestsRef, friendsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendshipRequestsRef = FirebaseDatabase.getInstance().getReference().child("Friendship Requests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
//        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("selectedUserId").toString();
        senderUserID = auth.getCurrentUser().getUid();

        initializeFields();
        retrieveUserInformation();
    }

    private void retrieveUserInformation() {
        usersRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("profile_image")) {
                        String userImage = snapshot.child("profile_image").getValue().toString();
                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    }
                    String fullName = snapshot.child("full_name").getValue().toString();
                    String info = snapshot.child("info").getValue().toString();

                    fullNameTextView.setText(fullName);
                    infoTextView.setText(info);

                    manageFriendshipRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void manageFriendshipRequests() {

        friendshipRequestsRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserID)) {
                            FriendshipRequestType requestType = FriendshipRequestType.valueOf(snapshot.child(receiverUserID).child("request_type").getValue().toString());
                            if (requestType.equals(FriendshipRequestType.SENT)) {
                                currentState = CurrentState.REQUEST_SENT;
                                sendFriendshipRequestButton.setText(R.string.cancel_friendship_request);
                            } else if (requestType == FriendshipRequestType.RECEIVED) {
                                currentState = CurrentState.REQUEST_RECEIVED;
                                sendFriendshipRequestButton.setText(R.string.accept_friendship_request);
                                declineFriendshipRequestButton.setVisibility(View.VISIBLE);
                                declineFriendshipRequestButton.setEnabled(true);

                                declineFriendshipRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                         cancelFriendshipRequest();
                                    }
                                });
                            }
                        } else {
                            friendsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(receiverUserID)) {
                                                currentState = CurrentState.FRIENDS;
                                                sendFriendshipRequestButton.setText(R.string.remove_friend);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderUserID.equals(receiverUserID)) {
            sendFriendshipRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendshipRequestButton.setEnabled(false);
                    if (currentState == CurrentState.NEW) {
                        sendFriendshipRequest();
                    }
                    if (currentState == CurrentState.REQUEST_SENT) {
                        cancelFriendshipRequest();
                    }
                    if (currentState == CurrentState.REQUEST_RECEIVED) {
                        acceptFriendRequest();
                    }
                    if (currentState == CurrentState.FRIENDS) {
                        removeFriend();
                    }

                }
            });
        } else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
            sendFriendshipRequestButton.setVisibility(View.INVISIBLE );
        }
    }

    private void removeFriend() {
        friendsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendshipRequestButton.setEnabled(false);
                                                currentState = CurrentState.NEW;
                                                sendFriendshipRequestButton.setText(R.string.send_friendship_request);
                                                sendFriendshipRequestButton.setEnabled(true);

                                                declineFriendshipRequestButton.setEnabled(false);
                                                declineFriendshipRequestButton.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        friendsRef.child(senderUserID).child(receiverUserID)
                .child("Friends").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(receiverUserID).child(senderUserID)
                                    .child("Friends").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                friendshipRequestsRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    friendshipRequestsRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendFriendshipRequestButton.setEnabled(true);
                                                                                    currentState = CurrentState.FRIENDS;
                                                                                    sendFriendshipRequestButton.setText(R.string.remove_friend);
                                                                                    declineFriendshipRequestButton.setEnabled(false);
                                                                                    declineFriendshipRequestButton.setVisibility(View.INVISIBLE);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendshipRequest() {
        friendshipRequestsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                                friendshipRequestsRef.child(receiverUserID).child(senderUserID)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    sendFriendshipRequestButton.setEnabled(false);
                                                    currentState = CurrentState.NEW;
                                                    sendFriendshipRequestButton.setText(R.string.send_friendship_request);
                                                    sendFriendshipRequestButton.setEnabled(true);

                                                    declineFriendshipRequestButton.setEnabled(false);
                                                    declineFriendshipRequestButton.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });
                        }
                    }
                });
    }

    private void sendFriendshipRequest() {
        friendshipRequestsRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue(FriendshipRequestType.SENT)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendshipRequestsRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue(FriendshipRequestType.RECEIVED)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //TODO - notifications
//                                                Map<String, String> notificationMap = new HashMap<>();
//                                                notificationMap.put("from", senderUserID);
//                                                notificationMap.put("type", NotificationType.REQUEST.toString());
//                                                notificationsRef.child(receiverUserID).push()
//                                                        .setValue(notificationMap)
//                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                if (task.isSuccessful()) {
//                                                                    sendFriendshipRequestButton.setEnabled(true);
//                                                                    currentState = CurrentState.REQUEST_SENT;
//                                                                    sendFriendshipRequestButton.setText(R.string.cancel_friendship_request);
//                                                                }
//                                                            }
//                                                        });
                                                sendFriendshipRequestButton.setEnabled(true);
                                                currentState = CurrentState.REQUEST_SENT;
                                                sendFriendshipRequestButton.setText(R.string.cancel_friendship_request);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void initializeFields() {
        userProfileImage = findViewById(R.id.userProfilePicture);
        fullNameTextView = findViewById(R.id.userProfileFullNameTextView);
        infoTextView = findViewById(R.id.userProfileInfoTextView);
        sendFriendshipRequestButton = findViewById(R.id.sendFriendshipRequestButton);
        sendMessageRequestButton = findViewById(R.id.sendMessageRequestButton);
        currentState = CurrentState.NEW;
        declineFriendshipRequestButton = findViewById(R.id.declineFriendshipRequestButton);
    }
}