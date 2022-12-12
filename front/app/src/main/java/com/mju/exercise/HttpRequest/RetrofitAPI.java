package com.mju.exercise.HttpRequest;

import com.mju.exercise.Domain.ApiResponseDTO;
import com.mju.exercise.Domain.MatchingDTO;
import com.mju.exercise.Domain.OpenMatchDTO;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.Domain.SendNotiDTO;
import com.mju.exercise.Domain.SignInDTO;
import com.mju.exercise.Domain.SignUpDTO;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RetrofitAPI {

    //회원가입
    @Headers("Content-Type: application/json")
    @POST("/api/user/signUp")
    Call<Boolean> signUp(@Body SignUpDTO signUpDTO);
    //프로필 설정
    @Headers({"Content-Type: application/json"})
    @POST("/api/user/setMyProfile")
    Call<Boolean> setMyProfile(@Body ProfileDTO profileDTO);
    //프로필 가져오기
    @Headers({"Content-Type: application/json"})
    @GET("/api/user/getUserProfile/{userId}")
    Call<ProfileDTO> getUserProfile(@Path(value="userId", encoded = true) String userId);
    //닉에임으로 프로필 이미지만 가져오기
    @Headers({"Content-Type: application/json"})
    @GET("/api/user/getUserProfileImgByNickName/{nickName}")
    Call<List<String>> getUserProfileImgByNickName(@Path(value="nickName", encoded = true) String nickName);
    @Headers({"Content-Type: application/json"})
    @GET("/api/user/getUserIdByNickName/{nickName}")
    Call<String> getUserIdByNickName(@Path(value = "nickName", encoded = true) String nickName);

    @Headers({"Content-Type: application/json"})
    @GET("/api/user/getUserIndexByUserId/{userId}")
    Call<Long> getUserIndexByUserId(@Path(value="userId", encoded = true) String userId);

    //이미지 업로드
    @Multipart
    @POST("/api/user/upload/image")
    Call<ApiResponseDTO> uploadImg(@Part MultipartBody.Part imgFile);
    //이미지 다운로드
    @Multipart
    @GET("/api/user/getProfileImg/{imgPath}")
    Call<MultipartBody.Part> getProfileImg(@Path(value="imgPath",encoded = true) String imgPath);


    //로그인
    @Headers("Content-Type: application/json")
    @POST("/api/auth/login")
    Call<ApiResponseDTO> login(@Body SignInDTO signInDTO);
    @GET("/api/auth/tokenCheck")
    Call<Boolean> tokenCheck();
    //비밀번호 변경
    @Headers("Content-Type: application/json")
    @POST("/api/user/changePw/{userId}/{newPw}")
    Call<Boolean> changePw(@Path(value = "userId", encoded = true) String userId, @Path(value = "newPw", encoded = true) String newPw);


    //오픈매치 가져오기 종목별
    @Headers("Content-Type: application/json")
    @GET("/api/match/openMatchList/{sportType}")
    Call<List<OpenMatchDTO>> loadOpenMatchesSportType(@Path(value = "sportType", encoded = true) String sportType);
    //오픈매치 가져오기 전부
    @Headers("Content-Type: application/json")
    @GET("/api/match/openMatchList")
    Call<List<OpenMatchDTO>> loadOpenMatchesAll();
    //내가 만든 오픈매치만 가져오기
    @Headers("Content-Type: application/json")
    @GET("/api/match/openMatchByMe/{userIdx}")
    Call<List<OpenMatchDTO>> loadOpenMatchesCreatedByMe(@Path(value = "userIdx", encoded = true) Long userIdx);
    //내가 참여중인 오픈매치만 가져오기
    @Headers("Content-Type: application/json")
    @GET("/api/match/joinedOpenMatch/{userIdx}")
    Call<List<OpenMatchDTO>> loadOpenMatchesJoined(@Path(value = "userIdx", encoded = true) Long userIdx);


    //오픈매치 생성
    @Headers("Content-Type: application/json")
    @POST("/api/match/openMatch")
    Call<OpenMatchDTO> openMatch(@Body OpenMatchDTO openMatchDTO);
    //오픈매치 참여
    @Headers("Content-Type: application/json")
    @POST("/api/match/joinMatch")
    Call<Long> joinMatch(@Body MatchingDTO matchingDTO);
    //오픈매치 삭제
    @Headers("Content-Type: application/json")
    @DELETE("/api/match/delete/{openMatchIdx}")
    Call<Boolean> delete(@Path(value = "openMatchIdx", encoded = true) Long openMatchIdx);
    //오픈매치 떠나기
    @Headers("Content-Type: application/json")
    @POST("/api/match/leaveMatch")
    Call<Boolean> leaveMatch(@Body MatchingDTO matchingDTO);
    //오픈매치 삭제시 참가중이던 유저들도 모두 떠나기 처리
    @Headers("Content-Type: application/json")
    @DELETE("/api/match/leaveAllMatchUser/{openMatchIdx}")
    Call<Boolean> leaveAllMatchUser(@Path(value = "openMatchIdx", encoded = true) Long openMatchIdx);


    //현재 오픈매치에 참여중인 모든 유저 프로필 정보 가져오기
    @Headers("Content-Type: application/json")
    @GET("/api/match/joinedUserProfiles/{openMatchIdx}")
    Call<List<ProfileDTO>> getJoinedUserProfiles(@Path(value = "openMatchIdx", encoded = true) Long openMatchIdx);


    //알림 발송 API
    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AAAAiy71WHw:APA91bFQ4vN6FiUowug0bHUOAU4olEfSaBV93j8JlU5p-gShVNC4oAAecNRx7Oclw456sqWnoz4mRLIMCtEjIJ5gdMh3wdHgMxA8oSFKJH5HB5_OF34myeNtY-zIFNLJ4ke590AUyO7K"
    })
    @POST("https://fcm.googleapis.com/fcm/send")
    Call<Void> sendNoti(@Body SendNotiDTO sendNotiDTO);

}
