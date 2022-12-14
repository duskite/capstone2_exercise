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

    //????????? ????????? ????????? ???????????? ????????? ??????
    ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri imgUri;

    private PreferenceUtil preferenceUtil;
    private RetrofitUtil retrofitUtil;
    private String serverImgPath;

    //???????????? ????????? ?????? ?????????, ????????? ???????????? ?????? ?????? ?????????
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

        //????????? ???????????? ????????? ?????? ???????????? ????????? ????????????
        Intent intent = getIntent();
        if(intent.getSerializableExtra("profile") != null){
            beforeProfile = (ProfileDTO) intent.getSerializableExtra("profile");
            loadBeforeProfile();
        }
    }

    //????????? ?????? ????????? ???????????? ??????
    public void loadBeforeProfile(){
        edtNickname.setText(beforeProfile.getNickname());
        tvRegion.setText(beforeProfile.getRegion());

        String path = beforeProfile.getImage();
        String url = retrofitUtil.getBASE_URL_NONE_SLASH() + path;
        Log.d("???????????????", url);
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

    //?????? ?????? ????????????
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
                        Log.d("????????????", geocoderToStr(location));
                        tvRegion.setText(geocoderToStr(location));

                    }catch (IOException e){
                        Toast.makeText(getApplicationContext(), "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
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

    //????????? ????????? ???????????? ????????? ?????? ????????? ?????? ??????, ?????? ?????? boolean?????? ??????
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


    //????????? ?????? ??????
    private void sendProfileData(ProfileDTO profileDTO) {

        //chekcDays??? checkSports??? ????????? favDays, favSoprt ?????? ??? ??????
        favReflectAtArray();

        //?????? ??????, ?????? ??? ??????????????? ???????????? ??????
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

        //????????? ?????? ??????
        retrofitUtil.getRetrofitAPI().setMyProfile(profileDTO).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                Log.d("?????????", "onResponse");
                if (response.isSuccessful()) {
                    if (response.body()) {
                        Log.d("?????????", "?????? true");
                        Toast.makeText(getApplicationContext(), "????????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                        intent.putExtra("userId", profileDTO.getUserID());
                        startActivity(intent);
                        finish();

                    } else {
                        Log.d("?????????", "?????? false");
                        Toast.makeText(getApplicationContext(), "????????? ???????????? ?????????????????????. ????????? ??????", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.d("?????????", "onFailure");
                Log.d("?????????", t.getMessage());

            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btnEnter) {
                retrofitUtil.setToken(preferenceUtil.getString("accessToken"));
                Log.d("?????????", preferenceUtil.getString("accessToken"));
                //????????? ????????? ??????
                ProfileDTO profileDTO = new ProfileDTO();

                profileDTO.setUserID(preferenceUtil.getString("userId"));
                profileDTO.setNickname(edtNickname.getText().toString());
                profileDTO.setIntroduce(edtProfileMsg.getText().toString());
                profileDTO.setRegion(tvRegion.getText().toString());

                SharedPreferences sharedPreferences= getSharedPreferences("userInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putString("userNickname",edtNickname.getText().toString()); // key,value ???????????? ??????
                editor.commit();

                //???????????? ????????? ????????? ??????
                if (imgUri != null) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), getRealFile(imgUri));
                    MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

                    retrofitUtil.getRetrofitAPI().uploadImg(body).enqueue(new Callback<ApiResponseDTO>() {
                        @Override
                        public void onResponse(Call<ApiResponseDTO> call, Response<ApiResponseDTO> response) {
                            if (response.isSuccessful()) {
                                JSONObject resultBody = new JSONObject((Map) response.body().getResult());
                                try {
                                    Log.d("?????????", resultBody.getString("image"));
                                    profileDTO.setImage(resultBody.getString("image"));
                                    sendProfileData(profileDTO);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponseDTO> call, Throwable t) {
                            Log.d("?????????", "onFailure");
                            Log.d("?????????", t.getMessage());
                        }
                    });

                    //?????? ?????????
                } else {
                    //?????? ????????? ?????? ????????? ?????? ?????? ????????? ??????
                    if(beforeProfile != null){
                        profileDTO.setImage(beforeProfile.getImage());
                    }
                    sendProfileData(profileDTO);
                }


                //????????? ?????????????????? ????????? ???????????? ????????? ?????? ???????????????
            } else if (view == imgProfile) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                activityResultLauncher.launch(intent);


                //?????? ?????? ????????????
            } else if (view == btnRegionLoad) {
                checkGPS();
            }
        }
    };

    //????????? ?????????
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


    //????????? ???????????? ?????? ?????????
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
