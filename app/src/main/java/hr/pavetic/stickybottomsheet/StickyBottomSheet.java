package hr.pavetic.stickybottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import hr.pavetic.stickybottomsheet.databinding.FragmentStickyBottomSheetBinding;

public class StickyBottomSheet extends BottomSheetDialogFragment {

    private FragmentStickyBottomSheetBinding binding;
    private static StickyBottomSheet instance;

    private ConstraintLayout.LayoutParams buttonLayoutParams;
    private static int collapsedMargin;
    private static int buttonHeight;
    private static int expandedHeight;

    public static StickyBottomSheet newInstance() {
        if(instance == null)
            instance = new StickyBottomSheet();
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStickyBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Adapter adapter = new Adapter(initString());
        binding.sheetRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        binding.sheetRecyclerview.setHasFixedSize(true);
        binding.sheetRecyclerview.setAdapter(adapter);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> setupRatio((BottomSheetDialog) dialogInterface));

        ((BottomSheetDialog) dialog).getBehavior().addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // [중간 높이 <= 슬라이딩 시점 <= 최대 높이 사이] 에서만 slideOffset이 양수를 가질 수 있다.
                if(slideOffset > 0) {
                    buttonLayoutParams.topMargin = (int) (((expandedHeight - buttonHeight) - collapsedMargin) * slideOffset + collapsedMargin);
                    Log.d("aaa", "slideOffset : " + slideOffset);
                }
                // 즉 음수가 되는 순간은 중간 높이보다 리스트 높이가 낮아질 때를 뜻한다. (이때는 버튼 위치를 중간 높이일 때로 고정)
                else //If not sliding above expanded, set initial margin
                    buttonLayoutParams.topMargin = collapsedMargin;

                // 조건문에 따라 버튼의 탑 마진을 설정하여 버튼의 위치를 설정한다.
                binding.sheetButton.setLayoutParams(buttonLayoutParams);
            }
        });

        return dialog;
    }

    private void setupRatio(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        if(bottomSheet == null)
            return;

        //Retrieve button parameters
        buttonLayoutParams = (ConstraintLayout.LayoutParams) binding.sheetButton.getLayoutParams();

        // 바텀 시트 구성 요소 설정
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
        ViewGroup.LayoutParams bottomSheetLayoutParams = bottomSheet.getLayoutParams();
        bottomSheetLayoutParams.height = getBottomSheetDialogDefaultHeight();

        // expandedHeight = 최대 높이 (전체 화면의 90%) / peekHeight = 중간 높이 (최대 높이의 약 70%)
        expandedHeight = bottomSheetLayoutParams.height;
        int peekHeight = (int) (expandedHeight / 1.3);

        // 바텀 시트 설정
        bottomSheet.setLayoutParams(bottomSheetLayoutParams);
        // 최대 높이의 바텀 시트를 내릴 때 설정한 중간 높이를 거칠지(false),바로 사라지게 할지 설정 (true)
        BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(false);
        // 바텀 시트의 접었을 때의 높이 설정 (중간 높이로 설정했음)
        BottomSheetBehavior.from(bottomSheet).setPeekHeight(peekHeight);
        // 바텀 시트를 끝까지 슬라이드로 내렸을 때 사라지게 할 수 있는지(true), 설정한 중간 높이가 있는 경우 중간 높이까지만 혹은 설정한 중간 높이가 없는 경우 최대 높이에서 불변 (false)
        BottomSheetBehavior.from(bottomSheet).setHideable(true);

        // 버튼의 높이 = 버튼 자체 높이 + 아래로부터의 마진 40
        buttonHeight = binding.sheetButton.getHeight() + 40;
        // 최대 높이 ~ 중간 높이 : 가변적 / 중간 높이 ~ 사라질때 : 불변적 >> 불변적인 (중간 높이-버튼의 높이)를 디폴트 값으로 설정한다.
        collapsedMargin = peekHeight - buttonHeight;
        buttonLayoutParams.topMargin = collapsedMargin;
        binding.sheetButton.setLayoutParams(buttonLayoutParams);

        // 버튼 뒤로 리스트가 보이지 않도록 해주는 선택적인 조치
        ConstraintLayout.LayoutParams recyclerLayoutParams = (ConstraintLayout.LayoutParams) binding.sheetRecyclerview.getLayoutParams();
        float k = (buttonHeight - 60) / (float) buttonHeight;
        recyclerLayoutParams.bottomMargin = (int) (k*buttonHeight); 
        binding.sheetRecyclerview.setLayoutParams(recyclerLayoutParams);
    }

    //구현하고자 하는 리스트의 최대 높이 (사용자 기기의 디스플레이의 90%)
    private int getBottomSheetDialogDefaultHeight() {
        return getWindowHeight() * 90 / 100;
    }

    //사용자 기기의 디스플레이 높이 구하기
    private int getWindowHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) requireContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    // 예시로 보여줄 리스트 생성
    private List<String> initString() {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < 35; i++)
            list.add("Item " + i);
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
