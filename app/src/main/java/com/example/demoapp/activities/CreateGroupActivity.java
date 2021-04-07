package com.example.demoapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoapp.R;
import com.example.demoapp.fragments.FriendsFragment;
import com.example.demoapp.model.ChatType;
import com.example.demoapp.model.User;
import com.example.demoapp.model.UserRole;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText groupNameEditText;
    private RecyclerView friendsRecyclerList;
    private Button createGroupButton, cancelButton;

    private DatabaseReference friendsRef, usersRef, groupsRef, chatsRef;
    private FirebaseAuth auth;
    private String currentUserID;
    List<String> selectedMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Chats");

        initializeFields();

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMainActivity();
            }
        });
    }

    private void createGroup() {
        if (selectedMembers.size() == 0) {
            Toast.makeText(this, "Select at least one member", Toast.LENGTH_SHORT).show();
        } else {
            String groupName = groupNameEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(groupName)) {
                DatabaseReference groupKeyRef = groupsRef.push();
                String groupPushID = groupKeyRef.getKey();
                Map<String, Object> membersMap = new HashMap<>();
                Map<String, Object> chatBody = new HashMap<>();
                membersMap.put(groupPushID + "/" + currentUserID, UserRole.ADMIN);
                chatBody.put(currentUserID + "/" + groupPushID + "/type", ChatType.GROUP);

                for (String user : selectedMembers) {
                    membersMap.put(groupPushID + "/" + user, UserRole.MEMBER);
                    chatBody.put(user + "/" + groupPushID + "/type", ChatType.GROUP);
                }
                membersMap.put(groupPushID + "/name", groupName);
                membersMap.put(groupPushID + "/date_created", getCurrentDate());
                membersMap.put(groupPushID + "/time", getCurrentTime());
                groupsRef.updateChildren(membersMap);
                chatsRef.updateChildren(chatBody);
                sendUserToMainActivity();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(friendsRef, User.class)
                .build();
        FirebaseRecyclerAdapter<User, CreateGroupActivity.FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<User, CreateGroupActivity.FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CreateGroupActivity.FriendsViewHolder holder, int position, @NonNull User model) {
                String friendsIDs = getRef(position).getKey();
                final String[] profileImage = {"default_image"};
                usersRef.child(friendsIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("profile_image")) {
                            profileImage[0] = snapshot.child("profile_image").getValue().toString();
                            String profileImage = snapshot.child("profile_image").getValue().toString();
                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.profileImage) ;
                        }
                        String username = snapshot.child("username").getValue().toString();
                        String fullName = snapshot.child("full_name").getValue().toString();
                        String info = snapshot.child("info").getValue().toString();

                        holder.usernameTextView.setText(username);
                        holder.fullNameTextView.setText(fullName);
                        holder.infoTextView.setText(info);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!selectedMembers.contains(friendsIDs)) {
                            holder.itemView.setBackgroundColor(Color.GRAY);
                            selectedMembers.add(friendsIDs);
                        } else {
                            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                            selectedMembers.remove(friendsIDs);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public CreateGroupActivity.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new CreateGroupActivity.FriendsViewHolder(view);
            }
        };
        friendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    private void initializeFields() {
        toolbar = findViewById(R.id.createGroupToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create group");
        groupNameEditText = findViewById(R.id.groupNameEditText);
        createGroupButton = findViewById(R.id.createGroupButton);
        cancelButton = findViewById(R.id.cancelButton);

        friendsRecyclerList = findViewById(R.id.friendsRecyclerList);
        friendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
    }



    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView, fullNameTextView, infoTextView;
        CircleImageView profileImage;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTextView = itemView.findViewById(R.id.usernameFriendsTextView);
            fullNameTextView = itemView.findViewById(R.id.fullNameFriendsTextView);
            infoTextView = itemView.findViewById(R.id.infoFriendsTextView);
            profileImage = itemView.findViewById(R.id.usersProfileImage);
            itemView.findViewById(R.id.acceptRequestButton).setVisibility(View.GONE);
            itemView.findViewById(R.id.declineRequestButton).setVisibility(View.GONE);
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(CreateGroupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private String getCurrentTime() {
        Calendar calendarTime =  Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        return currentTimeFormat.format(calendarTime.getTime());
    }

    private String getCurrentDate() {
        Calendar calendarDate =  Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.US);
        return currentDateFormat.format(calendarDate.getTime());
    }
}