package com.example.happyfridge.ui;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.happyfridge.data.model.Product;
import com.example.happyfridge.data.repository.ProductRepository;

import java.util.List;

public class SharedStatisticsViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final MediatorLiveData<Integer> consumedOnTimePercentage = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> consumedExpiredPercentage = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> hasData = new MediatorLiveData<>();

    private final LiveData<Integer> onTimeCountSource;
    private final LiveData<Integer> expiredCountSource;

    public SharedStatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);

        onTimeCountSource = repository.getConsumedOnTimeCountLiveData();
        expiredCountSource = repository.getConsumedExpiredCountLiveData();

        setupMediators();
    }

    private void setupMediators() {
        Runnable calculateAndUpdate = () -> {
            Integer onTime = onTimeCountSource.getValue();
            Integer expired = expiredCountSource.getValue();
            calculateAndUpdatePercentages(onTime, expired);
        };

        consumedOnTimePercentage.addSource(onTimeCountSource, count -> calculateAndUpdate.run());
        consumedOnTimePercentage.addSource(expiredCountSource, count -> calculateAndUpdate.run());

        consumedExpiredPercentage.addSource(onTimeCountSource, count -> calculateAndUpdate.run());
        consumedExpiredPercentage.addSource(expiredCountSource, count -> calculateAndUpdate.run());

        hasData.addSource(onTimeCountSource, count -> calculateAndUpdate.run());
        hasData.addSource(expiredCountSource, count -> calculateAndUpdate.run());
    }

    private void calculateAndUpdatePercentages(Integer onTime, Integer expired) {
        int currentOnTime = (onTime != null) ? onTime : 0;
        int currentExpired = (expired != null) ? expired : 0;
        int totalConsumed = currentOnTime + currentExpired;

        if (totalConsumed > 0) {
            int onTimePercent = Math.round((float) currentOnTime * 100 / totalConsumed);
            int expiredPercent = 100 - onTimePercent;

            updateLiveDataValue(consumedOnTimePercentage, onTimePercent);
            updateLiveDataValue(consumedExpiredPercentage, expiredPercent);
            updateLiveDataValue(hasData, true);
        } else {
            updateLiveDataValue(consumedOnTimePercentage, 0);
            updateLiveDataValue(consumedExpiredPercentage, 0);
            updateLiveDataValue(hasData, false);
        }
    }

    private <T> void updateLiveDataValue(MediatorLiveData<T> liveData, T newValue) {
        if (liveData.getValue() == null || !liveData.getValue().equals(newValue)) {
            liveData.setValue(newValue);
        }
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

    public void consumeProduct(Product product) {
        if (product != null) {
            repository.consumeProduct(product);
        }
    }

    public void deleteProduct(Product product) {
        if (product != null) {
            repository.delete(product);
        }
    }

    public LiveData<List<Product>> getAllProducts() {
        return repository.getAllProducts();
    }
}
