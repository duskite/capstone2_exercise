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
import com.mju.exercise.Domain.OpenMatchDTO;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.OpenMatch.FilterDataLoader;
import com.mju.exercise.Profile.UserInfoActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private OnProfileListener onProfileListener;

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
            if(comment.senderId != null){
                Log.d("채팅프로필", "센더이름: " + comment.senderId);
                loadProfile(comment.senderId, profileImg);
            }

            //상대 프로필로 넘어가기
            profileImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retrofitUtil.getRetrofitAPI().getUserIdByNickName(comment.senderId).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.isSuccessful()){
                                onProfileListener.goProfile(response.body());
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                }
            });
        }
    }

    private void setProfileImg(String path, ImageView imageView){
        String url = retrofitUtil.getBASE_URL_NONE_SLASH() + path;
        Log.d("채팅프로필", url);
        Glide.with(mContext).load(url).circleCrop().into(imageView);
    }

    //프로필 가져오기
    private void loadProfile(String nickName, ImageView imageView){
        Log.d("채팅프로필", "닉네임: " + nickName);
        retrofitUtil.getRetrofitAPI().getUserIdByNickName(nickName).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    response.body();
                    Log.d("채팅프로필", response.body());
                    retrofitUtil.getRetrofitAPI().getUserProfile(response.body()).enqueue(new Callback<ProfileDTO>() {
                        @Override
                        public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                            if(response.isSuccessful()){
                                Log.d("채팅프로필", "프로필 성공");
                                setProfileImg(response.body().getImage(), imageView);
                            }
                        }

                        @Override
                        public void onFailure(Call<ProfileDTO> call, Throwable t) {
                            Log.d("채팅프로필", "프로필 실패");

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("채팅프로필", "실패");
            }
        });

    }

    public void setOnProfileListener(OnProfileListener onProfileListener){
        this.onProfileListener = onProfileListener;
    }
    public interface OnProfileListener {
        void goProfile(String userId);
    }

}
