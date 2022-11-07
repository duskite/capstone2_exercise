package com.mju.exercise.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    private ImageView mImgProfile, mImgViewEditProfile;
    private TextView mTxtUserName, mTxtAddress, mTxtFavoriteSport, mTxtProfileMsg, mTxtFavoriteDay;
    private Button btnLogout;

    private PreferenceUtil preferenceUtil;
    private RetrofitUtil retrofitUtil;

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
        mImgViewEditProfile = (ImageView) findViewById(R.id.imgViewEditProfile);
        mImgViewEditProfile.setOnClickListener(setOnClickListener);

        //로그아웃버튼
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(setOnClickListener);

        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        retrofitUtil = RetrofitUtil.getInstance();

        //우선 값 넘기는거 테스트
        loadProfile(preferenceUtil.getString("userId"));
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
//                    mTxtUserName.setText(response.body().getNickname());
//                    mTxtProfileMsg.setText(response.body().getIntroduce());

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
            Glide.with(this).load(url).into(mImgProfile);
        }
    }

    private void reflectProfile(ProfileDTO profileDTO){
        mTxtUserName.setText(profileDTO.getNickname());
        mTxtProfileMsg.setText(profileDTO.getIntroduce());
//        mTxtAddress.setText(profileDTO.getRegion());

        boolean[] favDay = new boolean[]{profileDTO.isFavMon(),
                profileDTO.isFavTue(), profileDTO.isFavWed(),
                profileDTO.isFavThu(), profileDTO.isFavFri(),
                profileDTO.isFavSat(), profileDTO.isFavSun()
        };

        mTxtFavoriteDay.setText("선호 요일: " + makeFavDayString(favDay));

        boolean[] favSport = new boolean[]{
                profileDTO.isFavSoccer(), profileDTO.isFavFutsal(),
                profileDTO.isFavBaseball(), profileDTO.isFavBasketball(),
                profileDTO.isFavBadminton(), profileDTO.isFavCycle()
        };

        mTxtFavoriteSport.setText("선호 종목: " + makeFavSportString(favSport));
    }

    //선호 날짜 담겨있는 불린 배열 받아서 좋아하는 종목을 한 줄짜리 문자열로 변환
    private String makeFavSportString(boolean[] favSportArray){
        String[] sports = {
                "축구", "풋살", "야구", "농구", "배드민턴", "사이클"
        };
        StringBuilder result = new StringBuilder();
        for(int i=0; i<favSportArray.length; i++){
            if(favSportArray[i]){
                result.append(sports[i] + " ");
            }
        }
        return result.toString();
    }
    //선호 종목 담겨있는 불린 배열 받아서 좋아하는 종목을 한 줄짜리 문자열로 변환
    private String makeFavDayString(boolean[] favDayArray){
        String[] days = {
                "월", "화", "수","목","금","토","일"
        };
        StringBuilder result = new StringBuilder();
        for(int i=0; i<favDayArray.length; i++){
            if(favDayArray[i]){
                result.append(days[i] + " ");
            }
        }
        return result.toString();
    }

    /**
     * 클릭 이벤트
     */
    private View.OnClickListener setOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mImgViewEditProfile) {

                Toast.makeText(mContext, "수정", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
                finish();

            }else if (v == btnLogout){
                //로그아웃하면 모든 값을 비움
                preferenceUtil.setString("accessToken", "");
                preferenceUtil.setString("refreshIdx", "");
                preferenceUtil.setString("userId", "");
                finish();
            }
        }
    };
}