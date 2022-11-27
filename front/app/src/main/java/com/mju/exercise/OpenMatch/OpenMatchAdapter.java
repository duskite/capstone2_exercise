package com.mju.exercise.OpenMatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mju.exercise.Domain.MatchingDTO;
import com.mju.exercise.Domain.OpenMatchDTO;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.PopupMapActivity;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.Profile.SmallProfileAdapter;
import com.mju.exercise.R;
import com.skydoves.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenMatchAdapter extends ArrayAdapter implements AdapterView.OnItemClickListener{

    private Context mContext;
    private ArrayList<OpenMatchDTO> list;
    RetrofitUtil retrofitUtil;
    PreferenceUtil preferenceUtil;
    private RootViewListener rootViewListener;


    public OpenMatchAdapter(@NonNull Context context, @NonNull ArrayList list) {
        super(context, 0, list);
        this.mContext = context;
        this.list = list;

        retrofitUtil = RetrofitUtil.getInstance();
        preferenceUtil = PreferenceUtil.getInstance(context);
    }

    //루트 리스트뷰 데이터 변경되면 반영하기 위해
    public void setRootViewListener(RootViewListener rootViewListener) {
        this.rootViewListener = rootViewListener;
    }
    public interface RootViewListener{
        void rootViewNotify();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    class ViewHolder{
        public TextView tvSubect;
        public TextView tvArticle;
        public ImageView tvSportType;
        public TextView tvPersonnel;
        public TextView tvPlayDateTime;
        public TextView tvDistanceToMe;

        public Button btnDetailOnMap, btnDetailClick;

        public Double myLat, myLng;
        public Double mapLat, mapLng;
        public Double distanceToMe;

        //스몰 프로필 부분
        public ArrayList<ProfileDTO> profileDTOs;
        public RecyclerView customListView;
        public SmallProfileAdapter smallProfileAdapter;

        //참여가능 여부
        public boolean isCanJoin = true;
        //자신이 만든것인지 여부
        public boolean isMadeMe = false;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            convertView = layoutInflater.inflate(R.layout.open_match_item, parent, false);
        }

        ExpandableLayout expandableLayout = (ExpandableLayout) convertView.findViewById(R.id.exItem);
        expandableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(expandableLayout.isExpanded()){
                    expandableLayout.collapse();
                }else {
                    expandableLayout.expand();
                }
            }
        });

        viewHolder = new ViewHolder();

        //프리뷰 부분
        viewHolder.tvSubect = (TextView) convertView.findViewById(R.id.omSubject);
        viewHolder.tvSportType = (ImageView) convertView.findViewById(R.id.omSportType);
        viewHolder.tvPersonnel = (TextView) convertView.findViewById(R.id.omPersonnel);
        viewHolder.tvPlayDateTime = (TextView) convertView.findViewById(R.id.omPlayDateTime);
        viewHolder.tvDistanceToMe = (TextView) convertView.findViewById(R.id.omDistanceToMe);


        //디테일 부분
        viewHolder.tvArticle = (TextView) expandableLayout.secondLayout.findViewById(R.id.detailArticle);
        viewHolder.btnDetailOnMap = (Button) convertView.findViewById(R.id.detailOnMap);
        viewHolder.btnDetailClick = (Button) convertView.findViewById(R.id.detailJoin);
        viewHolder.profileDTOs = new ArrayList<>();
        viewHolder.customListView = (RecyclerView) convertView.findViewById(R.id.detailProfileList);

        //데이터 하나 뽑은 후
        final OpenMatchDTO openMatchDTO = (OpenMatchDTO) list.get(position);

        //내가 만든것인지 체크
        if(preferenceUtil.getString("userId").equals(openMatchDTO.getOpenUserId())){
            viewHolder.isMadeMe = true;
        }

        viewHolder.btnDetailOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double lat = openMatchDTO.getLat();
                Double lng = openMatchDTO.getLng();
                Intent intent = new Intent(getContext(), PopupMapActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("type", false);
                getContext().startActivity(intent);
            }
        });

        //로그인 안한 유저는 참여하지 못하도록
        if(preferenceUtil.getString("userId").equals("") || preferenceUtil.getString("userId") == null){
            viewHolder.isCanJoin = false;
            viewHolder.btnDetailClick.setText("로그인 필요");
            viewHolder.btnDetailClick.setEnabled(false);
        }else{
            //오픈 매치에 참가할 수 있도록 하는 기능
            viewHolder.btnDetailClick.setText("참여하기");
            viewHolder.btnDetailClick.setEnabled(true);
            viewHolder.btnDetailClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("오픈매치 참여").setMessage("참여를 원하시면 참여하기 버튼을 눌러주세요.")
                            .setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("참여하기", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    //비밀번호가 걸려있는 오픈 매치일 경우
                                    if(openMatchDTO.getOpenMatchPw() != null){
                                        EditText editText = new EditText(getContext());
                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle("비밀번호 필요").setMessage("이 방에 입장하려면 비밀번호가 필요합니다.")
                                                .setView(editText)
                                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if(editText.getText().toString() != null && !editText.getText().toString().equals("")){
                                                            if(openMatchDTO.getOpenMatchPw() == Integer.parseInt(editText.getText().toString())){
                                                                Toast.makeText(getContext(), "비밀번호 일치", Toast.LENGTH_SHORT).show();

                                                                //참여 처리
                                                                MatchingDTO matchingDTO = new MatchingDTO();
                                                                matchingDTO.setOpenMatchId(openMatchDTO.getId());
                                                                Long userIdx = Long.valueOf(preferenceUtil.getString("userIdx"));
                                                                // -1이면 조회되지 않는 유저임(없는 유저). 참가로직 안돌도록
                                                                if(userIdx == -1l || userIdx == null){
                                                                    Toast.makeText(mContext, "참가 실패", Toast.LENGTH_SHORT).show();
                                                                    return;
                                                                }
                                                                matchingDTO.setUserIndex(userIdx);
                                                                retrofitUtil.getRetrofitAPI().getUserProfile(preferenceUtil.getString("userId")).enqueue(new Callback<ProfileDTO>() {
                                                                    @Override
                                                                    public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                                                                        if(response.isSuccessful()){
                                                                            if(response.body() != null){
                                                                                Log.d("프로필제한", "프로필이 있는 유저");
                                                                                retrofitUtil.getRetrofitAPI().joinMatch(matchingDTO).enqueue(new Callback<Long>() {
                                                                                    @Override
                                                                                    public void onResponse(Call<Long> call, Response<Long> response) {
                                                                                        if(response.isSuccessful()){
                                                                                            if(response.body() == -1l){
                                                                                                Toast.makeText(mContext, "이미 참여한 오픈매치", Toast.LENGTH_SHORT).show();
                                                                                            }else {
                                                                                                Toast.makeText(mContext, "참여 완료", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                            notifyDataSetChanged();

                                                                                        }else {
                                                                                            Toast.makeText(mContext, "응답 없음", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onFailure(Call<Long> call, Throwable t) {

                                                                                    }
                                                                                });
                                                                            }
                                                                        }else {
                                                                            Toast.makeText(mContext, "프로필 생성 후 이용가능합니다.", Toast.LENGTH_SHORT).show();
                                                                            Log.d("프로필제한", "프로필이 없는 유저");
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<ProfileDTO> call, Throwable t) {

                                                                    }
                                                                });
                                                            }else {
                                                                Toast.makeText(getContext(), "비밀번호 불일치", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }else {
                                                            new MaterialAlertDialogBuilder(getContext())
                                                                    .setTitle("비공개 오픈매치").setMessage("비밀번호 입력후 참여 가능합니다.")
                                                                    .setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                                        }
                                                                    }).show();

                                                        }

                                                    }
                                                })
                                                .show();
                                    }else {
                                            //참여 처리
                                            MatchingDTO matchingDTO = new MatchingDTO();
                                            matchingDTO.setOpenMatchId(openMatchDTO.getId());
                                            Long userIdx = Long.valueOf(preferenceUtil.getString("userIdx"));
                                            // -1이면 조회되지 않는 유저임(없는 유저). 참가로직 안돌도록
                                            if(userIdx == -1l || userIdx == null){
                                                Toast.makeText(mContext, "참가 실패", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            matchingDTO.setUserIndex(userIdx);
                                            retrofitUtil.getRetrofitAPI().getUserProfile(preferenceUtil.getString("userId")).enqueue(new Callback<ProfileDTO>() {
                                                @Override
                                                public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                                                    if(response.isSuccessful()){
                                                        if(response.body() != null){
                                                            Log.d("프로필제한", "프로필이 있는 유저");
                                                            retrofitUtil.getRetrofitAPI().joinMatch(matchingDTO).enqueue(new Callback<Long>() {
                                                                @Override
                                                                public void onResponse(Call<Long> call, Response<Long> response) {
                                                                    if(response.isSuccessful()){
                                                                        if(response.body() == -1l){
                                                                            Toast.makeText(mContext, "이미 참여한 오픈매치", Toast.LENGTH_SHORT).show();
                                                                        }else {
                                                                            Toast.makeText(mContext, "참여 완료", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        notifyDataSetChanged();

                                                                    }else {
                                                                        Toast.makeText(mContext, "응답 없음", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<Long> call, Throwable t) {

                                                                }
                                                            });
                                                        }
                                                    }else {
                                                        Toast.makeText(mContext, "프로필 생성 후 이용가능합니다.", Toast.LENGTH_SHORT).show();
                                                        Log.d("프로필제한", "프로필이 없는 유저");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ProfileDTO> call, Throwable t) {

                                                }
                                            });
                                    }
                                }

                            }).show();

                }
            });
        }


        viewHolder.tvSubect.setText(openMatchDTO.getSubject());
        //운동종류에 따라서 다른 이미지 아이콘 적용
        iconReflect(viewHolder, openMatchDTO.getSportType());
        //해당 오픈 매치에 유저 얼마나 있는지 확인용
        retrofitUtil.getRetrofitAPI().getJoinedUserProfiles(openMatchDTO.getId()).enqueue(new Callback<List<ProfileDTO>>() {
            @Override
            public void onResponse(Call<List<ProfileDTO>> call, Response<List<ProfileDTO>> response) {
                if(response.isSuccessful()){
                    int cnt = response.body().size();

                    viewHolder.tvPersonnel.setText(String.valueOf("현재 인원:" + String.valueOf(cnt) + "/" + openMatchDTO.getPersonnel()));

                    //임시로 추가
                    if(openMatchDTO.getPersonnel() != null){

                        //모집인원 수가 다 채워진 오픈매치는 disabled 함
                        if(cnt >= openMatchDTO.getPersonnel()){
                            viewHolder.isCanJoin = false;
                            viewHolder.btnDetailClick.setText("참여 불가");
                            viewHolder.btnDetailClick.setEnabled(false);
                        }
                            //이미 참여한 오픈 매치는 나가기 버튼을 제공
                            for(ProfileDTO profileDTO: response.body()){
                                if(profileDTO.getUserID().equals(preferenceUtil.getString("userId"))){
                                    viewHolder.btnDetailClick.setEnabled(true);
                                    viewHolder.btnDetailClick.setText("나가기");
                                    viewHolder.btnDetailClick.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            new MaterialAlertDialogBuilder(getContext())
                                                    .setTitle("오픈매치 떠나기").setMessage("정말 떠나시겠습니까?")
                                                    .setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                        }
                                                    })
                                                    .setPositiveButton("떠나기", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            //나가기 기능 제공
                                                            //오픈매치 인덱스와, 유저인덱스를 가지고 나가기 처리
                                                            leaveMatching(openMatchDTO.getId(), Long.valueOf(preferenceUtil.getString("userIdx")));
                                                            viewHolder.btnDetailClick.setText("참여하기");
                                                            notifyDataSetChanged();

                                                        }
                                                    }).show();


                                        }
                                    });
                                    break;
                                }
                            }
                            if(openMatchDTO.getOpenUserId().equals(preferenceUtil.getString("userId"))){
                                viewHolder.btnDetailClick.setEnabled(true);
                                //내가 만든 오픈매치는 삭제하기 버튼이 뜨도록
                                viewHolder.btnDetailClick.setText("삭제 하기");
                                viewHolder.btnDetailClick.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle("오픈매치 삭제").setMessage("정말 삭제하시겠습니까?")
                                                .setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                })
                                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        //삭제처리
                                                        retrofitUtil.getRetrofitAPI().delete(openMatchDTO.getId()).enqueue(new Callback<Boolean>() {
                                                            @Override
                                                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                if(response.isSuccessful()){
                                                                    if(response.body()){
                                                                        Toast.makeText(mContext, "삭제완료", Toast.LENGTH_SHORT).show();

                                                                        //현재 디테일을 포함하고 있는 리스트뷰에 알려줘야함
                                                                        rootViewListener.rootViewNotify();
                                                                    }else {
                                                                        Toast.makeText(mContext, "오류로 삭제 실패", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<Boolean> call, Throwable t) {

                                                            }
                                                        });

                                                    }
                                                }).show();


                                    }
                                });
                            }

                    //오픈매치 스몰 프로필 부분
                    viewHolder.profileDTOs = (ArrayList<ProfileDTO>) response.body();
                    viewHolder.smallProfileAdapter = new SmallProfileAdapter(getContext(), viewHolder.profileDTOs);
                    viewHolder.customListView.setAdapter(viewHolder.smallProfileAdapter);
                    viewHolder.customListView.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));

                    }

                }else {
                    viewHolder.tvPersonnel.setText(String.valueOf("현재 인원:" + "로딩 오류/" + openMatchDTO.getPersonnel()));
                }
            }

            @Override
            public void onFailure(Call<List<ProfileDTO>> call, Throwable t) {

            }
        });


        viewHolder.tvPlayDateTime.setText(String.valueOf(openMatchDTO.getPlayDateTime()));
        if(!preferenceUtil.getString("lat").equals("") && !preferenceUtil.getString("lng").equals("")){
            viewHolder.myLat = Double.valueOf(preferenceUtil.getString("lat"));
            viewHolder.myLng = Double.valueOf(preferenceUtil.getString("lng"));

            //지도 내용 없으면 버튼 비활성화
            if(openMatchDTO.getLat() == null || openMatchDTO.getLng() == null){
                viewHolder.btnDetailOnMap.setEnabled(false);
                viewHolder.btnDetailOnMap.setText("위치 미정");
                viewHolder.tvDistanceToMe.setText("나와의 거리: 장소 미정");
            }else {
                viewHolder.mapLat = openMatchDTO.getLat();
                viewHolder.mapLng = openMatchDTO.getLng();

                viewHolder.distanceToMe = computeDistance(viewHolder.myLat, viewHolder.myLng, viewHolder.mapLat, viewHolder.mapLng);
                viewHolder.tvDistanceToMe.setText("나와의 거리: " + String.format("%.1f", convertMtoKM(viewHolder.distanceToMe)) + "km    ");
            }
        }else {
            viewHolder.btnDetailOnMap.setEnabled(true);
            viewHolder.tvDistanceToMe.setText("나와의 거리: 알 수 없음");
        }


        //디테일 부분
        // 상세내용
        Log.d("디테일", "아티클: " + openMatchDTO.getArticle());
        if(openMatchDTO.getArticle() == null || openMatchDTO.getArticle().equals("")){
            viewHolder.tvArticle.setText("상세 내용 없음");
        }else {
            viewHolder.tvArticle.setText(openMatchDTO.getArticle());
        }


        if(openMatchDTO.getPlayDateTime() == null){
            viewHolder.tvPlayDateTime.setText("날짜 미정");
        }


        return convertView;

    }

    private void leaveMatching(Long openMatchIdx, Long userIdx){

        MatchingDTO matchingDTO = new MatchingDTO();
        matchingDTO.setOpenMatchId(openMatchIdx);
        matchingDTO.setUserIndex(userIdx);

        Log.d("매치떠나기", "midx: " + openMatchIdx + ", uidx: " + userIdx);

        retrofitUtil.getRetrofitAPI().leaveMatch(matchingDTO).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful()){
                    if(response.body()){
                        Log.d("매치떠나기", "응답 받음");
                        Toast.makeText(getContext(), "오픈매치를 떠났습니다.", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getContext(), "오류 발생. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                    notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });
    }

    private void iconReflect(ViewHolder viewHolder, String type){
        if(type.equals("축구")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_football);
        }else if(type.equals("풋살")) {
            viewHolder.tvSportType.setImageResource(R.drawable.ic_futsal);
        }else if(type.equals("농구")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_basketball);
        }else if(type.equals("야구")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_baseball);
        }else if(type.equals("배드민턴")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_badminton);
        }else if(type.equals("사이클")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_cycle);
        }

    }

    private Double computeDistance(Double myLat, Double myLng, Double mapLat, Double mapLng){

        Double R = 6372.8 * 1000;

        Double dLat = Math.toRadians(mapLat - myLat);
        Double dLng = Math.toRadians(mapLng - myLng);
        Double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLng / 2), 2) * Math.cos(Math.toRadians(myLat)) * Math.cos(Math.toRadians(mapLat));
        Double c = 2 * Math.asin(Math.sqrt(a));

        return (Double) (R * c);
    }

    private double convertMtoKM(Double distance){
        double result = 0.0;
        result = distance / 1000;

        return result;
    }

}
