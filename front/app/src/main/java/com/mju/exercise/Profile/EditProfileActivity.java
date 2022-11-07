package com.mju.exercise.Profile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mju.exercise.Domain.ApiResponseDTO;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    Button btnEnter;
    ImageView imgProfile;
    EditText edtNickname, edtProfileMsg;
    Spinner ddo, si, gu;
    CheckBox chkFavMon, chkFavTue, chkFavWed, chkFavThu, chkFavFri, chkFavSat, chkFavSun;
    CheckBox chkFavSoccer, chkFavFutsal, chkFavBaseball, chkFavBasketball, chkFavBadminton, chkFavCycle;

    //이미지 업로드 위해서 액티비티 결과값 체크
    ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri imgUri;

    private PreferenceUtil preferenceUtil;
    private RetrofitUtil retrofitUtil;
    private String serverImgPath;

    //선호하는 요일과 운동 체크용, 디비에 전송하기 전에 잠시 담아둠
    private boolean[] favDays = new boolean[7];
    private boolean[] favSports = new boolean[6];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        init();
    }

    public void init(){
        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        retrofitUtil = RetrofitUtil.getInstance();
        retrofitUtil.setToken(preferenceUtil.getString("accessToken"));

        btnEnter = (Button) findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(onClickListener);

        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(onClickListener);

        edtNickname = (EditText) findViewById(R.id.txtNickname);
        edtProfileMsg = (EditText) findViewById(R.id.txtProfileMsg);

        chkFavMon = (CheckBox) findViewById(R.id.chkFavMon);
        chkFavTue = (CheckBox) findViewById(R.id.chkFavTue);
        chkFavWed = (CheckBox) findViewById(R.id.chkFavWed);
        chkFavThu = (CheckBox) findViewById(R.id.chkFavThu);
        chkFavFri = (CheckBox) findViewById(R.id.chkFavFri);
        chkFavSat = (CheckBox) findViewById(R.id.chkFavSat);
        chkFavSun = (CheckBox) findViewById(R.id.chkFavSun);

        chkFavSoccer = (CheckBox) findViewById(R.id.chkFavSoccer);
        chkFavFutsal = (CheckBox) findViewById(R.id.chkFavFutsal);
        chkFavBaseball = (CheckBox) findViewById(R.id.chkFavBaseball);
        chkFavBasketball = (CheckBox) findViewById(R.id.chkFavBasketball);
        chkFavBadminton = (CheckBox) findViewById(R.id.chkFavBadminton);
        chkFavCycle = (CheckBox) findViewById(R.id.chkFavCycle);


        chkFavMon.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavTue.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavWed.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavThu.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavFri.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavSat.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavSun.setOnCheckedChangeListener(onCheckedChangeListener);

        chkFavSoccer.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavFutsal.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavBaseball.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavBasketball.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavBadminton.setOnCheckedChangeListener(onCheckedChangeListener);
        chkFavCycle.setOnCheckedChangeListener(onCheckedChangeListener);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData().getData() != null){
                imgUri = result.getData().getData();
                Glide.with(this).load(imgUri).into(imgProfile);
            }else{

            }
        });

    }

    //프로필 내용 전송
    private void sendProfileData(ProfileDTO profileDTO){
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
                if(response.isSuccessful()){
                    if(response.body()){
                        Log.d("프로필", "응답 true");
                        Toast.makeText(getApplicationContext(), "프로필 업데이트 완료", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                        startActivity(intent);
                        finish();

                    }else {
                        Log.d("프로필", "응답 false");
                        Toast.makeText(getApplicationContext(), "프로필 업데이트 실패!!!!", Toast.LENGTH_SHORT).show();
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
            if(view == btnEnter) {
                retrofitUtil.setToken(preferenceUtil.getString("accessToken"));
                Log.d("프로필", preferenceUtil.getString("accessToken"));
                //여기서 서버로 요청
                ProfileDTO profileDTO = new ProfileDTO();

                profileDTO.setUserID(preferenceUtil.getString("userId"));
                profileDTO.setNickname(edtNickname.getText().toString());
                profileDTO.setIntroduce(edtProfileMsg.getText().toString());


                //이미지가 있으면 이미지 전송
                if(imgUri != null){
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), getRealFile(imgUri));
                    MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

                    retrofitUtil.getRetrofitAPI().uploadImg(body).enqueue(new Callback<ApiResponseDTO>() {
                        @Override
                        public void onResponse(Call<ApiResponseDTO> call, Response<ApiResponseDTO> response) {
                            if(response.isSuccessful()){
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

                    //이미지 없을때
                }else{
                    sendProfileData(profileDTO);
                }


            //이미지 클릭했을때는 사진첩 열리면서 이미지 선택 가능하도록
            }else if(view == imgProfile){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                activityResultLauncher.launch(intent);

            }
        }
    };

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()){
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
