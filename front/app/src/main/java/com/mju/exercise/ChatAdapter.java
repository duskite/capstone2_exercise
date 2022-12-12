package com.mju.exercise;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.Profile.UserInfoActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ChatData.Comment> comments;
    private String otherNickname;
    private String myId;
    private Context mContext;
    private RetrofitUtil retrofitUtil;

    public ChatAdapter(ArrayList<ChatData.Comment> comments, String myId, String otherName, Context context) {
        this.comments = comments;
        this.myId = myId;
        this.otherNickname = otherName;
        this.mContext = context;

        retrofitUtil = RetrofitUtil.getInstance();
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
        ImageView profileImg;

        public OtherViewHolder(@NonNull View itemView) {
            super(itemView);

            show_msg = itemView.findViewById(R.id.show_message);
            time_msg = itemView.findViewById(R.id.txt_seen);
            name_tv = itemView.findViewById(R.id.name_tv);
            profileImg = itemView.findViewById(R.id.profile_image);
        }

        public void bind(ChatData.Comment comment) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy.MM.dd HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            Date date = new Date((long)comment.timestamp);
            String time = simpleDateFormat.format(date);
            show_msg.setText(comment.message);
            time_msg.setText(time);
            name_tv.setText(comment.senderId);
            loadProfile(comment.senderId, profileImg);

//            //상대 프로필로 넘어가기
//            profileImg.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(mContext, UserInfoActivity.class);
//                    intent.putExtra("userId", comment.senderId);
//                }
//            });
        }
    }

    private void setProfileImg(String path, ImageView imageView){
        String url = retrofitUtil.getBASE_URL_NONE_SLASH() + path;
        Log.d("채팅프로필", url);
        Glide.with(mContext).load(url).into(imageView);
    }

    //프로필 가져오기
    private void loadProfile(String nickName, ImageView imageView){
        Log.d("프로필로드", "넘어온 닉네임: " + nickName);
        retrofitUtil.getRetrofitAPI().getUserProfileImgByNickName(nickName).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String path = response.body();
                    if(path != null){
                        //프로필 이미지 변경
                        setProfileImg(path, imageView);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }
}
