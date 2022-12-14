package com.mju.exercise.OpenMatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.mju.exercise.R;
import com.mju.exercise.StatusEnum.Status;
import com.skydoves.expandablelayout.ExpandableLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

public class FilteringDialog extends BottomSheetDialogFragment{

    private ExpandableLayout exlayoutDistance, exlayoutDay, exlayoutPerssonel;
    private Button btnFilterApply;

    private Status.FilterTypeDay filterTypeDay = Status.FilterTypeDay.DAY_DEFAULT;
    private Status.FilterTypeJoin filterTypeJoin = Status.FilterTypeJoin.JOIN_DEFAULT;
    private Status.FilterTypeDistance filterTypeDistance = Status.FilterTypeDistance.DISTANCE_DEFAULT;
    private Status.DistanceDiff mDiff = Status.DistanceDiff.DEFAULT;
    private Status.FavDayType mFavDay = Status.FavDayType.DEFAULT;
    private LocalDateTime mPickDay;

    private final int CHIP_INT = 0x8000;
    private String[] chipStr = {
            "????????? ?????????", "100m ??????", "500m ??????","1km ??????","3km ??????","3km ??????",
            "????????? ?????????", "?????? ??????", "?????? ?????? ??????",
            "?????? ?????????"
    };

    private String[] disStr = {"????????? ?????????", "100m ??????", "500m ??????","1km ??????","3km ??????","3km ??????"};
    private String[] dayStr = {"????????? ?????????", "?????? ??????", "?????? ?????? ??????"};
    private String[] joinStr = {"???????????????"};

    private String[] favDayStr = {
            "?????????", "?????????", "?????????", "?????????", "?????????", "?????????", "?????????"
    };
    private HashMap<String, Integer> chipMap = new HashMap<>();
    private boolean isClickedDis = false;
    private boolean isClickedDay = false;
    private boolean isClickedPer = false;
    private String lastDis = null, lastDay = null, lastPer = null;


    public OpenMatchFilter openMatchFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_filtering, container, false);
        btnFilterApply = (Button) view.findViewById(R.id.btnFilterApply);
        btnFilterApply.setOnClickListener(setOnClickListener);

        exlayoutDistance = (ExpandableLayout) view.findViewById(R.id.exlayoutDistance);
        exlayoutDay = (ExpandableLayout) view.findViewById(R.id.exlayoutDay);
        exlayoutPerssonel = (ExpandableLayout) view.findViewById(R.id.exlayoutPerssonel);

        initChipMap();

        initExpandableLayout(exlayoutDistance);
        initExpandableLayout(exlayoutDay);
        initExpandableLayout(exlayoutPerssonel);

        return view;
    }

    private void initExpandableLayout(ExpandableLayout expandableLayout){
        TextView tvFilterType = (TextView) expandableLayout.parentLayout.findViewById(R.id.tvFilterType);
        Button btnFilterDetail = (Button) expandableLayout.parentLayout.findViewById(R.id.btnFilterDetail);
        if(expandableLayout.isExpanded()){
            btnFilterDetail.setText("????????????");
        }else {
            btnFilterDetail.setText("??? ??????");
        }

        btnFilterDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(expandableLayout.isExpanded()){
                    btnFilterDetail.setText("??? ??????");
                    expandableLayout.collapse();
                }else {
                    btnFilterDetail.setText("????????????");
                    expandableLayout.expand();
                }
            }
        });

        ChipGroup chipGroup = (ChipGroup) expandableLayout.secondLayout.findViewById(R.id.chipGroupFilter);
        chipGroup.setSingleSelection(true);
        switch (expandableLayout.getId()){
            case R.id.exlayoutDistance:
                tvFilterType.setText("?????? ??????");
                String[] distance = {
                        "????????? ?????????", "100m ??????", "500m ??????","1km ??????","3km ??????","3km ??????"
                };
                for(String tmp: distance){
                    chipGroup.addView(makeChip(tmp));
                }
                break;
            case R.id.exlayoutDay:
                tvFilterType.setText("?????? ??????");
                String[] day = {
                        "????????? ?????????", "?????? ??????", "?????? ?????? ??????"
                };
                for(String tmp: day){
                    chipGroup.addView(makeChip(tmp));
                }
                break;
            case R.id.exlayoutPerssonel:
                tvFilterType.setText("?????? ??????");
                String[] personnel = {
                        "?????? ?????????"
                };
                for(String tmp: personnel){
                    chipGroup.addView(makeChip(tmp));
                }
                break;
        }
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, int checkedId) {
                Log.d("?????????", String.valueOf(checkedId));
                settingFilter(checkedId);
            }
        });

    }

    private void initChipMap(){
        int i = CHIP_INT;
        for (String str: chipStr){
            chipMap.put(str, i);
            i += 1;
            Log.d("?????????", "??????: " + String.valueOf(i));
        }
    }

    private void settingFilter(int checkedId){
        switch (checkedId){
            //????????????
            case CHIP_INT:
                filterTypeDistance = Status.FilterTypeDistance.DISTANCE_NEAR;
                break;
            case CHIP_INT + 1:
                filterTypeDistance = Status.FilterTypeDistance.DISTANCE_DIFFERENCE;
                mDiff = Status.DistanceDiff.M100;
                break;
            case CHIP_INT + 2:
                filterTypeDistance = Status.FilterTypeDistance.DISTANCE_DIFFERENCE;
                mDiff = Status.DistanceDiff.M500;
                break;
            case CHIP_INT + 3:
                filterTypeDistance = Status.FilterTypeDistance.DISTANCE_DIFFERENCE;
                mDiff = Status.DistanceDiff.M1KM;
                break;
            case CHIP_INT + 4:
                filterTypeDistance = Status.FilterTypeDistance.DISTANCE_DIFFERENCE;
                mDiff = Status.DistanceDiff.M3KM;
                break;
            case CHIP_INT +5:
                filterTypeDistance = Status.FilterTypeDistance.DISTANCE_DIFFERENCE;
                mDiff = Status.DistanceDiff.M3KMUP;
                break;

            //?????? ??????
            case CHIP_INT + 6:
                filterTypeDay = Status.FilterTypeDay.DAY_NEAR;
                break;
            case CHIP_INT +7:
                filterTypeDay = Status.FilterTypeDay.DAY_FAVDAY;
                //??????????????? ????????? ???????????? ????????????
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_favday_select, null);
                RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rdgFavDay);
                new AlertDialog.Builder(getContext())
                        .setTitle("?????? ??????")
                        .setView(view)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                favRdbtnMapping(radioGroup.getCheckedRadioButtonId());
                            }
                        })
                        .show();
                break;
            case CHIP_INT +8:
                filterTypeDay = Status.FilterTypeDay.DAY_PICK;
                //??????????????? ????????? ???????????? ????????????
                MaterialDatePicker materialDatePicker;
                materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("????????? ??????")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        Log.d("?????????", "??????:" + selection.toString());
                        Date date = new Date(selection);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mPickDay = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                            Log.d("?????????", "??????:" + mPickDay.toString());
                        }
                    }
                });

                materialDatePicker.show(getChildFragmentManager(), "date");
                break;

            //?????? ??????
            case CHIP_INT +9:
                filterTypeJoin = Status.FilterTypeJoin.JOIN_CAN;
                break;
        }
    }

    private void favRdbtnMapping(int id){
        Log.d("?????????", "?????????: " + String.valueOf(id));
        switch (id){
            case R.id.rdbtnMon:
                mFavDay = Status.FavDayType.MON;
                break;
            case R.id.rdbtnTue:
                mFavDay = Status.FavDayType.TUE;
                break;
            case R.id.rdbtnWed:
                mFavDay = Status.FavDayType.WED;
                break;
            case R.id.rdbtnThu:
                mFavDay = Status.FavDayType.THU;
                break;
            case R.id.rdbtnFri:
                mFavDay = Status.FavDayType.FRI;
                break;
            case R.id.rdbtnSat:
                mFavDay = Status.FavDayType.SAT;
                break;
            case R.id.rdbtnSun:
                mFavDay = Status.FavDayType.SUN;
                break;
        }
    }

    //??? ?????? ?????? ????????? ????????? ??? ???????????? ??????
    private Chip makeChip(String chipText){
        Chip chip = new Chip(getContext()); // Must contain context in parameter
        chip.setText(chipText);
        chip.setCheckable(true);
        chip.setId(chipMap.get(chipText));
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("??????", chip.getText().toString() + "??????");
                checkClickedType(chip.getText().toString());
            }
        });

        return chip;
    }

    private void checkClickedType(String string){

        if(Arrays.asList(disStr).contains(string)){
            Log.d("??????", "?????? ??????");
            if(lastDis != null){
                if(!string.equals(lastDis)){
                    lastDis = string;
                    return;
                }else {
                    isClickedDis = !isClickedDis;
                }
            }else {
                lastDis = string;
                isClickedDis = !isClickedDis;
            }


        }else if(Arrays.asList(dayStr).contains(string)){
            Log.d("??????", "?????? ??????");
            if(lastDay != null){
                if(!string.equals(lastDay)){
                    lastDay = string;
                    return;
                }else {
                    isClickedDay = !isClickedDay;
                }
            }else {
                lastDay = string;
                isClickedDay = !isClickedDay;
            }


        }else if(string.equals("?????? ?????????")){
            Log.d("??????", "?????? ??????");
            if(lastPer != null){
                if(!string.equals(lastPer)){
                    lastPer = string;
                    return;
                }else {
                    isClickedPer = !isClickedPer;
                }
            }else {
                lastPer = string;
                isClickedPer = !isClickedPer;
            }


        }

    }

    //???????????? ????????????
    private View.OnClickListener setOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.btnFilterApply:
                    Log.d("??????", "?????? ????????????");


                    //????????? ????????? ?????? ????????? ????????? ??????????????? ???
                    if(!isClickedDay){
                        filterTypeDay = Status.FilterTypeDay.DAY_DEFAULT;
                    }
                    if(!isClickedPer){
                        filterTypeJoin = Status.FilterTypeJoin.JOIN_DEFAULT;
                    }
                    if(!isClickedDis){
                        filterTypeDistance = Status.FilterTypeDistance.DISTANCE_DEFAULT;
                    }

                    //?????? OpenMatchActivity??? ??? ??????
                    openMatchFilter.setFilter(filterTypeJoin, filterTypeDistance, filterTypeDay,
                            mDiff, mFavDay, mPickDay);

                    dismiss();
                    break;
            }

        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                setupRatio(bottomSheetDialog);
            }
        });
        return dialog;
    }

    private void setupRatio(BottomSheetDialog bottomSheetDialog) {
        //id = com.google.android.material.R.id.design_bottom_sheet for Material Components
        // id = android.support.design.R.id.design_bottom_sheet for support librares
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = getBottomSheetDialogDefaultHeight();
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    private int getBottomSheetDialogDefaultHeight() { return getWindowHeight() * 75 / 100; }
    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    public void setDialogResult(OpenMatchFilter openMatchFilter){
        this.openMatchFilter = openMatchFilter;
    }
}
