package com.example.internchatapplication.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.internchatapplication.R;
import com.example.internchatapplication.models.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{
    ArrayList<ChatModel> model;
    Context context;

    public ChatAdapter(ArrayList<ChatModel> model, Context context) {
        this.model = model;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.item_sent_group,parent,false);
        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chatModel=model.get(position);
        try {
            holder.msgtxt.setText(chatModel.getMessage());
            if (!chatModel.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
                holder.lay.setGravity(Gravity.LEFT);
                holder.lay2.setBackgroundResource(R.drawable.receive_drawable);
                holder.nametxt.setText(chatModel.getName());
            }else
            {
                holder.lay.setGravity(Gravity.RIGHT);
                holder.nametxt.setVisibility(View.GONE);
                holder.view.setVisibility(View.GONE);
            }
            if (!chatModel.getImageUrl().equals("")) {
                holder.image.setVisibility(View.VISIBLE);
                    Glide
                            .with(context)
                            .load(chatModel.getImageUrl())
                            .centerCrop()
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(holder.image);
            }

        } catch (Exception e) {
            //Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
        holder.lay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!chatModel.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("chats")
                                            .child("messages")
                                            .child(chatModel.getMessageId()).setValue(null);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create();
                    dialog.show();
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView msgtxt,nametxt;
        LinearLayout lay,lay2;
        ImageView image;
        View view;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            msgtxt=itemView.findViewById(R.id.message);
            nametxt=itemView.findViewById(R.id.name);
            lay=itemView.findViewById(R.id.lay);
            lay2=itemView.findViewById(R.id.lay2);
            view=itemView.findViewById(R.id.view7);
            image=itemView.findViewById(R.id.image);
        }
    }
}
