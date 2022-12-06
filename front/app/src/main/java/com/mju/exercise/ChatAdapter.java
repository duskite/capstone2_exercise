package com.mju.exercise;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ChatData.Comment> comments;
    private String otherNickname;
    private String myId;

    public ChatAdapter(ArrayList<ChatData.Comment> comments,String myId, String otherName) {
        this.comments = comments;
        this.myId = myId;
        this.otherNickname = otherName;
    }

    private int MINE_CHAT = 0;
    private int OTHER_CHAT = 1;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MINE_CHAT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatting_me, parent, false);
            return new MineViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatting_you, parent, false);
            return new OtherViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatData.Comment comment = comments.get(position);
        if (holder instanceof MineViewHolder) {
            ((MineViewHolder) holder).bind(comment);
        } else {
            ((OtherViewHolder) holder).bind(comment);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (comments.get(position).senderId.equals(myId)) {
            return MINE_CHAT;
        } else {
            return OTHER_CHAT;
        }
    }

    public class MineViewHolder extends RecyclerView.ViewHolder{

        TextView show_msg;
        TextView time_msg;

        public MineViewHolder(@NonNull View itemView) {
            super(itemView);

            show_msg = itemView.findViewById(R.id.show_message);
            time_msg = itemView.findViewById(R.id.txt_seen);
        }

        public void bind(ChatData.Comment comment) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy.MM.dd HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            Date date = new Date((long)comment.timestamp);
            String time = simpleDateFormat.format(date);
            show_msg.setText(comment.message);
            time_msg.setText(time);
        }
    }

    public class OtherViewHolder extends RecyclerView.ViewHolder{

        TextView show_msg;
        TextView time_msg;
        TextView name_tv;

        public OtherViewHolder(@NonNull View itemView) {
            super(itemView);

            show_msg = itemView.findViewById(R.id.show_message);
            time_msg = itemView.findViewById(R.id.txt_seen);
            name_tv = itemView.findViewById(R.id.name_tv);
        }

        public void bind(ChatData.Comment comment) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy.MM.dd HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            Date date = new Date((long)comment.timestamp);
            String time = simpleDateFormat.format(date);
            show_msg.setText(comment.message);
            time_msg.setText(time);
            name_tv.setText(comment.senderId);
        }
    }
}
