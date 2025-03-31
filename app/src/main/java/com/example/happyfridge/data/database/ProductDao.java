package com.example.happyfridge.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.happyfridge.data.model.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    LiveData<List<Product>> getAllProductsSortedByExpiry();

    @Query("SELECT * FROM products WHERE expiryDate >= :startOfDay AND expiryDate < :endOfDay ORDER BY expiryDate ASC")
    LiveData<List<Product>> getProductsExpiringOnDate(long startOfDay, long endOfDay);

    @Query("SELECT * FROM products WHERE id = :productId")
    LiveData<Product> getProductById(int productId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("DELETE FROM products WHERE id = :productId")
    void deleteById(int productId);
}
