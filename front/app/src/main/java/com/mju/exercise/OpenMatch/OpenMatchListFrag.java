package com.mju.exercise.OpenMatch;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mju.exercise.Domain.OpenMatchDTO;
import com.mju.exercise.HttpRequest.RetrofitUtil;
import com.mju.exercise.R;
import com.mju.exercise.StatusEnum.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenMatchListFrag extends Fragment implements OpenMatchFilter, SwipeRefreshLayout.OnRefreshListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SPORT = "sport";

    // TODO: Rename and change types of parameters
    private int mSportType;

    ArrayList<OpenMatchDTO> openMatches;
    ListView customListView;
    RetrofitUtil retrofitUtil;
    OpenMatchAdapter openMatchAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<OpenMatchDTO> tmpList = new ArrayList<>();

    public OpenMatchListFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OpenMatchListFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static OpenMatchListFrag newInstance(int param1) {
        OpenMatchListFrag fragment = new OpenMatchListFrag();
        Bundle args = new Bundle();
        args.putInt(SPORT, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSportType = getArguments().getInt(SPORT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(openMatchAdapter != null){
            openMatchAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        retrofitUtil = RetrofitUtil.getInstance();
        openMatches = new ArrayList<>();
        loadOpenMatchesSportType(sportTypeToString(mSportType));

        View view = inflater.inflate(R.layout.fragment_open_match_list, container, false);

        customListView = (ListView) view.findViewById(R.id.listViewOpenMatchList);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        // Inflate the layout for this fragment
        return view;
    }


    public void loadOpenMatchesSportType(String sport){

        retrofitUtil.getRetrofitAPI().loadOpenMatchesSportType(sport).enqueue(new Callback<List<OpenMatchDTO>>() {
            @Override
            public void onResponse(Call<List<OpenMatchDTO>> call, Response<List<OpenMatchDTO>> response) {
                if(response.isSuccessful()){
                    openMatches = (ArrayList<OpenMatchDTO>) response.body();
                    openMatchAdapter = new OpenMatchAdapter(getContext(), openMatches);
                    openMatchAdapter.setRootViewListener(new OpenMatchAdapter.RootViewListener() {
                        @Override
                        public void rootViewDelNotify(OpenMatchDTO openMatchDTO) {
                            openMatches.remove(openMatchDTO);
                            openMatchAdapter.notifyDataSetChanged();
                        }
                    });
                    customListView.setAdapter(openMatchAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<OpenMatchDTO>> call, Throwable t) {

            }
        });
    }

    private String sportTypeToString(int mSportType){
        switch (mSportType){
            case R.id.btnSoccer:
                return "축구";
            case R.id.btnFutsal:
                return "풋살";
            case R.id.btnBaseball:
                return "야구";
            case R.id.btnBasketball:
                return "농구";
            case R.id.btnBadminton:
                return "배드민턴";
            case R.id.btnCycle:
                return "사이클";
        }

        return "없음";
    }


    @Override
    public void setFilter(Status.FilterTypeJoin filterTypeJoin, Status.FilterTypeDistance filterTypeDistance,
                          Status.FilterTypeDay filterTypeDay, Status.DistanceDiff distanceDiff,
                          Status.FavDayType favDayType,
                          LocalDateTime localDateTime) {

        //필터링할 오픈매치 없으면 그냥 리턴
        if(openMatches == null){
            return;
        }

        if(filterTypeJoin == Status.FilterTypeJoin.JOIN_DEFAULT && filterTypeDay == Status.FilterTypeDay.DAY_DEFAULT && filterTypeDistance == Status.FilterTypeDistance.DISTANCE_DEFAULT){
            //모두 디폴트면 기존 리스트 그대로 보여줌
            openMatchAdapter = new OpenMatchAdapter(getContext(), openMatches);
            openMatchAdapter.setRootViewListener(new OpenMatchAdapter.RootViewListener() {
                @Override
                public void rootViewDelNotify(OpenMatchDTO openMatchDTO) {
                    openMatches.remove(openMatchDTO);
                    openMatchAdapter.notifyDataSetChanged();
                }
            });
            customListView.setAdapter(openMatchAdapter);
        }else {
            ArrayList<OpenMatchDTO> newOpenMatches = (ArrayList<OpenMatchDTO>) openMatches.clone();
            if(filterTypeDistance != Status.FilterTypeDistance.DISTANCE_DEFAULT){

                //거리로 필터링
                if(filterTypeDistance == Status.FilterTypeDistance.DISTANCE_DIFFERENCE){
                    distanceInner(newOpenMatches, distanceDiff);
                }else if(filterTypeDistance == Status.FilterTypeDistance.DISTANCE_NEAR){
                    distanceSort(newOpenMatches);
                }

                //무언가 처리하면 임시값을 tmpList에 담아뒀다가 반영함
                newOpenMatches = (ArrayList<OpenMatchDTO>) tmpList.clone();
                tmpList.clear();
            }
            //거리 관련 필터링 한 거 없으면 기존 데이터 그대로 이용함
            if(filterTypeDay != Status.FilterTypeDay.DAY_DEFAULT){
                //날짜로 필터링
                if(filterTypeDay == Status.FilterTypeDay.DAY_FAVDAY){
                    dayFav(newOpenMatches, favDayType);
                }else if(filterTypeDay == Status.FilterTypeDay.DAY_NEAR){
                    daySort(newOpenMatches);
                }else if(filterTypeDay == Status.FilterTypeDay.DAY_PICK){
                    Log.d("필터특정날짜", "특정날짜 고름");
                    dayPick(newOpenMatches, localDateTime);
                }

                //무언가 처리하면 임시값을 tmpList에 담아뒀다가 반영함
                newOpenMatches = (ArrayList<OpenMatchDTO>) tmpList.clone();
                tmpList.clear();
            }

            //비동기로 처리되는 애임
            //참가 가능 여부로 필터링
            if(filterTypeJoin == Status.FilterTypeJoin.JOIN_CAN){
                canJoin(newOpenMatches);
                newOpenMatches = (ArrayList<OpenMatchDTO>) tmpList.clone();
                tmpList.clear();
            }
        }

    }



    public void dayFav(ArrayList<OpenMatchDTO> pastList, Status.FavDayType favDayType){
        FilterDataLoader filterDataLoader = new FilterDataLoader(getContext());
        filterDataLoader.setDataListener(new FilterDataLoader.DataLoadedListener() {
            @Override
            public void dataLoadComplete(ArrayList<OpenMatchDTO> list) {
                tmpList = (ArrayList<OpenMatchDTO>) list.clone();
                openMatchAdapter = new OpenMatchAdapter(getContext(), list);
                openMatchAdapter.setRootViewListener(new OpenMatchAdapter.RootViewListener() {
                    @Override
                    public void rootViewDelNotify(OpenMatchDTO openMatchDTO) {
                        list.remove(openMatchDTO);
                        openMatchAdapter.notifyDataSetChanged();
                    }
                });
                customListView.setAdapter(openMatchAdapter);
                openMatchAdapter.notifyDataSetChanged();
            }
        });
        filterDataLoader.getDataFavDay(pastList, favDayType);
    }
    public void dayPick(ArrayList<OpenMatchDTO> pastList, LocalDateTime localDateTime){
        FilterDataLoader filterDataLoader = new FilterDataLoader(getContext());
        filterDataLoader.setDataListener(new FilterDataLoader.DataLoadedListener() {
            @Override
            public void dataLoadComplete(ArrayList<OpenMatchDTO> list) {
                tmpList = (ArrayList<OpenMatchDTO>) list.clone();
                openMatchAdapter = new OpenMatchAdapter(getContext(), list);
                openMatchAdapter.setRootViewListener(new OpenMatchAdapter.RootViewListener() {
                    @Override
                    public void rootViewDelNotify(OpenMatchDTO openMatchDTO) {
                        list.remove(openMatchDTO);
                        openMatchAdapter.notifyDataSetChanged();
                    }
                });
                customListView.setAdapter(openMatchAdapter);
                openMatchAdapter.notifyDataSetChanged();
            }
        });
        filterDataLoader.getDataPickDay(pastList, localDateTime);
    }
    public void daySort(ArrayList<OpenMatchDTO> pastList){
        FilterDataLoader filterDataLoader = new FilterDataLoader(getContext());
        filterDataLoader.setDataListener(new FilterDataLoader.DataLoadedListener() {
            @Override
            public void dataLoadComplete(ArrayList<OpenMatchDTO> list) {
                tmpList = (ArrayList<OpenMatchDTO>) list.clone();
                openMatchAdapter = new OpenMatchAdapter(getContext(), list);
                openMatchAdapter.setRootViewListener(new OpenMatchAdapter.RootViewListener() {
                    @Override
                    public void rootViewDelNotify(OpenMatchDTO openMatchDTO) {
                        list.remove(openMatchDTO);
                        openMatchAdapter.notifyDataSetChanged();
                    }
                });
                customListView.setAdapter(openMatchAdapter);
                openMatchAdapter.notifyDataSetChanged();
            }
        });
        filterDataLoader.getDataDaySort(pastList);
    }


    public void distanceInner(ArrayList<OpenMatchDTO> pastList, Status.DistanceDiff diff){
        FilterDataLoader filterDataLoader = new FilterDataLoader(getContext());
        filterDataLoader.setDataListener(new FilterDataLoader.DataLoadedListener() {
            @Override
            public void dataLoadComplete(ArrayList<OpenMatchDTO> list) {
                tmpList = (ArrayList<OpenMatchDTO>) list.clone();
                openMatchAdapter = new OpenMatchAdapter(getContext(), list);
                openMatchAdapter.setRootViewListener(new OpenMatchAdapter.RootViewListener() {
                    @Override
                    public void rootViewDelNotify(OpenMatchDTO openMatchDTO) {
                        list.remove(openMatchDTO);
                        openMatchAdapter.notifyDataSetChanged();
                    }
                });
                customListView.setAdapter(openMatchAdapter);
                openMatchAdapter.notifyDataSetChanged();
            }
        });
        filterDataLoader.getDataDisDiff(pastList, diff);
    }

    public void distanceSort(ArrayList<OpenMatchDTO> pastList){
        FilterDataLoader filterDataLoader = new FilterDataLoader(getContext());
        filterDataLoader.setDataListener(new FilterDataLoader.DataLoadedListener() {
            @Override
            public void dataLoadComplete(ArrayList<OpenMatchDTO> list) {
                tmpList = (ArrayList<OpenMatchDTO>) list.clone();
                openMatchAdapter = new OpenMatchAdapter(getContext(), list);
                openMatchAdapter.setRootViewListener(new OpenMatchAdapter.RootViewListener() {
                    @Override
                    public void rootViewDelNotify(OpenMatchDTO openMatchDTO) {
                        list.remove(openMatchDTO);
                        openMatchAdapter.notifyDataSetChanged();
                    }
                });
                customListView.setAdapter(openMatchAdapter);
                openMatchAdapter.notifyDataSetChanged();
            }
        });
        filterDataLoader.getDataDistanceSort(pastList);
    }

    public void canJoin(ArrayList<OpenMatchDTO> pastList){
        FilterDataLoader filterDataLoader = new FilterDataLoader(getContext());
        filterDataLoader.setDataListener(new FilterDataLoader.DataLoadedListener() {
            @Override
            public void dataLoadComplete(ArrayList<OpenMatchDTO> list) {
                tmpList = (ArrayList<OpenMatchDTO>) list.clone();
                openMatchAdapter = new OpenMatchAdapter(getContext(), list);
                openMatchAdapter.setRootViewListener(new OpenMatchAdapter.RootViewListener() {
                    @Override
                    public void rootViewDelNotify(OpenMatchDTO openMatchDTO) {
                        list.remove(openMatchDTO);
                        openMatchAdapter.notifyDataSetChanged();
                    }
                });
                customListView.setAdapter(openMatchAdapter);
                openMatchAdapter.notifyDataSetChanged();
            }
        });
        filterDataLoader.getDataCanJoin(pastList);
    }

    @Override
    public void onRefresh() {
        openMatches.clear();
        loadOpenMatchesSportType(sportTypeToString(mSportType));
        openMatchAdapter.notifyDataSetChanged();

        swipeRefreshLayout.setRefreshing(false);
    }
}