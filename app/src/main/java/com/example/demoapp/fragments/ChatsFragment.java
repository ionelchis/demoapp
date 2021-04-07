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
import com.example.demoapp.activities.GroupChatActivity;
import com.example.demoapp.model.Chat;
import com.example.demoapp.model.User;
import com.example.demoapp.activities.ChatActivity;
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
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatsRecyclerView;

    private DatabaseReference chatsRef, usersRef, messagesRef, groupsRef;
    private FirebaseAuth auth;
    private String currentUserID;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
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
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView =  inflater.inflate(R.layout.fragment_chats, container, false);


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        chatsRecyclerView = privateChatsView.findViewById(R.id.chatsRecyclerView);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            currentUserID = auth.getCurrentUser().getUid();
            chatsRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(currentUserID);
            messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);

            FirebaseRecyclerOptions<Chat> options = new FirebaseRecyclerOptions.Builder<Chat>()
                    .setQuery(chatsRef, Chat.class)
                    .build();

            FirebaseRecyclerAdapter<Chat, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Chat, ChatsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Chat model) {
                    final String chatIDs = getRef(position).getKey();
                    final String[] profileImage = {"default_image"};

                    usersRef.child(chatIDs).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.hasChild("profile_image")) {
                                    profileImage[0] = snapshot.child("profile_image").getValue().toString();
                                    Picasso.get().load(profileImage[0]).placeholder(R.drawable.profile_image).into(holder.chatImage);
                                }
                                String username = snapshot.child("username").getValue().toString();
                                holder.chatNameTextView.setText(username);
                                final String[] message = new String[4];
                                //Get last message info
                                messagesRef.child(chatIDs)
                                        .limitToLast(1)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    DataSnapshot data = snapshot.getChildren().iterator().next();
                                                    message[0] = snapshot.child(data.getKey()).child("from").getValue().toString();
                                                    message[1] = snapshot.child(data.getKey()).child("message").getValue().toString();
                                                    message[2] = snapshot.child(data.getKey()).child("time").getValue().toString();
                                                    message[3] = snapshot.child(data.getKey()).child("date").getValue().toString().split(",")[0];

                                                    usersRef.child(message[0]).child("username").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                holder.chatLastMessage.setText(snapshot.getValue() + ": " + message[1]);
                                                                holder.chatLastMessageDate.setText(message[3]);
                                                                holder.chatLastMessageTime.setText(message[2] + ", ");
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {}
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //TODO - funct send user to chat activiy
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("receiverID", chatIDs);
                                        chatIntent.putExtra("name", username);
                                        chatIntent.putExtra("profileImage", profileImage[0]);
                                        startActivity(chatIntent);
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    groupsRef.child(chatIDs).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.hasChild("profile_image")) {
                                    profileImage[0] = snapshot.child("profile_image").getValue().toString();
                                    Picasso.get().load(profileImage[0]).placeholder(R.drawable.profile_image).into(holder.chatImage);
                                }
                                String name = snapshot.child("name").getValue().toString();
                                String dateCreated = snapshot.child("date_created").getValue().toString().split(",")[0];
                                String timeCreated = snapshot.child("time").getValue().toString() + ", ";
                                holder.chatNameTextView.setText(name);
                                final String[] message = new String[4];
                                //Get last message info
                                messagesRef.child(chatIDs)
                                        .limitToLast(1)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    DataSnapshot data = snapshot.getChildren().iterator().next();
                                                    message[0] = snapshot.child(data.getKey()).child("from").getValue().toString();
                                                    message[1] = snapshot.child(data.getKey()).child("message").getValue().toString();
                                                    message[2] = snapshot.child(data.getKey()).child("time").getValue().toString();
                                                    message[3] = snapshot.child(data.getKey()).child("date").getValue().toString().split(",")[0];
                                                    usersRef.child(message[0]).child("username").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                holder.chatLastMessage.setText(snapshot.getValue() + ": " + message[1]);
                                                                holder.chatLastMessageDate.setText(message[3]);
                                                                holder.chatLastMessageTime.setText(message[2] + ", ");
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {}
                                                    });
                                                }  else {
                                                    holder.chatLastMessage.setText("Group created");
                                                    holder.chatLastMessageDate.setText(dateCreated);
                                                    holder.chatLastMessageTime.setText(timeCreated);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //TODO - funct send user to chat activiy
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        String[] members = new String[(int) (snapshot.getChildrenCount()-1)];
                                        int k = 0;
                                        for (DataSnapshot c : snapshot.getChildren()) {
                                            if (!c.getKey().equals("name"))
                                                members[k++] = c.getKey();
                                        }
                                        chatIntent.putExtra("members", members);
                                        chatIntent.putExtra("receiverID", snapshot.getKey());
                                        chatIntent.putExtra("name", name);
                                        chatIntent.putExtra("profileImage", profileImage[0]);
                                        startActivity(chatIntent);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }

                @NonNull
                @Override
                public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_display_layout, parent, false);
                    return new ChatsViewHolder(view);
                }
            };

            chatsRecyclerView.setAdapter(adapter);
            adapter.startListening();
        }
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

            TextView chatNameTextView, chatLastMessage, chatLastMessageDate, chatLastMessageTime;
            CircleImageView chatImage;

            public ChatsViewHolder(@NonNull View itemView) {
                super(itemView);

                chatNameTextView = itemView.findViewById(R.id.chatNameTextView);
                chatLastMessage = itemView.findViewById(R.id.chatLastMessage);
                chatLastMessage.setLines(1);
                chatImage = itemView.findViewById(R.id.chatImage);
                chatLastMessageDate = itemView.findViewById(R.id.chatLastMessageDate);
                chatLastMessageTime = itemView.findViewById(R.id.chatLastMessageTime);
            }
        }
}