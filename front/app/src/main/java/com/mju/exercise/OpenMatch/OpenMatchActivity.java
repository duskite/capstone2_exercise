package com.mju.exercise.OpenMatch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.mju.exercise.R;

public class OpenMatchActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private int sportType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_match);

        Intent intent = getIntent();
        sportType = intent.getIntExtra("sport", 0);
        Log.d("인텐트체크", String.valueOf(intent.getIntExtra("sport", 0)));

        init();
        bottomNavigationView.setSelectedItemId(R.id.open_match_list);
    }

    private void init(){
        fragmentManager = getSupportFragmentManager();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(new NavSelectedListener());
    }

    class NavSelectedListener implements NavigationBarView.OnItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentTransaction = fragmentManager.beginTransaction();
            switch (item.getItemId()){
                case R.id.open_match_list:{
                    OpenMatchListFrag openMatchListFrag = OpenMatchListFrag.newInstance(sportType);
                    fragmentTransaction.replace(R.id.host_fragment, openMatchListFrag)
                            .commit();
                    return true;
                }
                case R.id.open_match_created:{
                    fragmentTransaction.replace(R.id.host_fragment, new OpenMatchCreatedFrag())
                            .commit();
                    return true;
                }
                case R.id.open_match_joined:{
                    fragmentTransaction.replace(R.id.host_fragment, new OpenMatchJoinedFrag())
                            .commit();
                    return true;
                }

                case R.id.open_match_create:
                    return true;
                case R.id.chat_list:
                    return true;

            }

            return false;
        }
    }
}
