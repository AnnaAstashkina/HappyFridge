package com.example.happyfridge.ui.addedit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.happyfridge.R;
import com.example.happyfridge.data.model.Product;
import com.example.happyfridge.data.repository.ProductRepository;
import com.example.happyfridge.util.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class AddEditProductViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final MutableLiveData<Product> productToEdit = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> saveFinished = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    public static final int EXPIRY_TYPE_DATE = 0;
    public static final int EXPIRY_TYPE_DURATION = 1;
    private final MutableLiveData<Integer> expiryInputType = new MutableLiveData<>(EXPIRY_TYPE_DATE);
    private Observer<Product> productObserver;

    public AddEditProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<Product> getProductToEdit() {
        return productToEdit;
    }

    public LiveData<Boolean> getSaveFinished() {
        return saveFinished;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getExpiryInputType() {
        return expiryInputType;
    }

    public void loadProduct(int productId) {
        if (productObserver == null) {
            productObserver = product -> {
                if (product != null) {
                    productToEdit.postValue(product);
                    expiryInputType.postValue(EXPIRY_TYPE_DATE);
                }
            };
        }
        repository.getProductById(productId).observeForever(productObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (productObserver != null && productToEdit.getValue() != null) {
            LiveData<Product> liveData = repository.getProductById(productToEdit.getValue().getId());
            if (liveData != null) {
                liveData.removeObserver(productObserver);
            }
        }
        productObserver = null;
    }

    public void setExpiryInputType(int type) {
        expiryInputType.setValue(type);
    }

    public void saveProduct(String name, String category, String quantityStr, String unit, Date expiryDate,
                            Date manufacturingDate, String shelfLifeValueStr, boolean shelfLifeIsMonths) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.error_empty_field) + ": Название");
            return;
        }
        if (category == null || category.isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.error_generic) + ": Категория");
            return;
        }
        if (unit == null || unit.isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.error_generic) + ": Ед. изм.");
            return;
        }
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.error_empty_field) + ": Количество");
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorMessage.setValue(getApplication().getString(R.string.error_invalid_number) + ": Количество");
            return;
        }

        Date finalExpiryDate = null;
        int currentExpiryType = expiryInputType.getValue() != null ? expiryInputType.getValue() : EXPIRY_TYPE_DATE;

        if (currentExpiryType == EXPIRY_TYPE_DATE) {
            if (expiryDate == null) {
                errorMessage.setValue(getApplication().getString(R.string.error_empty_field) + ": Дата просрочки");
                return;
            }
            finalExpiryDate = expiryDate;
        } else {
            if (manufacturingDate == null) {
                errorMessage.setValue(getApplication().getString(R.string.error_empty_field) + ": Дата изготовления");
                return;
            }
            if (shelfLifeValueStr == null || shelfLifeValueStr.trim().isEmpty()) {
                errorMessage.setValue(getApplication().getString(R.string.error_empty_field) + ": Срок хранения");
                return;
            }
            int shelfLifeValue;
            try {
                shelfLifeValue = Integer.parseInt(shelfLifeValueStr);
                if (shelfLifeValue <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                errorMessage.setValue(getApplication().getString(R.string.error_invalid_number) + ": Срок хранения");
                return;
            }

            finalExpiryDate = DateUtils.calculateExpiryDate(manufacturingDate, shelfLifeValue, shelfLifeIsMonths);
            if (finalExpiryDate == null) {
                errorMessage.setValue(getApplication().getString(R.string.error_generic) + ": Расчет даты");
                return;
            }
        }

        Product currentProduct = productToEdit.getValue();
        if (currentProduct != null) {
            currentProduct.setName(name.trim());
            currentProduct.setCategory(category);
            currentProduct.setQuantity(quantity);
            currentProduct.setUnit(unit);
            currentProduct.setExpiryDate(finalExpiryDate);
            repository.update(currentProduct);
        } else {
            Product newProduct = new Product(name.trim(), category, quantity, unit, finalExpiryDate);
            repository.insert(newProduct);
        }
        saveFinished.setValue(true);
    }

    public void resetSaveState() {
        saveFinished.setValue(false);
        errorMessage.setValue(null);
    }
}
