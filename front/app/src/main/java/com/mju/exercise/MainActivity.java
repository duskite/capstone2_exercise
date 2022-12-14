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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
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
    private FirebaseDatabase firebaseDatabase;

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

    private void updateNotiToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d("??????", s);

                firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("Notification").child("ALL_USERS").child(preferenceUtil.getString("userId")).setValue(s);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("?????????", "onRestart");
        Log.d("?????????", preferenceUtil.getString("accessToken"));
        loginCheck();
    }

    public void loginCheck(){
        Log.d("?????????", preferenceUtil.getString("accessToken"));
        if(preferenceUtil.getString("accessToken").equals("")){
            Log.d("?????????", "????????? ????????? ????????????");
            btnLoginOrUserInfo.setText("?????????");
            isLogined = false;
            return;
        }
        Log.d("?????????", "????????? ????????? ?????????");
        btnLoginOrUserInfo.setText("?????????");
        isLogined = true;
        //????????? ?????????????????? ?????? ?????? ????????????
        updateNotiToken();
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
     * ?????? ?????????
     */
    private View.OnClickListener setOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.btnLoginOrUserInfo:
                    if (isLogined) {
                        //??????????????? ??????
                        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent.putExtra("userId", preferenceUtil.getString("userId"));
                        startActivity(intent);
                    } else {
                        //?????????????????? ??????
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

    //?????? ??????????????? ?????? ????????? ????????? ????????? ??????, ?????? ??????
    private void goNextActivity(int sportType){
        Intent intent = new Intent(getApplicationContext(), OpenMatchActivity.class);
        intent.putExtra("sport", sportType);
        startActivity(intent);
    }


    // ????????? ??????
    // ??????, ?????? ??? ?????????
    public void OnCheckPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(this, "??? ????????? ???????????? ????????? ???????????? ?????????", Toast.LENGTH_LONG).show();

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
                    Toast.makeText(this, "??? ????????? ?????? ????????? ?????? ???????????????", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "??? ????????? ?????? ????????? ?????? ???????????????", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}