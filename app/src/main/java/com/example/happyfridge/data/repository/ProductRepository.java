package com.example.happyfridge.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.happyfridge.data.database.AppDatabase;
import com.example.happyfridge.data.database.ProductDao;
import com.example.happyfridge.data.model.Product;
import com.example.happyfridge.util.DateUtils;

import java.util.Date;
import java.util.List;

public class ProductRepository {

    private final ProductDao productDao;
    private final LiveData<List<Product>> allProducts;
    private final SharedPreferences statsPrefs;

    private final MutableLiveData<Integer> consumedOnTimeCountLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> consumedExpiredCountLiveData = new MutableLiveData<>();

    private static final String STATS_PREFS_NAME = "consumption_stats";
    private static final String KEY_CONSUMED_ON_TIME = "consumed_on_time_count";
    private static final String KEY_CONSUMED_EXPIRED = "consumed_expired_count";

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        allProducts = productDao.getAllProductsSortedByExpiry();
        statsPrefs = application.getSharedPreferences(STATS_PREFS_NAME, Context.MODE_PRIVATE);
        consumedOnTimeCountLiveData.postValue(statsPrefs.getInt(KEY_CONSUMED_ON_TIME, 0));
        consumedExpiredCountLiveData.postValue(statsPrefs.getInt(KEY_CONSUMED_EXPIRED, 0));
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<Product> getProductById(int productId) {
        return productDao.getProductById(productId);
    }

    public LiveData<List<Product>> getProductsExpiringOnDate(Date date) {
        long startOfDay = DateUtils.getStartOfDayMillis(date);
        long endOfDay = DateUtils.getEndOfDayMillis(date);
        return productDao.getProductsExpiringOnDate(startOfDay, endOfDay);
    }

    public void insert(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.insert(product);
        });
    }

    public void update(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.update(product);
        });
    }

    public void delete(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.delete(product);
        });
    }

    public void consumeProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long daysRemaining = DateUtils.getDaysRemaining(product.getExpiryDate());
            boolean consumedOnTime = daysRemaining >= 0;

            incrementStat(consumedOnTime ? KEY_CONSUMED_ON_TIME : KEY_CONSUMED_EXPIRED);

            productDao.delete(product);
        });
    }

    private void incrementStat(String key) {
        int currentCount = statsPrefs.getInt(key, 0);
        int newCount = currentCount + 1;
        statsPrefs.edit().putInt(key, newCount).apply();
        if (KEY_CONSUMED_ON_TIME.equals(key)) {
            consumedOnTimeCountLiveData.postValue(newCount);
        } else if (KEY_CONSUMED_EXPIRED.equals(key)) {
            consumedExpiredCountLiveData.postValue(newCount);
        }
        Log.d("MEME", "onTime" + consumedOnTimeCountLiveData.getValue() + "expired" + consumedExpiredCountLiveData.getValue());
    }

    public int getConsumedOnTimeCount() {
        return statsPrefs.getInt(KEY_CONSUMED_ON_TIME, 0);
    }

    public int getConsumedExpiredCount() {
        return statsPrefs.getInt(KEY_CONSUMED_EXPIRED, 0);
    }

    public LiveData<Integer> getConsumedOnTimeCountLiveData() {
        return consumedOnTimeCountLiveData;
    }

    public LiveData<Integer> getConsumedExpiredCountLiveData() {
        return consumedExpiredCountLiveData;
    }
}
