package com.mju.exercise;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mju.exercise.Domain.MapDTO;
import com.mju.exercise.Domain.TypeDTO;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements NaverMap.OnMapClickListener, Overlay.OnClickListener, OnMapReadyCallback, NaverMap.OnCameraChangeListener, NaverMap.OnCameraIdleListener
{
    private MapView mapView;
    private static NaverMap naverMap;
    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private boolean isCameraAnimated = false;
    ArrayList<MapDTO> list ;
    MapDTO mapDTO = null;
    private InfoWindow infoWindow;
    TextView football,baseball,basketball,badminton;
    ProgressDialog dialog;
    private String URL = "http://www.kspo.or.kr/openapi/service/sportsNewFacilInfoService/getNewFacilInfoList?serviceKey=Y3X5lPIRYsK%2FLOfcybbffuDtQrSrECzV0hRpUfjiV4YOcmEuCvUBKN6%2BGH4DV2Jy2NWTr3v7X3doCnS4xs3jSQ%3D%3D&pageNo=1&numOfRows=300&ftypeNm="+ TypeDTO.type+"";
                                                                                                                         //rdz8ehRJ8X8J8QyMoSfmEppmgj%2FqnvSuKTR3PrFoE4RFIUqCCwxBIqv10Qi%2Fs4f13U4bOZEaTevkEwxhrdmB5Q%3D%3D
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //네이버 지도
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        MyAsyncTask myAsyncTask = new MyAsyncTask(); //비동기식 api 파싱
        myAsyncTask.execute();

        football = findViewById(R.id.football);
        football.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypeDTO.type = "축구";
                finish();
                Intent intent = new Intent(MapActivity.this,MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        baseball = findViewById(R.id.baseball);
        baseball.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypeDTO.type = "야구";
                finish();
                Intent intent = new Intent(MapActivity.this,MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        basketball = findViewById(R.id.basketball);
        basketball.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypeDTO.type = "농구"; // 구기운동장
                finish();
                Intent intent = new Intent(MapActivity.this,MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        badminton = findViewById(R.id.badminton);
        badminton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypeDTO.type = "배드민턴";
                finish();
                Intent intent = new Intent(MapActivity.this,MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap)
    {
        this.naverMap = naverMap;
        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);
        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        naverMap.setOnMapClickListener(this);
        infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(getApplicationContext()) {
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                Marker marker = infoWindow.getMarker();
                String name = String.valueOf(marker.getTag());
                int id = name.indexOf("."); //.을 기준으로 문자를 자름
                String name2 = name.substring(0, id); //.앞의 문자를 가져옴
                String name3 = name.substring(name.lastIndexOf(".")+1); //.뒤의 문자를 가져옴
                View  view = View.inflate(getApplicationContext(), R.layout.view_info_window, null);
                ((TextView) view.findViewById(R.id.name)).setText(name3);
                ((TextView) view.findViewById(R.id.type)).setText(list.get(Integer.parseInt(name2)).getFtypeNm());
                ((TextView) view.findViewById(R.id.addr)).setText(list.get(Integer.parseInt(name2)).getFaciAddr1());
                ((TextView) view.findViewById(R.id.tel)).setText(list.get(Integer.parseInt(name2)).getFaciTel());

                return view;
            }
        });



    }

    @Override
    public void onCameraChange(int reason, boolean animated) {
        isCameraAnimated = animated;
    }

    @Override
    public void onCameraIdle() {
        if (isCameraAnimated) {

        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_REQUEST_CODE:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }
    @Override
    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
        if (infoWindow.getMarker() != null) {
            infoWindow.close();
        }
    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if (overlay instanceof Marker) {
            Marker marker = (Marker) overlay;
            if (marker.getInfoWindow() != null) {
                infoWindow.close();
            } else {
                infoWindow.open(marker);
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(marker.getPosition())
                        .animate(CameraAnimation.Fly, 1000);
                naverMap.moveCamera(cameraUpdate);
            }
            return true;
        }
        return false;
    }
    @Override
    public void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public class MyAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                boolean faciAddr1 = false;
                boolean faciAddr2 = false;
                boolean faciNm = false;
                boolean faciPointX =false;
                boolean faciPointY =false;
                boolean faciTel =false;
                boolean ftypeNm =false;

                URL url = new URL(URL); //api주소
                InputStream is = url.openStream();
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(is, "UTF-8"));
                // XmlPullParser를 이용해 데이터 파싱을 한글형식으로 받아옴

                String tag;
                int eventType = parser.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT){
                    //반복문을 통한 XML파일의 끝에 도달했을때 반환값
                    switch (eventType){
                        case XmlPullParser.START_DOCUMENT:
                            //XML 파일의 맨 처음의 반환값
                            list = new ArrayList<MapDTO>();
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.END_TAG:
                            //요소의 종료태그를 만났을때 반환값
                            if(parser.getName().equals("item") && mapDTO != null) {
                                list.add(mapDTO);
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            //요소의 처음태그를 만났을때 반환값
                            if(parser.getName().equals("item")){
                                mapDTO = new MapDTO();
                            }
                            if (parser.getName().equals("faciAddr1")) faciAddr1 = true;
                            if (parser.getName().equals("faciAddr2")) faciAddr2 = true;
                            if (parser.getName().equals("faciNm")) faciNm = true;
                            if (parser.getName().equals("faciPointX")) faciPointX = true;
                            if (parser.getName().equals("faciPointY")) faciPointY = true;
                            if (parser.getName().equals("faciTel")) faciTel = true;
                            if (parser.getName().equals("ftypeNm")) ftypeNm = true;

                            break;
                        case XmlPullParser.TEXT:
                            //요소의 텍스트를 만났을때 반환값
                            if(faciAddr1) {
                                mapDTO.setFaciAddr1(parser.getText());
                                faciAddr1 = false;
                            }else if(faciAddr2){
                                mapDTO.setFaciAddr2(parser.getText());
                                faciAddr2 = false;
                            } else if(faciNm){
                                mapDTO.setFaciNm(parser.getText());
                                faciNm = false;
                            } else if(faciPointX) {
                                try {
                                    Double.parseDouble(parser.getText());
                                    mapDTO.setFaciPointX(parser.getText());
                                    faciPointX = false;
                                } catch (NumberFormatException e) {

                                }
                            } else if(faciPointY) {
                                try {
                                    Double.parseDouble(parser.getText());
                                    mapDTO.setFaciPointY(parser.getText());
                                    faciPointY = false;
                                } catch (NumberFormatException e) {

                                }
                            } else if(faciTel) {
                                mapDTO.setFaciTel(parser.getText());
                                faciTel = false;
                            } else if(ftypeNm) {
                                mapDTO.setFtypeNm(parser.getText());
                                ftypeNm = false;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) { //데이터 파싱 종료후
            super.onPostExecute(s);
            if(TypeDTO.type == null){
                return;
            }
            try{
                dialog= new ProgressDialog(MapActivity.this);
                dialog.setTitle(TypeDTO.type+"");
                dialog.setMessage(TypeDTO.type+"장을 검색중입니다");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        for(int i = 1; i < list.size(); i++) {
                            Marker marker = new Marker();
                            LatLng LtLg = new LatLng(Double.valueOf(list.get(i).getFaciPointY()), Double.valueOf(list.get(i).getFaciPointX()));
                            marker.setTag(i+"."+list.get(i).getFaciNm());
                            marker.setPosition(LtLg);
                            marker.setAnchor(new PointF(0.5f, 1.0f));
                            marker.setMap(naverMap);
                            //마커 크기, 색상 설정
                            marker.setWidth(75);
                            marker.setHeight(100);
                            marker.setIcon(MarkerIcons.BLACK);
                            marker.setIconTintColor(Color.RED);
                            marker.setOnClickListener(MapActivity.this);
                        }
                        dialog.dismiss();
                        dialog = null;
                    }
                };
                handler.sendEmptyMessageDelayed(0, 3000);

            }catch (Exception e){

            }
        }
    }


}
