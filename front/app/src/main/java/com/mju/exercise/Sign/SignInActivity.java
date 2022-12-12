package com.mju.exercise.Sign;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.mju.exercise.Domain.ApiResponseDTO;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.Domain.SignInDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText edtId, edtPw;
    private Button btnLogin, btnSignUp, btnForgetPassword;

    private RetrofitUtil retrofitUtil;
    private PreferenceUtil preferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        init();
    }

    private void init(){
        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        retrofitUtil = RetrofitUtil.getInstance();

        edtId = (TextInputEditText) findViewById(R.id.signInId);
        edtPw = (TextInputEditText) findViewById(R.id.signInPw);

        btnLogin = (Button) findViewById(R.id.btnSignIn);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnForgetPassword = (Button) findViewById(R.id.btnForgetPassword);

        btnLogin.setOnClickListener(onClickListener);
        btnSignUp.setOnClickListener(onClickListener);
        btnForgetPassword.setOnClickListener(onClickListener);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view == btnLogin){
                //이미 토큰 발행된 사용자일 경우
                //토큰이 유효한지 체크하고 바로 화면 전환
                if(!preferenceUtil.getString("accessToken").equals("")){
                    Log.d("로그인", preferenceUtil.getString("accessToken"));
                    retrofitUtil.getRetrofitAPI().tokenCheck().enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            if(response.isSuccessful()){
                                //로그인 처리 됨
                                if(response.body()){
                                    finish();
                                }else{
                                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {

                        }
                    });


                }else {
                    //accessToken이 ""이면 로그인을 해야하는 사용자임

                    SignInDTO signInDTO = new SignInDTO();
                    signInDTO.setUserId(edtId.getText().toString());
                    signInDTO.setUserPw(edtPw.getText().toString());


                        retrofitUtil.getRetrofitAPI().login(signInDTO).enqueue(new Callback<ApiResponseDTO>() {
                            @Override
                            public void onResponse(Call<ApiResponseDTO> call, Response<ApiResponseDTO> response) {
                                Log.d("로그인", "응답");
                                if (response.isSuccessful()) {
                                    Log.d("로그인", "성공");
                                    Log.d("로그인", String.valueOf(response.body().getCode()));

                                    if (response.body().getCode() == 200) {
                                        try {

                                            JSONObject resultBody = new JSONObject((Map) response.body().getResult());

                                            preferenceUtil.setString("refreshIdx", resultBody.getString("refreshIdx"));
                                            preferenceUtil.setString("accessToken", resultBody.getString("accessToken"));
                                            preferenceUtil.setString("userId", edtId.getText().toString());

                                            //닉네임 가져오기
                                            loadProfileIngo(edtId.getText().toString());

                                            retrofitUtil.getRetrofitAPI().getUserIndexByUserId(preferenceUtil.getString("userId")).enqueue(new Callback<Long>() {
                                                @Override
                                                public void onResponse(Call<Long> call, Response<Long> response) {
                                                    if(response.isSuccessful()){
                                                        Long userIdx = response.body();
                                                        preferenceUtil.setString("userIdx", userIdx.toString());
                                                    }else {
                                                        Toast.makeText(getApplicationContext(), "로그인 오류", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<Long> call, Throwable t) {

                                                }
                                            });

                                            finish();
                                        } catch (NullPointerException e) {
                                            Log.d("로그인", "이미 토큰이 발급된 사용자");
                                            finish();
                                        } catch (JSONException e) {
                                            Log.d("로그인", "json 에러");
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }

                            @Override
                            public void onFailure(Call<ApiResponseDTO> call, Throwable t) {

                            }
                        });

                }


            }else if(view == btnSignUp){
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }else if(view == btnForgetPassword){
                Intent intent = new Intent(getApplicationContext(), ForgetPwActivity.class);
                startActivity(intent);
            }
        }


    };

    private void loadProfileIngo(String userId){
        retrofitUtil.getRetrofitAPI().getUserProfile(userId).enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                if(response.isSuccessful()){
                    preferenceUtil.setString("nickname", response.body().getNickname());

                }
            }

            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {

            }
        });

    }
}
