package com.example.warehousetracker;

public interface StockObserver {
    void onLowStock(String productId, String productName, int currentQuantity);
}