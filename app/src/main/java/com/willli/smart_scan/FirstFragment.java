package com.willli.smart_scan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.willli.smart_scan.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 单次扫描按钮
        binding.buttonFirst.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            startActivity(intent);
        });

        // 批量扫描按钮
        binding.buttonBatchScan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BatchScanActivity.class);
            startActivity(intent);
        });

        // 历史记录按钮
        binding.buttonHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HistoryActivity.class);
            startActivity(intent);
        });

        // 功能特性文本点击跳转到关于页面
        binding.textviewFirst.setOnClickListener(v -> {
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}