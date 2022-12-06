package com.mju.exercise;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mju.exercise.Preference.PreferenceUtil;

import java.util.ArrayList;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getName() + " HH_LOG";
    private FirebaseDatabase firebaseDatabase;  // 실시간 데이터베이스
    private PreferenceUtil preferenceUtil;
    private ArrayList<ChatData.Comment> comments;
    private ImageButton btn_send;
    private EditText text_send;
    private TextView chatTitle;
    private String myNickname;
    private String chatRoomUid; //채팅방 하나 id
    private String groupTitle; //채팅방 하나 id
    private ChatAdapter chatAdapter;
    RecyclerView chatRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        Intent i = getIntent();
        String otherId = i.getStringExtra("otherId");
        String otherNickname = i.getStringExtra("otherNickname");

        groupTitle = i.getStringExtra("groupTitle");

        initView();
        checkChatRoom(otherNickname);
        createChatting(otherId, otherNickname);
    }

    private void initView() {
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        chatRecyclerView = findViewById(R.id.recycler_view);
        chatTitle = findViewById(R.id.username);


        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);    // test 이름의 기본모드 설정, 만약 test key값이 있다면 해당 값을 불러옴.
        myNickname = sharedPreferences.getString("userNickname", "");
    }

    private void createChatting(String otherId, String otherNickname) {
        btn_send.setOnClickListener(v -> {
            if (groupTitle != null) {

                ChatData ChatData = new ChatData();
                ChatData.usersId.put(myNickname, true);
                if (chatRoomUid == null) {
                    Toast.makeText(ChatActivity.this, "채팅방 생성", Toast.LENGTH_SHORT).show();

                    firebaseDatabase.getReference().child("groupChat").child(groupTitle).push().setValue(ChatData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            checkChatRoom(otherNickname);
                        }
                    });
                } else {
                    sendMsgToDataBase();
                }

            } else {

                ChatData ChatData = new ChatData();
                ChatData.usersId.put(myNickname, true);
                ChatData.usersId.put(otherNickname, true);

                if (chatRoomUid == null) {
                    Toast.makeText(ChatActivity.this, "채팅방 생성", Toast.LENGTH_SHORT).show();

                    firebaseDatabase.getReference().child("chatRooms").push().setValue(ChatData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            checkChatRoom(otherNickname);
                        }
                    });
                } else {
                    sendMsgToDataBase();
                }
            }
        });
    }

    private void checkChatRoom(String otherNickname) {
        comments = new ArrayList<>(); //채팅 메시지

        if (groupTitle != null) {
            chatTitle.setText(groupTitle);

            //단체채팅
            firebaseDatabase.getReference().child("groupChat").orderByChild(groupTitle).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) //나, 상대방 id 가져온다.
                    {
                        ChatData chatData = dataSnapshot.getValue(ChatData.class);
                        Log.i(TAG, "onDataChange: chatDtat = " + comments);
                        chatRoomUid = dataSnapshot.getKey();
                        btn_send.setEnabled(true);

                        chatAdapter = new ChatAdapter(comments, myNickname, otherNickname);
                        getChatting();
                        //동기화
                        chatRecyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                        chatRecyclerView.setAdapter(chatAdapter);

                        //메시지 보내기
                        sendMsgToDataBase();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            chatTitle.setText(otherNickname);
            //개인채팅
            firebaseDatabase.getReference().child("chatRooms").orderByChild("usersId/" + myNickname).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) //나, 상대방 id 가져온다.
                    {
                        ChatData ChatData = dataSnapshot.getValue(ChatData.class);
                        if (ChatData.usersId.containsKey(otherNickname)) {           //상대방 id 포함돼 있을때 채팅방 key 가져옴
                            chatRoomUid = dataSnapshot.getKey();
                            Log.i(TAG, "onDataChange: chatRoomUid = " + chatRoomUid);
                            btn_send.setEnabled(true);

                            chatAdapter = new ChatAdapter(comments, myNickname, otherNickname);
                            getChatting();
                            //동기화
                            chatRecyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                            chatRecyclerView.setAdapter(chatAdapter);

                            //메시지 보내기
                            sendMsgToDataBase();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void sendMsgToDataBase() {
        if (!text_send.getText().toString().equals("")) {
            ChatData.Comment comment = new ChatData.Comment();
            comment.senderId = myNickname;
            comment.message = text_send.getText().toString();
            comment.timestamp = ServerValue.TIMESTAMP;
            if (groupTitle != null) {
                firebaseDatabase.getReference().child("groupChat").child(groupTitle).child("comments").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        text_send.setText("");
                    }
                });
            } else {
                firebaseDatabase.getReference().child("chatRooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        text_send.setText("");
                    }
                });
            }
        }
    }

    private void getChatting() {
        if (groupTitle != null) {
            FirebaseDatabase.getInstance().getReference().child("groupChat").child(groupTitle).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    comments.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        comments.add(dataSnapshot.getValue(ChatData.Comment.class));
                    }
                    chatAdapter.notifyDataSetChanged();

                    chatRecyclerView.scrollToPosition(comments.size() - 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            FirebaseDatabase.getInstance().getReference().child("chatRooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    comments.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        comments.add(dataSnapshot.getValue(ChatData.Comment.class));
                    }
                    chatAdapter.notifyDataSetChanged();

                    chatRecyclerView.scrollToPosition(comments.size() - 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }
}

































































