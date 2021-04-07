package com.example.demoapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.model.ChatType;
import com.example.demoapp.model.Message;
import com.example.demoapp.adapters.MessageAdapter;
import com.example.demoapp.model.MessageType;
import com.example.demoapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String receiverID, receiverUsername, receiverProfileImage, messageSenderID;
    private String[] members;

    private TextView username;
    private CircleImageView userProfileImage;
    private RecyclerView privateMessagesList;

    private Toolbar chatToolbar;
    private EditText userMessageInput;
    private ImageButton sendPrivateMessageButton;
    private ImageButton attachFileButton;

    private FirebaseAuth auth;
    private DatabaseReference rootRef, chatsRef;

    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private String myUrl = "";
    private MessageType checker;
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        messageSenderID = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Chats");

        receiverID = getIntent().getExtras().get("receiverID").toString();
        members = (String[]) getIntent().getExtras().get("members");

        receiverUsername = getIntent().getExtras().get("name").toString();
        receiverProfileImage = getIntent().getExtras().get("profileImage").toString();

        initializeFields();

        sendPrivateMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        attachFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachFile();
            }
        });

        rootRef.child("Messages").child(messageSenderID).child(receiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Message message = snapshot.getValue(Message.class);
                        messagesList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        privateMessagesList.smoothScrollToPosition(privateMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

    }

    private void attachFile() {
        CharSequence options[] = new CharSequence[]
                {
                  "Image",
                  "PDF File",
                  "Document"
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select file");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    checker = MessageType.IMAGE;
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent.createChooser(galleryIntent, "Select image"), 438);
                }
                if (which == 1) {
                    checker = MessageType.PDF;
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("application/*");
                    startActivityForResult(galleryIntent.createChooser(galleryIntent, "Select image"), 438);
                }
                if (which == 2) {
                    checker = MessageType.DOCUMENT;
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadingBar.setTitle("Sending image");
            loadingBar.setMessage("Your image is uploading");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            fileUri = data.getData();
            if (checker != MessageType.IMAGE) {
                //TODO - implementation for pdf and doc
            } else if (checker == MessageType.IMAGE) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + receiverID;
                final String messageReceiverRef = "Messages/" + receiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderID).child(receiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                StorageReference filePath = storageReference.child(messagePushID + "." + ".jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map<String, Object> messageImageBody = new HashMap<String, Object>();
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("filename", fileUri.getLastPathSegment());
                            messageImageBody.put("type", MessageType.IMAGE);
                            messageImageBody.put("from", messageSenderID);
                            messageImageBody.put("to", receiverID);
                            messageImageBody.put("messageUID", messagePushID);
                            messageImageBody.put("time", getCurrentTime());
                            messageImageBody.put("date", getCurrentDate());

                            Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Error while sending image", Toast.LENGTH_SHORT).show();
                                    }
                                    userMessageInput.setText("");
                                }
                            });
                        }
                    }
                });

            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
            loadingBar.dismiss();
        }
    }

    private void sendMessage() {
        String message = userMessageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(message)) {
            if (members == null) {
                String messageSenderRef = "Messages/" + messageSenderID + "/" + receiverID;
                String messageReceiverRef = "Messages/" + receiverID + "/" + messageSenderID;

                Map<String, Object> chatBody = new HashMap<String, Object>();
                chatBody.put(messageSenderID + "/" + receiverID + "/type", ChatType.PRIVATE);
                chatBody.put(receiverID + "/" + messageSenderID + "/type", ChatType.PRIVATE);
                chatsRef.updateChildren(chatBody);

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderID).child(receiverID).push();

                String messagePushID = userMessageKeyRef.getKey();

                Map<String, Object> messageTextBody = new HashMap<String, Object>();
                messageTextBody.put("message", message);
                messageTextBody.put("type", MessageType.TEXT);
                messageTextBody.put("from", messageSenderID);
                messageTextBody.put("to", receiverID);
                messageTextBody.put("messageUID", messagePushID);
                messageTextBody.put("time", getCurrentTime());
                messageTextBody.put("date", getCurrentDate());

                Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(ChatActivity.this, "Error while sending message", Toast.LENGTH_SHORT).show();
                        }
                        userMessageInput.setText("");
                    }
                });
            } else {
                for (String memberID : members) {
                    String messageSenderRef = "Messages/" + memberID + "/" + receiverID;
                    DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                            .child(memberID).child(receiverID).push();
                    String messagePushID = userMessageKeyRef.getKey();
                    Map<String, Object> messageTextBody = new HashMap<String, Object>();
                    messageTextBody.put("message", message);
                    messageTextBody.put("type", MessageType.TEXT);
                    messageTextBody.put("from", messageSenderID);
                    messageTextBody.put("to", receiverID);
                    messageTextBody.put("messageUID", messagePushID);
                    messageTextBody.put("time", getCurrentTime());
                    messageTextBody.put("date", getCurrentDate());
                    Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);

                    rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(ChatActivity.this, "Error while sending message", Toast.LENGTH_SHORT).show();
                            }
                            userMessageInput.setText("");
                        }
                    });
                }
            }
        }
    }

    private void initializeFields() {
        chatToolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("");

        View actionBarView = getLayoutInflater().inflate(R.layout.chat_bar_layout, null);
        chatToolbar.addView(actionBarView);

        userProfileImage = actionBarView.findViewById(R.id.chatBarProfileImage);
        username = actionBarView.findViewById(R.id.chatBarUsername);
        username.setText(receiverUsername);
        Picasso.get().load(receiverProfileImage).placeholder(R.drawable.profile_image).into(userProfileImage);

        sendPrivateMessageButton = findViewById(R.id.sendPrivateMessageButton);
        userMessageInput = findViewById(R.id.userMessageInput);
        attachFileButton = findViewById(R.id.attachFileButton);

        messageAdapter = new MessageAdapter(messagesList);
        privateMessagesList = findViewById(R.id.privateMessagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        privateMessagesList.setLayoutManager(linearLayoutManager);
        privateMessagesList.setAdapter(messageAdapter);





        loadingBar = new ProgressDialog(this);

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