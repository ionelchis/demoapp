package com.example.demoapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import com.example.demoapp.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar findFriendsToolbar;
    private RecyclerView findFriendsRecyclerView;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeFields();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(usersRef, User.class)
                .build();

        FirebaseRecyclerAdapter<User, FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<User, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull User model) {
                holder.usernameTextView.setText(model.getUsername());
                holder.fullNameTextView.setText(model.getFull_name());
                holder.infoTextView.setText(model.getInfo());
                Picasso.get().load(model.getProfile_image()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedUserId = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("selectedUserId", selectedUserId);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new FindFriendsViewHolder(view);
            }
        };

        findFriendsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView, fullNameTextView, infoTextView;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTextView = itemView.findViewById(R.id.usernameFriendsTextView);
            fullNameTextView = itemView.findViewById(R.id.fullNameFriendsTextView);
            infoTextView = itemView.findViewById(R.id.infoFriendsTextView);
            profileImage = itemView.findViewById(R.id.usersProfileImage);

        }
    }

    private void initializeFields() {
        findFriendsToolbar = findViewById(R.id.findFriendsToolbar);
        setSupportActionBar(findFriendsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


        findFriendsRecyclerView = findViewById(R.id.findFriendsRecyclerView);
        findFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}