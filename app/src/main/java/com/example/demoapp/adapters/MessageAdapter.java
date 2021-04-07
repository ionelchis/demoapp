package com.example.demoapp.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import com.example.demoapp.activities.ImageViewerActivity;
import com.example.demoapp.model.Message;
import com.example.demoapp.model.MessageType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> userMessagesList;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Message> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText, senderMessageTime, receiverMessageTime, currentDateChanged;
        public CircleImageView receiverProfileImage, senderProfileImage;
        public ImageView receiverImageMessage, senderImageMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.senderMessageText);
            receiverMessageText = itemView.findViewById(R.id.receiverMessageText);
            receiverProfileImage = itemView.findViewById(R.id.receiverProfileImage);
            senderProfileImage = itemView.findViewById(R.id.senderProfileImage);
            receiverImageMessage = itemView.findViewById(R.id.receiverImageMessage);
            senderImageMessage = itemView.findViewById(R.id.senderImageMessage);
            senderMessageTime = itemView.findViewById(R.id.senderMessageTime);
            receiverMessageTime = itemView.findViewById(R.id.receiverMessageTime);
            currentDateChanged = itemView.findViewById(R.id.currentDateChanged);
        }


    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);
        auth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID = auth.getCurrentUser().getUid();
        Message message = userMessagesList.get(position);

        String fromUserID = message.getFrom();
        MessageType fromMessageType = MessageType.valueOf(message.getType());
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("profile_image")) {
                        String profileImage = snapshot.child("profile_image").getValue().toString();
                        Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.senderProfileImage);
                        Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                    } else {
                        Picasso.get().load(R.drawable.profile_image).into(holder.receiverProfileImage);
                        Picasso.get().load(R.drawable.profile_image).into(holder.senderProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.senderProfileImage.setVisibility(View.INVISIBLE);
        holder.senderImageMessage.setVisibility(View.GONE);
        holder.receiverImageMessage.setVisibility(View.GONE);
        holder.receiverMessageTime.setVisibility(View.GONE);
        holder.senderMessageTime.setVisibility(View.GONE);
        holder.currentDateChanged.setVisibility(View.GONE);

        if ((position > 0 && !(message.getDate().equals(userMessagesList.get(position-1).getDate()))) || position == 0) {
            holder.currentDateChanged.setVisibility(View.VISIBLE);
            holder.currentDateChanged.setText(message.getDate());
        }

        if (fromMessageType == MessageType.TEXT) {
            if (fromUserID.equals(messageSenderID)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setText(message.getMessage());
                //Show time and profile image if the time of since last message changed
                if (canToggleImageAndTime(position, fromUserID, message)) {
                    holder.senderProfileImage.setVisibility(View.VISIBLE);
                    holder.senderMessageTime.setVisibility(View.VISIBLE);
                    holder.senderMessageTime.setText(message.getTime());
                }
            } else 
                {
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setText(message.getMessage());
                //Show time and profile image if the time of since last message changed
                if (canToggleImageAndTime(position, fromUserID, message)) {
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.receiverMessageTime.setVisibility(View.VISIBLE);
                    holder.receiverMessageTime.setText(message.getTime());
                }
            }
        } else if (fromMessageType == MessageType.IMAGE) {
            if (fromUserID.equals(messageSenderID)) {
                holder.senderImageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into(holder.senderImageMessage);

                //Show time and profile image if the time of since last message changed
                if (canToggleImageAndTime(position, fromUserID, message)) {
                    holder.senderProfileImage.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.senderMessageTime.getLayoutParams();
                    params.addRule(RelativeLayout.BELOW, holder.senderImageMessage.getId());
                    holder.senderMessageTime.setVisibility(View.VISIBLE);
                    holder.senderMessageTime.setText(message.getTime());
                }

                //Set listeners for sender image
                holder.senderImageMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewImage(position, holder);
                    }
                });
                holder.senderImageMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        CharSequence options[] = new CharSequence[] {
                                "Delete message",
                                "View image",
                                "Download image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteSentMessage(position, holder);
                                } else if (which == 1) {
                                    viewImage(position, holder);
                                } else if (which == 2) {

                                }
                            }
                        });
                        builder.show();
                        return true;
                    }
                });
            } else {
                holder.receiverImageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into(holder.receiverImageMessage);

                //Show time and profile image if the time of since last message changed
                if (canToggleImageAndTime(position, fromUserID, message)) {
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.receiverMessageTime.getLayoutParams();
                    params.addRule(RelativeLayout.BELOW, holder.receiverImageMessage.getId());
                    holder.receiverMessageTime.setVisibility(View.VISIBLE);
                    holder.receiverMessageTime.setText(message.getTime());
                }

                //Set listeners for receiver image
                holder.receiverImageMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewImage(position, holder);
                    }
                });
                holder.receiverImageMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        CharSequence options[] = new CharSequence[] {
                                "Delete for me",
                                "View image",
                                "Download image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteReceivedMessage(position, holder);
                                } else if (which == 1) {
                                    viewImage(position, holder);
                                } else if (which == 2) {
                                }
                            }
                        });
                        builder.show();
                        return true;
                    }
                });
            }
        }

        if (fromUserID.equals(messageSenderID)) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (userMessagesList.get(position).getType().equals(MessageType.TEXT.toString())) {
                        CharSequence options[] = new CharSequence[] {
                                "Delete message",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteSentMessage(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                    return true;
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (userMessagesList.get(position).getType().equals(MessageType.TEXT.toString())) {
                        CharSequence options[] = new CharSequence[] {
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteReceivedMessage(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageUID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getFrom())
                                    .child(userMessagesList.get(position).getMessageUID())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                                                userMessagesList.remove(position);
                                                notifyDataSetChanged();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Error while deleting message", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteReceivedMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageUID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                            userMessagesList.remove(position);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Error while deleting message", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void viewImage(final int position, final MessageViewHolder holder) {
        Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
        intent.putExtra("url", userMessagesList.get(position).getMessage());
        holder.itemView.getContext().startActivity(intent);
    }

    private boolean canToggleImageAndTime(final int position, String fromUserID, Message message) {
        return !(position > 0 && userMessagesList.get(position-1).getFrom().equals(fromUserID)
                && userMessagesList.get(position-1).getDate().equals(message.getDate())
                && userMessagesList.get(position-1).getTime().equals(message.getTime()));
    }
}
