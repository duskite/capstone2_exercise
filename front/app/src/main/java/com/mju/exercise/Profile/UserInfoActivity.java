package com.mju.exercise.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mju.exercise.ChatActivity;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.Profile.EditProfileActivity;
import com.mju.exercise.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends AppCompatActivity {
    private Context mContext;
    private ImageView mImgProfile;
    private TextView mTxtUserName, mTxtAddress, mTxtFavoriteSport, mTxtProfileMsg, mTxtFavoriteDay;
    private Button btnLogout,btnOneChatJoin;
    private ExtendedFloatingActionButton mbtnEditProfile;
    private String userId;

    private PreferenceUtil preferenceUtil;
    private RetrofitUtil retrofitUtil;

    private ProfileDTO beforeProfile;

    ChipGroup chipGroupFavDay, chipGroupFavSport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //컨텍스트 지정
        mContext = this;

        //초기화
        init();
    }

    /**
     * 초기화
     */
    private void init() {
        //프로필 사진
        mImgProfile = (ImageView) findViewById(R.id.imgProfile);
        mImgProfile.setOnClickListener(setOnClickListener);

        //사용자 이름
        mTxtUserName = (TextView) findViewById(R.id.txtUserName);
        //지역
        mTxtAddress = (TextView) findViewById(R.id.txtAddress);
        //선호종목
        mTxtFavoriteSport = (TextView) findViewById(R.id.txtFavoriteSport);
        //선호요일
        mTxtFavoriteDay = (TextView) findViewById(R.id.txtFavoriteDay);
        //상태 메시지
        mTxtProfileMsg = (TextView) findViewById(R.id.txtProfileMsg);
        //수정 버튼
        mbtnEditProfile = (ExtendedFloatingActionButton) findViewById(R.id.btnEditProfile);
        mbtnEditProfile.setOnClickListener(setOnClickListener);

        //로그아웃버튼
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(setOnClickListener);

        //1대1채팅 버튼
        btnOneChatJoin = (Button) findViewById(R.id.btnOneChat);
        btnOneChatJoin.setOnClickListener(setOnClickListener);

        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        retrofitUtil = RetrofitUtil.getInstance();

        //선호 요일, 선호 종목. 서버에서 가져온 데이터로 동적 추가
        chipGroupFavDay = (ChipGroup) findViewById(R.id.chipGroupFavDay);
        chipGroupFavSport = (ChipGroup) findViewById(R.id.chipGroupFavSport);

        //다른 사람의 프로필도 볼 수 있도록 하기 위하여 인텐트에 유저 아이디를 담어서 넘기도록 함
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        //만약 현재 보는 프로필과 내 유저아이디가 같다면 채팅 비활성화
        if(userId.equals(preferenceUtil.getString("userId"))){
            btnOneChatJoin.setVisibility(View.GONE);
        }
        //만약 현재 보는 프로필과 내 유저아이디가 다르다면 로그아웃과 수정하기 비활성화
        if(!userId.equals(preferenceUtil.getString("userId"))){
            btnLogout.setVisibility(View.GONE);
            mbtnEditProfile.setVisibility(View.GONE);
        }

        loadProfile(userId);
    }


    //프로필 가져오기
    private void loadProfile(String userId){
        Log.d("프로필로드", "넘어온 Id: " + userId);
        retrofitUtil.getRetrofitAPI().getUserProfile(userId).enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                Log.d("프로필로드", "onResponse");
                if(response.isSuccessful()){
                    Log.d("프로필로드", "isSuccessful");
                    //프로필 이미지 변경
                    reflectImg(response.body().getImage());
                    //통째로 넘겨서 가져온 데이터로 프로필 변경
                    reflectProfile(response.body());

                    Log.d("프로필로드", response.body().getNickname());
                    Log.d("프로필로드", response.body().getIntroduce());
                }
            }

            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {
                    Log.d("프로필로드", "onFailure");
            }
        });
    }

    private void reflectImg(String path){
        String url = retrofitUtil.getBASE_URL_NONE_SLASH() + path;
        Log.d("이미지로드", url);
        if(path != null && !path.equals("")){
            Glide.with(mContext).load(url).circleCrop().into(mImgProfile);
            mImgProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bigProfileImage(url);
                }
            });
        }
    }

    private void bigProfileImage(String url){
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_profile_image, null);
        final ImageView imageView = (ImageView) dialogLayout.findViewById(R.id.imgBigProfile);
        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();

        Glide.with(mContext).load(url).into(imageView);
        dialog.setView(dialogLayout);
        dialog.show();
    }

    //프로필 화면에 띄우기, 서버에서 가져온것 반영
    private void reflectProfile(ProfileDTO profileDTO){
        //현재 프로필 정보 저장, 수정화면으로 넘어갈때 넘겨주려고
        beforeProfile = profileDTO;

        mTxtUserName.setText(profileDTO.getNickname());
        mTxtProfileMsg.setText(profileDTO.getIntroduce());
        mTxtAddress.setText(profileDTO.getRegion());

        boolean[] favDay = new boolean[]{profileDTO.isFavMon(),
                profileDTO.isFavTue(), profileDTO.isFavWed(),
                profileDTO.isFavThu(), profileDTO.isFavFri(),
                profileDTO.isFavSat(), profileDTO.isFavSun()
        };
        boolean[] favSport = new boolean[]{
                profileDTO.isFavSoccer(), profileDTO.isFavFutsal(),
                profileDTO.isFavBaseball(), profileDTO.isFavBasketball(),
                profileDTO.isFavBadminton(), profileDTO.isFavCycle()
        };

        makeFavSportChipGroup(favSport);
        makeFavDayChipGroup(favDay);

    }

    //칩 안에 넣을 스트링 전달후 칩 만들어서 리턴
    private Chip makeChip(String chipText){
        Chip chip = new Chip(this); // Must contain context in parameter
        chip.setText(chipText);
        chip.setCheckable(false);

        return chip;
    }

    private void makeFavSportChipGroup(boolean[] favSportArray){
        String[] sports = {
                "축구", "풋살", "야구", "농구", "배드민턴", "사이클"
        };
        for(int i=0; i < favSportArray.length; i++){
            if(favSportArray[i]){
                chipGroupFavSport.addView(makeChip(sports[i]));
            }
        }

    }
    private void makeFavDayChipGroup(boolean[] favDayArray){

        String[] days = {
                "월요일", "화요일", "수요일","목요일","금요일","토요일","일요일"
        };
        for(int i=0; i < favDayArray.length; i++){
            if(favDayArray[i]){
                chipGroupFavDay.addView(makeChip(days[i]));
            }
        }

    }

    /**
     * 클릭 이벤트
     */
    private View.OnClickListener setOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.btnEditProfile:
                    Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);

                    //로드된 프로필이 있으면 담아서 전달
                    if(beforeProfile != null){
                        intent.putExtra("profile", beforeProfile);
                    }
                    startActivity(intent);
                    finish();
                    break;

                case R.id.btnLogout:
                    //로그아웃하면 모든 값을 비움
                    preferenceUtil.setString("accessToken", "");
                    preferenceUtil.setString("refreshIdx", "");
                    preferenceUtil.setString("userId", "");
                    preferenceUtil.setString("lat", "");
                    preferenceUtil.setString("lng", "");
                    preferenceUtil.setString("userIdx","");
                    preferenceUtil.setString("nickname", "");

                    finish();
                    break;

                case R.id.btnOneChat:
                    Intent intent1 = new Intent(getApplicationContext(), ChatActivity.class);
                    intent1.putExtra("otherId",userId);
                    intent1.putExtra("otherNickname",mTxtUserName.getText());

                  startActivity(intent1);
                  finish();
                  break;




            }


        }
    };
}