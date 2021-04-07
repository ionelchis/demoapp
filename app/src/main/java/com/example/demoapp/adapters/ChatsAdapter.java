package com.example.demoapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import com.example.demoapp.activities.ChatActivity;
import com.example.demoapp.fragments.ChatsFragment;
import com.example.demoapp.model.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {

    private Context context;
    private List<Chat> chatsList = new ArrayList<>();
    private String currentUserID;

    private FirebaseAuth auth;
    private DatabaseReference usersRef, groupsRef, chatsRef;

    public ChatsAdapter(Context context, List<Chat> chatsList) {
        this.context = context;
        this.chatsList = chatsList;
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_display_layout, parent, false);
        return new ChatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, int position)
    {

    }

    @Override
    public int getItemCount() {
        return chatsList.size();
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
