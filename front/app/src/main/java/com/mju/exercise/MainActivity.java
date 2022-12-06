package com.mju.exercise;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mju.exercise.Calendar.CalendarActivity;
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

    static final int PERMISSIONS_REQUEST = 0x0000001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;


        initLoginTest();
        OnCheckPermission();

        Button button = (Button)findViewById(R.id.btnMap);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        Button button1 = (Button)findViewById(R.id.btnChat);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
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

            switch (v.getId()){
                case R.id.btnLoginOrUserInfo:
                    if (isLogined) {
                        //마이페이지 이동
                        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent.putExtra("userId", preferenceUtil.getString("userId"));
                        startActivity(intent);
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


    // 퍼미션 체크
    // 위치, 파일 및 미디어
    public void OnCheckPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(this, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,

                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE},

                        PERMISSIONS_REQUEST);

            } else {

                ActivityCompat.requestPermissions(this,

                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE},

                        PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}