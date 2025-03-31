package com.example.happyfridge.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.happyfridge.data.database.Converters;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "products")
@TypeConverters(Converters.class)
public class Product implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String category;
    public int quantity;
    public String unit;
    public Date expiryDate;

    public Product() {
    }

    public Product(String name, String category, int quantity, String unit, Date expiryDate) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
