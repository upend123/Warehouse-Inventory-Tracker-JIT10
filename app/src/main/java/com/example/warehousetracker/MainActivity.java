package com.example.warehousetracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StockObserver {
    private Warehouse warehouse;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private EditText etProductId, etQuantity;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        warehouse = new Warehouse();
        warehouse.addObserver(this);
        warehouse.load(this);  // Load saved data

        initViews();
        refreshList();
    }

    private void initViews() {
        etProductId = findViewById(R.id.etProductId);
        etQuantity = findViewById(R.id.etQuantity);
        recyclerView = findViewById(R.id.recyclerView);

        findViewById(R.id.btnAddProduct).setOnClickListener(v -> showAddProductDialog());
        findViewById(R.id.btnReceive).setOnClickListener(v -> performReceive());
        findViewById(R.id.btnFulfill).setOnClickListener(v -> performFulfill());

        adapter = new ProductAdapter(productList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Product");

        EditText etId = new EditText(this); etId.setHint("ID (e.g. Laptop)");
        EditText etName = new EditText(this); etName.setHint("Name (e.g. Dell Laptop)");
        EditText etThresh = new EditText(this); etThresh.setHint("Reorder Threshold");
        etThresh.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.addView(etId); layout.addView(etName); layout.addView(etThresh);

        builder.setView(layout);
        builder.setPositiveButton("Add", (d, w) -> {
            String id = etId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            if (id.isEmpty() || name.isEmpty()) return;
            try {
                int thresh = Integer.parseInt(etThresh.getText().toString());
                Product p = new Product(id, name, 0, thresh);
                warehouse.addProduct(p);
                refreshList();
                warehouse.save(this);
                Toast.makeText(this, "Added: " + name, Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid threshold", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void performReceive() {
        String id = etProductId.getText().toString().trim();
        if (id.isEmpty()) return;
        try {
            int qty = Integer.parseInt(etQuantity.getText().toString());
            if (warehouse.getProduct(id) == null) {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                return;
            }
            warehouse.receiveShipment(id, qty);
            refreshList();
            warehouse.save(this);
            etQuantity.setText("");
            Toast.makeText(this, "Received " + qty, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException ignored) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    private void performFulfill() {
        String id = etProductId.getText().toString().trim();
        if (id.isEmpty()) return;
        try {
            int qty = Integer.parseInt(etQuantity.getText().toString());
            if (warehouse.fulfillOrder(id, qty)) {
                refreshList();
                warehouse.save(this);
                etQuantity.setText("");
                Toast.makeText(this, "Fulfilled " + qty, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cannot fulfill: Not found or low stock", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException ignored) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshList() {
        productList.clear();
        productList.addAll(warehouse.getAllProducts());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLowStock(String id, String name, int qty) {
        new AlertDialog.Builder(this)
                .setTitle("Restock Alert!")
                .setMessage("Low stock for **" + name + "** – only **" + qty + "** left!")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        warehouse.save(this);
    }
}