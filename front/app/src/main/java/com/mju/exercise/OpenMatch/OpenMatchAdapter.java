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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.mju.exercise.Calendar.DBLoader;
import com.mju.exercise.ChatActivity;
import com.mju.exercise.Domain.MatchingDTO;
import com.mju.exercise.Domain.OpenMatchDTO;
import com.mju.exercise.Domain.ProfileDTO;
import com.mju.exercise.Domain.SendNotiDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.PopupMapActivity;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.Profile.SmallProfileAdapter;
import com.mju.exercise.R;
import com.skydoves.expandablelayout.ExpandableLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenMatchAdapter extends ArrayAdapter implements AdapterView.OnItemClickListener{

    private Context mContext;
    private ArrayList<OpenMatchDTO> list;
    RetrofitUtil retrofitUtil;
    PreferenceUtil preferenceUtil;
    private FirebaseDatabase firebaseDatabase;
    private RootViewListener rootViewListener;
    private DBLoader memoDB;


    public OpenMatchAdapter(@NonNull Context context, @NonNull ArrayList list) {
        super(context, 0, list);
        this.mContext = context;
        this.list = list;

        retrofitUtil = RetrofitUtil.getInstance();
        preferenceUtil = PreferenceUtil.getInstance(context);
        memoDB = new DBLoader(context);
    }

    //?????? ???????????? ????????? ???????????? ???????????? ??????
    public void setRootViewListener(RootViewListener rootViewListener) {
        this.rootViewListener = rootViewListener;
    }
    public interface RootViewListener{
        void rootViewDelNotify(OpenMatchDTO openMatchDTO);
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

        public Button btnDetailOnMap, btnDetailClick, btnChatJoin;

        public Double myLat, myLng;
        public Double mapLat, mapLng;
        public Double distanceToMe;

        //?????? ????????? ??????
        public ArrayList<ProfileDTO> profileDTOs;
        public RecyclerView customListView;
        public SmallProfileAdapter smallProfileAdapter;

        //???????????? ??????
        public boolean isCanJoin = true;
        //????????? ??????????????? ??????
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

        //????????? ??????
        viewHolder.tvSubect = (TextView) convertView.findViewById(R.id.omSubject);
        viewHolder.tvSportType = (ImageView) convertView.findViewById(R.id.omSportType);
        viewHolder.tvPersonnel = (TextView) convertView.findViewById(R.id.omPersonnel);
        viewHolder.tvPlayDateTime = (TextView) convertView.findViewById(R.id.omPlayDateTime);
        viewHolder.tvDistanceToMe = (TextView) convertView.findViewById(R.id.omDistanceToMe);


        //????????? ??????
        viewHolder.tvArticle = (TextView) expandableLayout.secondLayout.findViewById(R.id.detailArticle);
        viewHolder.btnDetailOnMap = (Button) convertView.findViewById(R.id.detailOnMap);
        viewHolder.btnDetailClick = (Button) convertView.findViewById(R.id.detailJoin);
        viewHolder.btnChatJoin = (Button) convertView.findViewById(R.id.ChatJoin);
        viewHolder.profileDTOs = new ArrayList<>();
        viewHolder.customListView = (RecyclerView) convertView.findViewById(R.id.detailProfileList);

        //????????? ?????? ?????? ???
        final OpenMatchDTO openMatchDTO = (OpenMatchDTO) list.get(position);

        //?????? ??????????????? ??????
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

        //????????? ?????? ????????? ???????????? ????????????
        if(preferenceUtil.getString("userId").equals("") || preferenceUtil.getString("userId") == null){
            viewHolder.isCanJoin = false;
            viewHolder.btnDetailClick.setText("????????? ??????");
            viewHolder.btnDetailClick.setEnabled(false);
            viewHolder.btnChatJoin.setText("????????? ??????");
            viewHolder.btnChatJoin.setEnabled(false);
        }else{
            //?????? ????????? ????????? ??? ????????? ?????? ??????
            viewHolder.btnDetailClick.setText("????????????");
            viewHolder.btnDetailClick.setEnabled(true);
            viewHolder.btnChatJoin.setText("????????? ????????????");
            viewHolder.btnChatJoin.setEnabled(true);

            viewHolder.btnChatJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    for (ProfileDTO userNick : viewHolder.profileDTOs){
//                        Log.i("HH_LOG", "onResponse: smallp = "+ userNick.getNickname());
//                    }
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("groupTitle", String.valueOf(openMatchDTO.getId()));
                    intent.putExtra("openMatchName", openMatchDTO.getSubject());
                    getContext().startActivity(intent);
                }
            });

            viewHolder.btnDetailClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("???????????? ??????").setMessage("????????? ???????????? ???????????? ????????? ???????????????.")
                            .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    //??????????????? ???????????? ?????? ????????? ??????
                                    if(openMatchDTO.getOpenMatchPw() != null){
                                        EditText editText = new EditText(getContext());
                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle("???????????? ??????").setMessage("??? ?????? ??????????????? ??????????????? ???????????????.")
                                                .setView(editText)
                                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if(editText.getText().toString() != null && !editText.getText().toString().equals("")){
                                                            if(openMatchDTO.getOpenMatchPw() == Integer.parseInt(editText.getText().toString())){
                                                                Toast.makeText(getContext(), "???????????? ??????", Toast.LENGTH_SHORT).show();

                                                                //?????? ??????
                                                                MatchingDTO matchingDTO = new MatchingDTO();
                                                                matchingDTO.setOpenMatchId(openMatchDTO.getId());
                                                                Long userIdx = Long.valueOf(preferenceUtil.getString("userIdx"));
                                                                // -1?????? ???????????? ?????? ?????????(?????? ??????). ???????????? ????????????
                                                                if(userIdx == -1l || userIdx == null){
                                                                    Toast.makeText(mContext, "?????? ??????", Toast.LENGTH_SHORT).show();
                                                                    return;
                                                                }
                                                                matchingDTO.setUserIndex(userIdx);
                                                                retrofitUtil.getRetrofitAPI().getUserProfile(preferenceUtil.getString("userId")).enqueue(new Callback<ProfileDTO>() {
                                                                    @Override
                                                                    public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                                                                        if(response.isSuccessful()){
                                                                            if(response.body() != null){
                                                                                Log.d("???????????????", "???????????? ?????? ??????");
                                                                                retrofitUtil.getRetrofitAPI().joinMatch(matchingDTO).enqueue(new Callback<Long>() {
                                                                                    @Override
                                                                                    public void onResponse(Call<Long> call, Response<Long> response) {
                                                                                        if(response.isSuccessful()){
                                                                                            if(response.body() == -1l){
                                                                                                Toast.makeText(mContext, "?????? ????????? ????????????", Toast.LENGTH_SHORT).show();
                                                                                            }else {
                                                                                                Toast.makeText(mContext, "?????? ??????", Toast.LENGTH_SHORT).show();
                                                                                                // ????????? ??????
                                                                                                createMemo(openMatchDTO.getSubject(), openMatchDTO.getArticle(), openMatchDTO.getPlayDateTime());
                                                                                                // ?????? db??? ??????
                                                                                                updateInNotiDB(openMatchDTO);
                                                                                            }
                                                                                            notifyDataSetChanged();

                                                                                        }else {
                                                                                            Toast.makeText(mContext, "?????? ??????", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onFailure(Call<Long> call, Throwable t) {

                                                                                    }
                                                                                });
                                                                            }
                                                                        }else {
                                                                            Toast.makeText(mContext, "????????? ?????? ??? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                                                            Log.d("???????????????", "???????????? ?????? ??????");
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<ProfileDTO> call, Throwable t) {

                                                                    }
                                                                });
                                                            }else {
                                                                Toast.makeText(getContext(), "???????????? ?????????", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }else {
                                                            new MaterialAlertDialogBuilder(getContext())
                                                                    .setTitle("????????? ????????????").setMessage("???????????? ????????? ?????? ???????????????.")
                                                                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                                        }
                                                                    }).show();

                                                        }

                                                    }
                                                })
                                                .show();
                                    }else {
                                            //?????? ??????
                                            MatchingDTO matchingDTO = new MatchingDTO();
                                            matchingDTO.setOpenMatchId(openMatchDTO.getId());
                                            Long userIdx = Long.valueOf(preferenceUtil.getString("userIdx"));
                                            // -1?????? ???????????? ?????? ?????????(?????? ??????). ???????????? ????????????
                                            if(userIdx == -1l || userIdx == null){
                                                Toast.makeText(mContext, "?????? ??????", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            matchingDTO.setUserIndex(userIdx);
                                            retrofitUtil.getRetrofitAPI().getUserProfile(preferenceUtil.getString("userId")).enqueue(new Callback<ProfileDTO>() {
                                                @Override
                                                public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                                                    if(response.isSuccessful()){
                                                        if(response.body() != null){
                                                            Log.d("???????????????", "???????????? ?????? ??????");
                                                            retrofitUtil.getRetrofitAPI().joinMatch(matchingDTO).enqueue(new Callback<Long>() {
                                                                @Override
                                                                public void onResponse(Call<Long> call, Response<Long> response) {
                                                                    if(response.isSuccessful()){
                                                                        if(response.body() == -1l){
                                                                            Toast.makeText(mContext, "?????? ????????? ????????????", Toast.LENGTH_SHORT).show();
                                                                        }else {
                                                                            Toast.makeText(mContext, "?????? ??????", Toast.LENGTH_SHORT).show();
                                                                            // ????????? ??????
                                                                            createMemo(openMatchDTO.getSubject(), openMatchDTO.getArticle(), openMatchDTO.getPlayDateTime());
                                                                            // ?????? DB??? ??????
                                                                            updateInNotiDB(openMatchDTO);
                                                                        }
                                                                        notifyDataSetChanged();

                                                                    }else {
                                                                        Toast.makeText(mContext, "?????? ??????", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<Long> call, Throwable t) {

                                                                }
                                                            });
                                                        }
                                                    }else {
                                                        Toast.makeText(mContext, "????????? ?????? ??? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                                        Log.d("???????????????", "???????????? ?????? ??????");
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
        //??????????????? ????????? ?????? ????????? ????????? ??????
        iconReflect(viewHolder, openMatchDTO.getSportType());
        //?????? ?????? ????????? ?????? ????????? ????????? ?????????
        retrofitUtil.getRetrofitAPI().getJoinedUserProfiles(openMatchDTO.getId()).enqueue(new Callback<List<ProfileDTO>>() {
            @Override
            public void onResponse(Call<List<ProfileDTO>> call, Response<List<ProfileDTO>> response) {
                if(response.isSuccessful()){
                    int cnt = response.body().size();

                    viewHolder.tvPersonnel.setText(String.valueOf("?????? ??????:" + String.valueOf(cnt) + "/" + openMatchDTO.getPersonnel()));

                    //????????? ??????
                    if(openMatchDTO.getPersonnel() != null){

                        //???????????? ?????? ??? ????????? ??????????????? disabled ???
                        if(cnt >= openMatchDTO.getPersonnel()){
                            viewHolder.isCanJoin = false;
                            viewHolder.btnDetailClick.setText("?????? ??????");
                            viewHolder.btnDetailClick.setEnabled(false);
                        }
                            //?????? ????????? ?????? ????????? ????????? ????????? ??????
                            for(ProfileDTO profileDTO: response.body()){
                                if(profileDTO.getUserID().equals(preferenceUtil.getString("userId"))){
                                    viewHolder.btnDetailClick.setEnabled(true);
                                    viewHolder.btnDetailClick.setText("?????????");
                                    viewHolder.btnDetailClick.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            new MaterialAlertDialogBuilder(getContext())
                                                    .setTitle("???????????? ?????????").setMessage("?????? ??????????????????????")
                                                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                        }
                                                    })
                                                    .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            //????????? ?????? ??????
                                                            //???????????? ????????????, ?????????????????? ????????? ????????? ??????
                                                            leaveMatching(openMatchDTO.getId(), Long.valueOf(preferenceUtil.getString("userIdx")));
                                                            viewHolder.btnDetailClick.setText("????????????");
                                                            notifyDataSetChanged();
                                                            // ??????DB?????? ??????
                                                            deleteInNotiDB(openMatchDTO.getId());

                                                        }
                                                    }).show();


                                        }
                                    });
                                    break;
                                }
                            }
                            if(openMatchDTO.getOpenUserId().equals(preferenceUtil.getString("userId"))){
                                viewHolder.btnDetailClick.setEnabled(true);
                                //?????? ?????? ??????????????? ???????????? ????????? ?????????
                                viewHolder.btnDetailClick.setText("?????? ??????");
                                viewHolder.btnDetailClick.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle("???????????? ??????").setMessage("?????? ?????????????????????????")
                                                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                })
                                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        //????????????
                                                        retrofitUtil.getRetrofitAPI().delete(openMatchDTO.getId()).enqueue(new Callback<Boolean>() {
                                                            @Override
                                                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                if(response.isSuccessful()){
                                                                    if(response.body()){
                                                                        Toast.makeText(mContext, "????????????", Toast.LENGTH_SHORT).show();
                                                                        //?????? ????????? ??????
                                                                        leaveMatching(openMatchDTO.getId(), Long.valueOf(preferenceUtil.getString("userIdx")));
                                                                        //?????? ???????????? ???????????? ?????? ??????????????? ???????????????
                                                                        rootViewListener.rootViewDelNotify(openMatchDTO);

                                                                        retrofitUtil.getRetrofitAPI().leaveAllMatchUser(openMatchDTO.getId()).enqueue(new Callback<Boolean>() {
                                                                            @Override
                                                                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                if(response.isSuccessful()){
                                                                                    Log.d("????????????", "?????? ????????? ????????? ???????????? ??????");
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<Boolean> call, Throwable t) {

                                                                            }
                                                                        });


                                                                    }else {
                                                                        Toast.makeText(mContext, "????????? ?????? ??????", Toast.LENGTH_SHORT).show();
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

                    //???????????? ?????? ????????? ??????
                    viewHolder.profileDTOs = (ArrayList<ProfileDTO>) response.body();
                    viewHolder.smallProfileAdapter = new SmallProfileAdapter(getContext(), viewHolder.profileDTOs);
                    viewHolder.customListView.setAdapter(viewHolder.smallProfileAdapter);
                    viewHolder.customListView.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));

                    }

                }else {
                    viewHolder.tvPersonnel.setText(String.valueOf("?????? ??????:" + "?????? ??????/" + openMatchDTO.getPersonnel()));
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

            //?????? ?????? ????????? ?????? ????????????
            if(openMatchDTO.getLat() == null || openMatchDTO.getLng() == null){
                viewHolder.btnDetailOnMap.setEnabled(false);
                viewHolder.btnDetailOnMap.setText("?????? ??????");
                viewHolder.tvDistanceToMe.setText("????????? ??????: ?????? ??????");
            }else {
                viewHolder.mapLat = openMatchDTO.getLat();
                viewHolder.mapLng = openMatchDTO.getLng();

                viewHolder.distanceToMe = computeDistance(viewHolder.myLat, viewHolder.myLng, viewHolder.mapLat, viewHolder.mapLng);
                viewHolder.tvDistanceToMe.setText("????????? ??????: " + String.format("%.1f", convertMtoKM(viewHolder.distanceToMe)) + "km    ");
            }
        }else {
            viewHolder.btnDetailOnMap.setEnabled(true);
            viewHolder.tvDistanceToMe.setText("????????? ??????: ??? ??? ??????");
        }


        //????????? ??????
        // ????????????
        Log.d("?????????", "?????????: " + openMatchDTO.getArticle());
        if(openMatchDTO.getArticle() == null || openMatchDTO.getArticle().equals("")){
            viewHolder.tvArticle.setText("?????? ?????? ??????");
        }else {
            viewHolder.tvArticle.setText(openMatchDTO.getArticle());
        }


        if(openMatchDTO.getPlayDateTime() == null){
            viewHolder.tvPlayDateTime.setText("?????? ??????");
        }


        return convertView;

    }

    private void leaveMatching(Long openMatchIdx, Long userIdx){

        MatchingDTO matchingDTO = new MatchingDTO();
        matchingDTO.setOpenMatchId(openMatchIdx);
        matchingDTO.setUserIndex(userIdx);

        Log.d("???????????????", "midx: " + openMatchIdx + ", uidx: " + userIdx);

        retrofitUtil.getRetrofitAPI().leaveMatch(matchingDTO).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful()){
                    if(response.body()){
                        Log.d("???????????????", "?????? ??????");
                        Toast.makeText(getContext(), "??????????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getContext(), "?????? ??????. ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }
                    notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });
    }
    // ???????????? ????????? ?????????
    private void iconReflect(ViewHolder viewHolder, String type){
        if(type.equals("??????")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_football);
        }else if(type.equals("??????")) {
            viewHolder.tvSportType.setImageResource(R.drawable.ic_futsal);
        }else if(type.equals("??????")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_basketball);
        }else if(type.equals("??????")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_baseball);
        }else if(type.equals("????????????")){
            viewHolder.tvSportType.setImageResource(R.drawable.ic_badminton);
        }else if(type.equals("?????????")){
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

    //???????????? ????????? ?????? ?????? ??????
    private void createMemo(String subject, String article, String openMatchDate){

        LocalDateTime localDateTime = null;
        LocalDate localDate = null;
        Date date = null;
        Long l = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if(openMatchDate != null){
                localDateTime = LocalDateTime.parse(openMatchDate);
                localDate = localDateTime.toLocalDate();
                date = java.sql.Date.valueOf(String.valueOf(localDate));
                l = date.getTime();
            }else {
                //?????? ????????? ?????? 1????????? ??????
                l = 1l;
            }

            memoDB.save("[?????????]" + subject, "???????????? ????????? ?????? ????????? ???????????????. \n\n??????: " + article, l);
        }
    }

    // ???????????? ????????? ??????DB??? ??????
    private void updateInNotiDB(OpenMatchDTO openMatchDTO){
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("Notification").child("OpenMatches").child(openMatchDTO.getId().toString())
                .child(preferenceUtil.getString("userId")).setValue("true");

        // ?????? ??????
        firebaseDatabase.getReference().child("Notification").child("OpenMatches").child(openMatchDTO.getId().toString()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                   for(DataSnapshot snapshot: task.getResult().getChildren()){
                       Log.d("??????", "???????????? ??????: " + snapshot.getKey());
                       String userId = (String) snapshot.getKey();

                       firebaseDatabase.getReference().child("Notification").child("ALL_USERS").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<DataSnapshot> task) {
                               if(task.isSuccessful()){
                                   Log.d("??????", "???????????? ?????? ??????: " + task.getResult().getValue());
                                   String userNotiToken = (String) task.getResult().getValue();

                                   try {
                                       sendNoti(openMatchDTO.getSubject(), userId, userNotiToken);
                                   } catch (JSONException e) {
                                       e.printStackTrace();
                                   }
                               }
                           }
                       });
                   }
                }
            }
        });


    }
    //?????? ??????????????? ???????????? ?????? ??????????????? ?????? ??????
    private void sendNoti(String openMatchName, String userId, String userNotiToken) throws JSONException {

        HashMap<String, String> innerJsonObject = new HashMap<>();
        innerJsonObject.put("title", "???????????? ??????");
        innerJsonObject.put("body", openMatchName + "??? ????????? ????????? ?????? ????????????.");

        SendNotiDTO sendNotiDTO = new SendNotiDTO();
        sendNotiDTO.setTo(userNotiToken);
        sendNotiDTO.setPriority("high");
        sendNotiDTO.setNotification(innerJsonObject);

        retrofitUtil.getRetrofitAPI().sendNoti(sendNotiDTO).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("??????", "????????????: " + response.code());
                if(response.isSuccessful()){
                    Log.d("??????", "?????? ?????????");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });

    }

    // ???????????? ???????????? ??????DB?????? ??????
    private void deleteInNotiDB(Long openMatchIdx){
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("Notification").child("OpenMatches").child(openMatchIdx.toString())
                .child(preferenceUtil.getString("userId")).setValue(null);
    }

}
