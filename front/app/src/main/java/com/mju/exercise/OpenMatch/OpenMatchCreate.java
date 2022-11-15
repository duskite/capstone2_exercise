package com.mju.exercise.OpenMatch;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.mju.exercise.R;

import java.time.Clock;
import java.time.LocalDateTime;

public class OpenMatchCreate extends BottomSheetDialogFragment {

    Button btnCreate, btnClose;
    TextInputEditText edtSubject, edtArticle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_open_match_create, container, false);

        btnCreate = view.findViewById(R.id.btnCreate);
        btnClose = view.findViewById(R.id.btnClose);
        edtSubject = view.findViewById(R.id.edtSubject);
        edtArticle = view.findViewById(R.id.edtArticle);

        btnCreate.setOnClickListener(setOnClickListener);
        btnClose.setOnClickListener(setOnClickListener);


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

                case R.id.btnClose:
                    dismiss();
                    break;
            }
        }
    };

    private boolean createOpenMatch(){

        edtSubject.getText().toString();
        edtArticle.getText().toString();

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
