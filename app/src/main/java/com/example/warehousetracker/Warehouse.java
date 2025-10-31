package com.example.warehousetracker;

import android.content.Context;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Warehouse {
    private static final String FILE_NAME = "inventory.txt";
    private ConcurrentHashMap<String, Product> inventory = new ConcurrentHashMap<>();
    private List<StockObserver> observers = new ArrayList<>();

    public Warehouse() {}

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    public void addProduct(Product product) {
        if (product != null && !inventory.containsKey(product.getId())) {
            inventory.put(product.getId(), product);
        }
    }

    public void receiveShipment(String productId, int quantity) {
        Product product = inventory.get(productId);
        if (product != null && quantity > 0) {
            product.setQuantity(product.getQuantity() + quantity);
            checkAndNotify(product);
        }
    }

    public boolean fulfillOrder(String productId, int quantity) {
        Product product = inventory.get(productId);
        if (product != null && quantity > 0 && product.getQuantity() >= quantity) {
            product.setQuantity(product.getQuantity() - quantity);
            checkAndNotify(product);
            return true;
        }
        return false;
    }

    public Product getProduct(String id) {
        return inventory.get(id);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(inventory.values());
    }

    private void checkAndNotify(Product product) {
        if (product.getQuantity() < product.getThreshold()) {
            for (StockObserver observer : observers) {
                observer.onLowStock(product.getId(), product.getName(), product.getQuantity());
            }
        }
    }

    // BONUS: Persist to file
    public void save(Context context) {
        try {
            PrintWriter pw = new PrintWriter(context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
            for (Product p : inventory.values()) {
                pw.println(p.getId() + "|" + p.getName() + "|" + p.getQuantity() + "|" + p.getThreshold());
            }
            pw.close();
        } catch (Exception ignored) {}
    }

    public void load(Context context) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(FILE_NAME)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    Product p = new Product(parts[0], parts[1],
                            Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    inventory.put(p.getId(), p);
                }
            }
            br.close();
        } catch (Exception ignored) {
        }
    }}
