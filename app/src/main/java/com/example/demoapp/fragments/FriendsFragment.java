package com.example.demoapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoapp.R;
import com.example.demoapp.activities.ChatActivity;
import com.example.demoapp.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    private View friendsView;
    private RecyclerView friendsRecyclerView;

    private DatabaseReference friendsRef, usersRef;
    private FirebaseAuth auth;
    private String currentUserID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        friendsView = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsRecyclerView = friendsView.findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        return friendsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            currentUserID = auth.getCurrentUser().getUid();
            friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
            FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>()
                    .setQuery(friendsRef, User.class)
                    .build();
            FirebaseRecyclerAdapter<User, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<User, FriendsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull User model) {
                    String friendsIDs = getRef(position).getKey();
                    final String[] profileImage = {"default_image"};
                    usersRef.child(friendsIDs).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild("profile_image")) {
                                profileImage[0] = snapshot.child("profile_image").getValue().toString();
                                String profileImage = snapshot.child("profile_image").getValue().toString();
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            String username = snapshot.child("username").getValue().toString();
                            String fullName = snapshot.child("full_name").getValue().toString();
                            String info = snapshot.child("info").getValue().toString();

                            holder.usernameTextView.setText(username);
                            holder.fullNameTextView.setText(fullName);
                            holder.infoTextView.setText(info);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                            chatIntent.putExtra("userID", friendsIDs);
                            chatIntent.putExtra("username", holder.usernameTextView.getText());
                            chatIntent.putExtra("profileImage", profileImage[0]);
                            startActivity(chatIntent);
                        }
                    });
                }

                @NonNull
                @Override
                public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                    return new FriendsViewHolder(view);
                }
            };
            friendsRecyclerView.setAdapter(adapter);
            adapter.startListening();
        }
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
}