package com.example.demoapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoapp.model.FriendshipRequestType;
import com.example.demoapp.R;
import com.example.demoapp.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendshipRequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendshipRequestsFragment extends Fragment {

    private View friendshipRequestsFragmentView;
    private RecyclerView friendshipRequestsRecyclerView;

    private DatabaseReference friendshipRequestsRef, usersRef, friendsRef;
    private FirebaseAuth auth;
    private String currentUserID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendshipRequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendshipRequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendshipRequestsFragment newInstance(String param1, String param2) {
        FriendshipRequestsFragment fragment = new FriendshipRequestsFragment();
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
        friendshipRequestsFragmentView = inflater.inflate(R.layout.fragment_friendship_requests, container, false);

        friendshipRequestsRecyclerView = friendshipRequestsFragmentView.findViewById(R.id.friendshipRequestsRecyclerView);
        friendshipRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        friendshipRequestsRef = FirebaseDatabase.getInstance().getReference().child("Friendship Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        return friendshipRequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(friendshipRequestsRef.child(currentUserID), User.class)
                .build();

        FirebaseRecyclerAdapter<User, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<User, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull User model) {
                holder.itemView.findViewById(R.id.acceptRequestButton).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.declineRequestButton).setVisibility(View.VISIBLE);

                String requestUserID = getRef(position).getKey();

                DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            FriendshipRequestType type = FriendshipRequestType.valueOf(snapshot.getValue().toString());
                            if (type == FriendshipRequestType.RECEIVED) {
                                usersRef.child(requestUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("profile_image")) {
                                            String profileImage = snapshot.child("profile_image").getValue().toString();
                                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.profileImage) ;
                                        }
                                        String username = snapshot.child("username").getValue().toString();
                                        String fullName = snapshot.child("full_name").getValue().toString();
                                        String info = snapshot.child("info").getValue().toString();

                                        holder.usernameTextView.setText(username);
                                        holder.fullNameTextView.setText(fullName);
                                        holder.infoTextView.setText(R.string.sent_you_a_friend_request);


                                        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                acceptFriendshipRequest(requestUserID);
                                            }
                                        });

                                        holder.declineButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                declineFriendshipRequest(requestUserID);
                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else {
                                Button cancelRequestButton = holder.itemView.findViewById(R.id.acceptRequestButton);
                                cancelRequestButton.setText("Cancel");
                                cancelRequestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_decline, 0, 0, 0);

                                holder.itemView.findViewById(R.id.declineRequestButton).setVisibility(View.INVISIBLE);
                                usersRef.child(requestUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("profile_image")) {
                                            String profileImage = snapshot.child("profile_image").getValue().toString();
                                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.profileImage) ;
                                        }
                                        String username = snapshot.child("username").getValue().toString();
                                        String fullName = snapshot.child("full_name").getValue().toString();
                                        String info = snapshot.child("info").getValue().toString();

                                        holder.usernameTextView.setText(username);
                                        holder.fullNameTextView.setText(fullName);
                                        holder.infoTextView.setText("You sent a req to " + fullName);


                                        cancelRequestButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                declineFriendshipRequest(requestUserID);
                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new RequestViewHolder(view);
            }
        };
        friendshipRequestsRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void declineFriendshipRequest(String requestUserID) {
        friendshipRequestsRef.child(currentUserID).child(requestUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendshipRequestsRef.child(requestUserID).child(currentUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Request declined", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendshipRequest(String requestUserID) {
        friendsRef.child(currentUserID).child(requestUserID)
                .child("Friends")
                .setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(requestUserID).child(currentUserID)
                                    .child("Friends")
                                     .setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                friendshipRequestsRef.child(currentUserID).child(requestUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    friendshipRequestsRef.child(requestUserID).child(currentUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful())
                                                                                        Toast.makeText(getContext(), "New friend added", Toast.LENGTH_SHORT).show();
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

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView, fullNameTextView, infoTextView;
        CircleImageView profileImage;
        Button acceptButton, declineButton;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTextView = itemView.findViewById(R.id.usernameFriendsTextView);
            fullNameTextView = itemView.findViewById(R.id.fullNameFriendsTextView);
            infoTextView = itemView.findViewById(R.id.infoFriendsTextView);
            profileImage = itemView.findViewById(R.id.usersProfileImage);
            acceptButton = itemView.findViewById(R.id.acceptRequestButton);
            declineButton = itemView.findViewById(R.id.declineRequestButton);
        }
    }
}