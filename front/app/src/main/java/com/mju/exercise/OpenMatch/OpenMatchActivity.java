package com.mju.exercise.OpenMatch;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.mju.exercise.ChatActivity;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.Profile.UserInfoActivity;
import com.mju.exercise.R;
import com.mju.exercise.Sign.SignInActivity;
import com.mju.exercise.StatusEnum.Status;

import java.io.IOException;
import java.time.LocalDateTime;

public class OpenMatchActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private Button btnFilterOpen, btnProfileOpen;
    private PreferenceUtil preferenceUtil;
    //gps
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private Double lat, lng;

    private int sportType;

    //필터관련, 필터 선택 안할시 기본값
    private Status.FilterTypeDay mFilterTypeDay = Status.FilterTypeDay.DAY_DEFAULT;
    private Status.FilterTypeJoin mFilterTypeJoin = Status.FilterTypeJoin.JOIN_DEFAULT;
    private Status.FilterTypeDistance mFilterTypeDistance = Status.FilterTypeDistance.DISTANCE_DEFAULT;
    private Status.DistanceDiff mDiff = Status.DistanceDiff.DEFAULT;
    private Status.FavDayType mFavDay = Status.FavDayType.DEFAULT;

    private OpenMatchListFrag openMatchListFrag;
    private OpenMatchJoinedFrag openMatchJoinedFrag;
    private OpenMatchCreatedFrag openMatchCreatedFrag;
    private Status.FragType fragType = Status.FragType.LIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_match);

        Intent intent = getIntent();
        sportType = intent.getIntExtra("sport", 0);
        Log.d("인텐트체크", String.valueOf(intent.getIntExtra("sport", 0)));

        openMatchListFrag = OpenMatchListFrag.newInstance(sportType);
        openMatchCreatedFrag = OpenMatchCreatedFrag.newInstance();
        openMatchJoinedFrag = OpenMatchJoinedFrag.newInstance();
        //첫 시작은 LIST 프래그먼트
        fragType = Status.FragType.LIST;

        init();
        checkGPS();
        bottomNavigationView.setSelectedItemId(R.id.open_match_list);
    }


    private void init(){

        btnFilterOpen = (Button) findViewById(R.id.btnFilterOpen);
        btnProfileOpen = (Button) findViewById(R.id.btnProfileOpen);
        btnFilterOpen.setOnClickListener(setOnClickListener);
        btnProfileOpen.setOnClickListener(setOnClickListener);

        fragmentManager = getSupportFragmentManager();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(new NavSelectedListener());

        //gps
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //로컬에 저장된 내 아이디
        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
    }

    private View.OnClickListener setOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.btnFilterOpen:

                    if(fragType == Status.FragType.NOTICE){
                        return;
                    }

                    FilteringDialog filteringDialog = new FilteringDialog();
                    filteringDialog.show(getSupportFragmentManager(), "filtering");
                    filteringDialog.setDialogResult(new OpenMatchFilter() {
                        @Override
                        public void setFilter(Status.FilterTypeJoin filterTypeJoin, Status.FilterTypeDistance filterTypeDistance, Status.FilterTypeDay filterTypeDay, Status.DistanceDiff distanceDiff, Status.FavDayType favDayType, LocalDateTime localDateTime) {
                            mFilterTypeDay = filterTypeDay;
                            mFilterTypeDistance = filterTypeDistance;
                            mFilterTypeJoin = filterTypeJoin;
                            Log.d("필터", "setFilter");

                            if(fragType == Status.FragType.LIST){
                                openMatchListFrag.setFilter(filterTypeJoin, filterTypeDistance, filterTypeDay, distanceDiff, favDayType, localDateTime);
                            }else if(fragType == Status.FragType.OPENED){
                                openMatchCreatedFrag.setFilter(filterTypeJoin, filterTypeDistance, filterTypeDay, distanceDiff, favDayType, localDateTime);
                            }else if(fragType == Status.FragType.JOINED){
                                openMatchJoinedFrag.setFilter(filterTypeJoin, filterTypeDistance, filterTypeDay, distanceDiff, favDayType, localDateTime);
                            }

                        }
                    });

                    break;

                case R.id.btnProfileOpen:
                    if(loginCheck()){
                        //여기서는 나의 프로필 이므로 내 아이디 담아서 넘김
                        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent.putExtra("userId", preferenceUtil.getString("userId"));
                        startActivity(intent);
                    }else {

                        Toast.makeText(getApplicationContext(), "해당 기능은 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }
    };

    // 현재 사용자가 로그인 되어 있는지 확인해서 사용가능한 메뉴 컨트롤
    // 기존에 로그인 되면 프리퍼런스 유틸에 자기 유저아이디 담도록 해놨음.
    private boolean loginCheck(){
        String userId = preferenceUtil.getString("userId");
        if(userId.equals("") || userId == null){
            return false;
        }
        return true;
    }



    class NavSelectedListener implements NavigationBarView.OnItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentTransaction = fragmentManager.beginTransaction();
            switch (item.getItemId()){
                case R.id.open_match_list:{
                    fragmentTransaction.replace(R.id.host_fragment, openMatchListFrag)
                            .commit();
                    fragType = Status.FragType.LIST;
                    return true;
                }
                case R.id.open_match_created:{
                    if(loginCheck()){
                        fragmentTransaction.replace(R.id.host_fragment, openMatchCreatedFrag)
                                .commit();
                        fragType = Status.FragType.OPENED;
                    }else{
                        fragmentTransaction.replace(R.id.host_fragment, new LoginNoticeFrag())
                                .commit();
                        fragType = Status.FragType.NOTICE;
                    }


                    return true;
                }
                case R.id.open_match_joined:{
                    if(loginCheck()){
                        fragmentTransaction.replace(R.id.host_fragment, openMatchJoinedFrag)
                                .commit();
                        fragType = Status.FragType.JOINED;
                    }else{
                        fragmentTransaction.replace(R.id.host_fragment, new LoginNoticeFrag())
                                .commit();
                        fragType = Status.FragType.NOTICE;
                    }

                    return true;
                }

                case R.id.open_match_create:
                    if(loginCheck()){
                        fragmentTransaction.replace(R.id.host_fragment, new OpenMatchOpenFrag())
                                .commit();
                        fragType = Status.FragType.NOTICE;
                    }else{
                        fragmentTransaction.replace(R.id.host_fragment, new LoginNoticeFrag())
                                .commit();
                        fragType = Status.FragType.NOTICE;
                    }
                    return true;

            }

            return false;
        }
    }

    //현재 위치 가져오기
    public void checkGPS () {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> currentLocationTask = fusedLocationProviderClient.getCurrentLocation(
                100,
                cancellationTokenSource.getToken()
        );
        currentLocationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();

                    lat = location.getLatitude();
                    lng = location.getLongitude();

                    preferenceUtil.setString("lat", lat.toString());
                    preferenceUtil.setString("lng", lng.toString());
                }
            }
        });
    }
}
