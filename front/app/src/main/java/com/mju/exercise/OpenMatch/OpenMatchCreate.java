package com.mju.exercise.OpenMatch;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.mju.exercise.ChatActivity;
import com.mju.exercise.Domain.OpenMatchDTO;
import com.mju.exercise.MainActivity;
import com.mju.exercise.PopupMapActivity;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.R;

import java.time.Clock;
import java.time.LocalDateTime;

public class OpenMatchCreate extends BottomSheetDialogFragment {

    Button btnCreate, btnDatePickOpen, btnPersonnelPickOpen;
    TextInputEditText edtSubject, edtArticle;
    PreferenceUtil preferenceUtil;

    ChipGroup chipGroup;

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_open_match_create, container, false);

        btnCreate = view.findViewById(R.id.btnCreate);
        btnDatePickOpen = view.findViewById(R.id.btnDatePickOpen);
        btnPersonnelPickOpen = view.findViewById(R.id.btnPersonnelPickOpen);


        edtSubject = view.findViewById(R.id.edtSubject);
        edtArticle = view.findViewById(R.id.edtArticle);


        btnCreate.setOnClickListener(setOnClickListener);
        btnDatePickOpen.setOnClickListener(setOnClickListener);
        btnPersonnelPickOpen.setOnClickListener(setOnClickListener);

        preferenceUtil = PreferenceUtil.getInstance(getContext());

        //작업중, 현재 단말기 없어서 테스트는 못함
        chipGroup = view.findViewById(R.id.chipGroup);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData().getData() != null){

            }else{

            }
        });


        return view;
    }

    private View.OnClickListener setOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnCreate:

                    if(!createOpenMatch()){
                        Toast.makeText(getContext(), "생성 실패", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    Toast.makeText(getContext(), "오픈매치 생성 완료", Toast.LENGTH_SHORT).show();
                    break;


                case R.id.btnMapPickOpen:
                    Intent intent = new Intent(getContext(), PopupMapActivity.class);
//                    activityResultLauncher.launch(intent);
                    startActivity(intent);
                    break;

                case R.id.btnDatePickOpen:
                    MaterialDatePicker materialDatePicker;
                    materialDatePicker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("경기 날짜를 선택")
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .build();

                    materialDatePicker.show(getParentFragmentManager(), "date");

                    break;

                case R.id.btnPersonnelPickOpen:
                    View dialogview = getLayoutInflater().inflate(R.layout.dialog_num_select, null);
                    NumberPicker numberPicker = dialogview.findViewById(R.id.numberPicker);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setView(dialogview);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    break;
            }
        }
    };

    private boolean createOpenMatch(){

        OpenMatchDTO openMatchDTO = new OpenMatchDTO();

        openMatchDTO.setSubject(edtSubject.getText().toString());
        openMatchDTO.setArticle(edtArticle.getText().toString());
        openMatchDTO.setOpenTime(nowTime());
        openMatchDTO.setOpenUserId(preferenceUtil.getString("userId"));

        Integer personnel;

        String sportType;
        LocalDateTime playDateTime;
        Double lat;
        Double lng;


        return false;
    }

    private LocalDateTime nowTime(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDateTime.now(Clock.systemDefaultZone());
        }
        //오레오 미만 기기에서는 지원하지 않는 다고 함.
        //나중에 추가 필요
        return null;
    }


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
    private int getBottomSheetDialogDefaultHeight() { return getWindowHeight() * 90 / 100; }
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
}
