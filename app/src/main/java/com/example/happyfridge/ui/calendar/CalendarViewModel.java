package com.example.happyfridge.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.happyfridge.data.model.Product;
import com.example.happyfridge.data.repository.ProductRepository;
import com.example.happyfridge.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final MutableLiveData<Date> selectedDate = new MutableLiveData<>();
    private final LiveData<List<Product>> productsForSelectedDate;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);

        selectedDate.setValue(DateUtils.getStartOfDay(new Date()));

        productsForSelectedDate = Transformations.switchMap(selectedDate, date -> {
            if (date == null) {
                return new MutableLiveData<>();
            }
            return repository.getProductsExpiringOnDate(date);
        });
    }

    public LiveData<List<Product>> getProductsForSelectedDate() {
        return productsForSelectedDate;
    }

    public LiveData<Date> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        selectedDate.setValue(DateUtils.getStartOfDay(calendar.getTime()));
    }

    public void delete(Product product) {
        repository.delete(product);
    }

    public void consume(Product product) {
        repository.consumeProduct(product);
    }
}