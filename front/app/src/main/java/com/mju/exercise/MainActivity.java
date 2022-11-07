package com.mju.exercise;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.OpenMatch.OpenMatchActivity;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.Profile.UserInfoActivity;
import com.mju.exercise.Sign.SignInActivity;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private Button btnLoginOrUserInfo;
    private Button btnSoccer, btnFutsal, btnBaseball, btnBasketball, btnBadminton, btnCycle;

    private PreferenceUtil preferenceUtil;
    private RetrofitUtil retrofitUtil;

    private static boolean isLogined = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //육성연 : 로그인, db관련 테스트중
        initLoginTest();

        Button button = (Button)findViewById(R.id.btnMap);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("로그인", "onRestart");
        Log.d("로그인", preferenceUtil.getString("accessToken"));
        loginCheck();
    }

    public void loginCheck(){
        Log.d("로그인", preferenceUtil.getString("accessToken"));
        if(preferenceUtil.getString("accessToken").equals("")){
            Log.d("로그인", "엑세스 토큰이 비어있음");
            btnLoginOrUserInfo.setText("로그인");
            isLogined = false;
            return;
        }
        Log.d("로그인", "엑세스 토큰이 들었음");
        btnLoginOrUserInfo.setText("프로필");
        isLogined = true;
        return;
    }

    public void initLoginTest() {
        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        retrofitUtil = RetrofitUtil.getInstance();
        btnLoginOrUserInfo = (Button) findViewById(R.id.btnLoginOrUserInfo);
        btnLoginOrUserInfo.setOnClickListener(setOnClickListener);

        btnSoccer = (Button) findViewById(R.id.btnSoccer);
        btnFutsal = (Button) findViewById(R.id.btnFutsal);
        btnBaseball = (Button) findViewById(R.id.btnBaseball);
        btnBasketball = (Button) findViewById(R.id.btnBasketball);
        btnBadminton = (Button) findViewById(R.id.btnBadminton);
        btnCycle = (Button) findViewById(R.id.btnCycle);

        btnSoccer.setOnClickListener(setOnClickListener);
        btnFutsal.setOnClickListener(setOnClickListener);
        btnBaseball.setOnClickListener(setOnClickListener);
        btnBasketball.setOnClickListener(setOnClickListener);
        btnBadminton.setOnClickListener(setOnClickListener);
        btnCycle.setOnClickListener(setOnClickListener);

        loginCheck();
    }

    /**
     * 클릭 이벤트
     */
    private View.OnClickListener setOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            if (v == btnLoginOrUserInfo) {
//
//                if (isLogined) {
//                    //마이페이지 이동
//                    startActivity(new Intent(mContext, UserInfoActivity.class));
//                } else {
//                    //로그인창으로 이동
//                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
//                    startActivity(intent);
//                }
//            }

            switch (v.getId()){
                case R.id.btnLoginOrUserInfo:
                    if (isLogined) {
                        //마이페이지 이동
                        startActivity(new Intent(mContext, UserInfoActivity.class));
                    } else {
                        //로그인창으로 이동
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        startActivity(intent);
                    }
                    break;

                case R.id.btnSoccer:
                case R.id.btnFutsal:
                case R.id.btnBaseball:
                case R.id.btnBasketball:
                case R.id.btnBadminton:
                case R.id.btnCycle:
                    goNextActivity(v.getId());
                    break;
            }
        }
    };

    //다음 액티비티에 어떤 리스트 뿌려야 하는지 전달, 종목 전달
    private void goNextActivity(int sportType){
        Intent intent = new Intent(getApplicationContext(), OpenMatchActivity.class);
        intent.putExtra("sport", sportType);
        startActivity(intent);
    }
}