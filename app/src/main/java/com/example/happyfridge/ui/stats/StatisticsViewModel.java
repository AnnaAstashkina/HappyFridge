package com.example.happyfridge.ui.stats;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.happyfridge.data.repository.ProductRepository;

public class StatisticsViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final MediatorLiveData<Integer> consumedOnTimePercentage = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> consumedExpiredPercentage = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> hasData = new MediatorLiveData<>();

    private final LiveData<Integer> onTimeCountSource;
    private final LiveData<Integer> expiredCountSource;


    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);

        onTimeCountSource = repository.getConsumedOnTimeCountLiveData();
        expiredCountSource = repository.getConsumedExpiredCountLiveData();

        consumedOnTimePercentage.addSource(onTimeCountSource, onTimeCount -> {
            calculateAndUpdatePercentages(onTimeCount, expiredCountSource.getValue());
        });

        consumedOnTimePercentage.addSource(expiredCountSource, expiredCount -> {
            calculateAndUpdatePercentages(onTimeCountSource.getValue(), expiredCount);
        });

        hasData.addSource(onTimeCountSource, onTimeCount -> {
            calculateAndUpdatePercentages(onTimeCount, expiredCountSource.getValue());
        });
        hasData.addSource(expiredCountSource, expiredCount -> {
            calculateAndUpdatePercentages(onTimeCountSource.getValue(), expiredCount);
        });

        consumedExpiredPercentage.addSource(onTimeCountSource, onTimeCount -> {
            calculateAndUpdatePercentages(onTimeCount, expiredCountSource.getValue());
        });
        consumedExpiredPercentage.addSource(expiredCountSource, expiredCount -> {
            calculateAndUpdatePercentages(onTimeCountSource.getValue(), expiredCount);
        });
    }

    private void calculateAndUpdatePercentages(Integer onTime, Integer expired) {
        int currentOnTime = (onTime != null) ? onTime : 0;
        int currentExpired = (expired != null) ? expired : 0;
        int totalConsumed = currentOnTime + currentExpired;

        if (totalConsumed > 0) {
            int onTimePercent = Math.round((float) currentOnTime * 100 / totalConsumed);
            int expiredPercent = 100 - onTimePercent;

            consumedOnTimePercentage.setValue(onTimePercent);
            consumedExpiredPercentage.setValue(expiredPercent);
            hasData.setValue(true);
        } else {
            consumedOnTimePercentage.setValue(0);
            consumedExpiredPercentage.setValue(0);
            hasData.setValue(false);
        }
        Log.d("MEME", "onTime" + consumedOnTimePercentage.getValue() + "expired" + consumedExpiredPercentage.getValue());
    }

    public LiveData<Integer> getConsumedOnTimePercentage() {
        return consumedOnTimePercentage;
    }

    public LiveData<Integer> getConsumedExpiredPercentage() {
        return consumedExpiredPercentage;
    }

    public LiveData<Boolean> getHasData() {
        return hasData;
    }

}