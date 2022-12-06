package com.example.internchatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Scroller;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.internchatapplication.adapter.ChatAdapter;
import com.example.internchatapplication.databinding.ActivityLogoutBinding;
import com.example.internchatapplication.models.ChatModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class LogoutActivity extends AppCompatActivity {

    ActivityLogoutBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String senderUid;
    ArrayList<ChatModel> messages;
    ChatAdapter adapter;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLogoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        database=FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        getSupportActionBar().hide();
        setmessages();
        Toast.makeText(this, user.getDisplayName(), Toast.LENGTH_SHORT).show();
        try {
            Glide
                    .with(this)
                    .load(user.getPhotoUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_google)
                    .into(binding.gLogout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.gLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log_out();
            }
        });
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=binding.messageBox.getText().toString();
                if (!msg.equals("")) {
                    send_msg(msg);
                }
            }
        });
        binding.filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Still working",Toast.LENGTH_SHORT).show();
            }
        });
        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if(requestCode == 25) {
                if(data != null) {
                    if(data.getData() != null) {
                        Uri selectedImage = data.getData();
                        Calendar calendar = Calendar.getInstance();
                        StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                        dialog.show();
                        reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                dialog.dismiss();
                                if(task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String filePath = uri.toString();

                                            String messageTxt = binding.messageBox.getText().toString();

                                            Date date = new Date();
                                            ChatModel message = new ChatModel(messageTxt, senderUid, date.getTime(),user.getDisplayName());
                                            //message.setMessage("photo");
                                            message.setImageUrl(filePath);
                                            binding.messageBox.setText("");

                                            String randomKey = database.getReference().push().getKey();

                                            /*HashMap<String, Object> lastMsgObj = new HashMap<>();
                                            lastMsgObj.put("lastMsg", message.getMessage());
                                            lastMsgObj.put("lastMsgTime", date.getTime());

                                            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);*/

                                            database.getReference().child("chats")
                                                    .child("messages")
                                                    .child(randomKey)
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(LogoutActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(LogoutActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                            //Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private void setmessages() {
        messages=new ArrayList<>();
        adapter = new ChatAdapter(messages,this);
        binding.recyler.setLayoutManager(new LinearLayoutManager(this));
        binding.recyler.setAdapter(adapter);
        binding.recyler.scrollToPosition(messages.size());
        database.getReference().child("chats")
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            ChatModel message = snapshot1.getValue(ChatModel.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void send_msg(String msg) {
        senderUid = auth.getUid();
        Date date = new Date();
        ChatModel message = new ChatModel(msg, senderUid, date.getTime(),user.getDisplayName());
        message.setImageUrl("");
        binding.messageBox.setText("");

        String randomKey = database.getReference().push().getKey();

        /*HashMap<String, Object> lastMsgObj = new HashMap<>();
        lastMsgObj.put("lastMsg", message.getMessage());
        lastMsgObj.put("lastMsgTime", date.getTime());*/

        database.getReference().child("chats")
                .child("messages")
                .child(randomKey)
                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LogoutActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       /// Toast.makeText(LogoutActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void log_out() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Warning!!!");
        builder.setMessage("Are you sure to Log Out?");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                auth.signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }
}