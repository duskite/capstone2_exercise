package com.mju.exercise.Profile;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.mju.exercise.Domain.ApiResponseDTO;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    Button btnEnter, btnRegionLoad;
    TextView tvRegion;
    ImageView imgProfile;
    EditText edtNickname, edtProfileMsg;
    ChipGroup chipGroupFavDay, chipGroupFavSport;

    //이미지 업로드 위해서 액티비티 결과값 체크
    ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri imgUri;

    private PreferenceUtil preferenceUtil;
    private RetrofitUtil retrofitUtil;
    private String serverImgPath;

    //선호하는 요일과 운동 체크용, 디비에 전송하기 전에 잠시 담아둠
    private boolean[] favDays = new boolean[7];
    private boolean[] favSports = new boolean[6];
    List<Integer> checkedDays = new ArrayList<>();
    List<Integer> checkedSports = new ArrayList<>();

    private ProfileDTO beforeProfile;

    //gps
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        init();

        //넘어온 인텐트가 있으면 해당 내용으로 수정란 채워놓음
        Intent intent = getIntent();
        if(intent.getSerializableExtra("profile") != null){
            beforeProfile = (ProfileDTO) intent.getSerializableExtra("profile");
            loadBeforeProfile();
        }
    }

    //수정전 기존 프로필 내용으로 반영
    public void loadBeforeProfile(){
        edtNickname.setText(beforeProfile.getNickname());
        tvRegion.setText(beforeProfile.getRegion());

        String path = beforeProfile.getImage();
        String url = retrofitUtil.getBASE_URL_NONE_SLASH() + path;
        Log.d("이미지로드", url);
        if(path != null && !path.equals("")){
            Glide.with(this).load(url).circleCrop().into(imgProfile);
        }
        edtProfileMsg.setText(beforeProfile.getIntroduce());
    }

    public void init() {
        //gps
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        retrofitUtil = RetrofitUtil.getInstance();
        retrofitUtil.setToken(preferenceUtil.getString("accessToken"));

        btnEnter = (Button) findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(onClickListener);
        btnRegionLoad = (Button) findViewById(R.id.btnRegionLoad);
        btnRegionLoad.setOnClickListener(onClickListener);

        tvRegion = (TextView) findViewById(R.id.tvRegion);

        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(onClickListener);

        edtNickname = (EditText) findViewById(R.id.txtNickname);
        edtProfileMsg = (EditText) findViewById(R.id.txtProfileMsg);

        chipGroupFavDay = (ChipGroup) findViewById(R.id.chipGroupFavDay);
        chipGroupFavDay.setOnCheckedStateChangeListener(setOnCheckedStateChangeListener);
        chipGroupFavSport = (ChipGroup) findViewById(R.id.chipGroupFavSport);
        chipGroupFavSport.setOnCheckedStateChangeListener(setOnCheckedStateChangeListener);


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData().getData() != null) {
                imgUri = result.getData().getData();
                Glide.with(this).load(imgUri).circleCrop().into(imgProfile);
            } else {

            }
        });

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

                    try{
                        Log.d("지오코더", geocoderToStr(location));
                        tvRegion.setText(geocoderToStr(location));

                    }catch (IOException e){
                        Toast.makeText(getApplicationContext(), "지역정보 조회 실패", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private String geocoderToStr(Location location) throws IOException {
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),
                1);
        Address address = addressList.get(0);

        return address.getAdminArea() + " " + address.getLocality() + " " + address.getThoroughfare();
    }

    //서버로 프로필 전송전에 칩그룹 값을 가지고 선호 요일, 선호 종목 boolean배열 바꿈
    private void favReflectAtArray(){

        for(Integer i : checkedDays){
            switch (i.intValue()){
                case R.id.chkFavMon:
                    favDays[0] = !favDays[0];
                    break;
                case R.id.chkFavTue:
                    favDays[1] = !favDays[1];
                    break;
                case R.id.chkFavWed:
                    favDays[2] = !favDays[2];
                    break;
                case R.id.chkFavThu:
                    favDays[3] = !favDays[3];
                    break;
                case R.id.chkFavFri:
                    favDays[4] = !favDays[4];
                    break;
                case R.id.chkFavSat:
                    favDays[5] = !favDays[5];
                    break;
                case R.id.chkFavSun:
                    favDays[6] = !favDays[6];
                    break;
            }
        }

        for(Integer i : checkedSports){
            switch (i.intValue()){
                case R.id.chkFavSoccer:
                    favSports[0] = !favSports[0];
                    break;
                case R.id.chkFavFutsal:
                    favSports[1] = !favSports[1];
                    break;
                case R.id.chkFavBaseball:
                    favSports[2] = !favSports[2];
                    break;
                case R.id.chkFavBasketball:
                    favSports[3] = !favSports[3];
                    break;
                case R.id.chkFavBadminton:
                    favSports[4] = !favSports[4];
                    break;
                case R.id.chkFavCycle:
                    favSports[5] = !favSports[5];
                    break;
            }
        }

    }


    //프로필 내용 전송
    private void sendProfileData(ProfileDTO profileDTO) {

        //chekcDays와 checkSports의 값으로 favDays, favSoprt 배열 값 변경
        favReflectAtArray();

        //선호 요일, 종목 값 체크한대로 반영해서 전송
        profileDTO.setFavMon(favDays[0]);
        profileDTO.setFavTue(favDays[1]);
        profileDTO.setFavWed(favDays[2]);
        profileDTO.setFavThu(favDays[3]);
        profileDTO.setFavFri(favDays[4]);
        profileDTO.setFavSat(favDays[5]);
        profileDTO.setFavSun(favDays[6]);

        profileDTO.setFavSoccer(favSports[0]);
        profileDTO.setFavFutsal(favSports[1]);
        profileDTO.setFavBaseball(favSports[2]);
        profileDTO.setFavBasketball(favSports[3]);
        profileDTO.setFavBadminton(favSports[4]);
        profileDTO.setFavCycle(favSports[5]);

        //프로필 정보 전송
        retrofitUtil.getRetrofitAPI().setMyProfile(profileDTO).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                Log.d("프로필", "onResponse");
                if (response.isSuccessful()) {
                    if (response.body()) {
                        Log.d("프로필", "응답 true");
                        Toast.makeText(getApplicationContext(), "프로필 업데이트 완료", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent.putExtra("userId", profileDTO.getUserID());
                        startActivity(intent);
                        finish();

                    } else {
                        Log.d("프로필", "응답 false");
                        Toast.makeText(getApplicationContext(), "프로필 업데이트 실패하였습니다. 닉네임 중복", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.d("프로필", "onFailure");
                Log.d("프로필", t.getMessage());

            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btnEnter) {
                retrofitUtil.setToken(preferenceUtil.getString("accessToken"));
                Log.d("프로필", preferenceUtil.getString("accessToken"));
                //여기서 서버로 요청
                ProfileDTO profileDTO = new ProfileDTO();

                profileDTO.setUserID(preferenceUtil.getString("userId"));
                profileDTO.setNickname(edtNickname.getText().toString());
                profileDTO.setIntroduce(edtProfileMsg.getText().toString());
                profileDTO.setRegion(tvRegion.getText().toString());

                SharedPreferences sharedPreferences= getSharedPreferences("userInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putString("userNickname",edtNickname.getText().toString()); // key,value 형식으로 저장
                editor.commit();

                //이미지가 있으면 이미지 전송
                if (imgUri != null) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), getRealFile(imgUri));
                    MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

                    retrofitUtil.getRetrofitAPI().uploadImg(body).enqueue(new Callback<ApiResponseDTO>() {
                        @Override
                        public void onResponse(Call<ApiResponseDTO> call, Response<ApiResponseDTO> response) {
                            if (response.isSuccessful()) {
                                JSONObject resultBody = new JSONObject((Map) response.body().getResult());
                                try {
                                    Log.d("이미지", resultBody.getString("image"));
                                    profileDTO.setImage(resultBody.getString("image"));
                                    sendProfileData(profileDTO);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponseDTO> call, Throwable t) {
                            Log.d("이미지", "onFailure");
                            Log.d("이미지", t.getMessage());
                        }
                    });

                    //사진 없을때
                } else {
                    //따로 선택한 사진 없으면 기존 사진 그대로 적용
                    if(beforeProfile != null){
                        profileDTO.setImage(beforeProfile.getImage());
                    }
                    sendProfileData(profileDTO);
                }


                //이미지 클릭했을때는 사진첩 열리면서 이미지 선택 가능하도록
            } else if (view == imgProfile) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                activityResultLauncher.launch(intent);


                //위치 정보 가져오기
            } else if (view == btnRegionLoad) {
                checkGPS();
            }
        }
    };

    //칩그룹 리스너
    private ChipGroup.OnCheckedStateChangeListener setOnCheckedStateChangeListener = new ChipGroup.OnCheckedStateChangeListener() {
        @Override
        public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
            switch (group.getId()){
                case R.id.chipGroupFavDay:
                    checkedDays = checkedIds;
                    break;
                case R.id.chipGroupFavSport:
                    checkedSports = checkedIds;
                    break;
            }

        }
    };


    //이미지 업로드전 경로 가져옴
    private File getRealFile(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        if(uri == null) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        Cursor cursor = getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if(cursor == null || cursor.getColumnCount() <1 ) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        String path = cursor.getString(column_index);

        if(cursor != null) {
            cursor.close();
            cursor = null;
        }

        return new File(path);
    }

}
