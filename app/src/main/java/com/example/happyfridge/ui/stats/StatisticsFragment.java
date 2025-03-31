package com.example.happyfridge.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.happyfridge.R;
import com.example.happyfridge.ui.SharedStatisticsViewModel;

public class StatisticsFragment extends Fragment {

    private SharedStatisticsViewModel sharedStatisticsViewModel;
    private TextView onTimePercentageText;
    private TextView expiredPercentageText;
    private TextView noDataTextView;
    private Group statsContentGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        onTimePercentageText = view.findViewById(R.id.text_percentage_on_time);
        expiredPercentageText = view.findViewById(R.id.text_percentage_expired);
        noDataTextView = view.findViewById(R.id.text_no_stats_data);
        statsContentGroup = view.findViewById(R.id.group_stats_content);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedStatisticsViewModel = new ViewModelProvider(requireActivity()).get(SharedStatisticsViewModel.class);

        sharedStatisticsViewModel.getConsumedOnTimePercentage().observe(getViewLifecycleOwner(), percentage -> {
            if (percentage != null) {
                onTimePercentageText.setText(getString(R.string.stats_percentage_format, percentage));
            }
        });

        sharedStatisticsViewModel.getConsumedExpiredPercentage().observe(getViewLifecycleOwner(), percentage -> {
            if (percentage != null) {
                expiredPercentageText.setText(getString(R.string.stats_percentage_format, percentage));
            }
        });

        sharedStatisticsViewModel.getHasData().observe(getViewLifecycleOwner(), hasData -> {
            if (hasData != null && hasData) {
                statsContentGroup.setVisibility(View.VISIBLE);
                noDataTextView.setVisibility(View.GONE);
            } else {
                statsContentGroup.setVisibility(View.GONE);
                noDataTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onTimePercentageText = null;
        expiredPercentageText = null;
        noDataTextView = null;
        statsContentGroup = null;
    }
}
