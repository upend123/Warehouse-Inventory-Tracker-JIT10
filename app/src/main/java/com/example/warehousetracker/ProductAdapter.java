    package com.example.warehousetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> products;

    public ProductAdapter(List<Product> products) {
        this.products = products;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Product p = products.get(position);
        holder.idText.setText(p.getId());
        holder.nameText.setText(p.getName());
        holder.qtyText.setText("Qty: " + p.getQuantity());
        holder.threshText.setText("Thresh: " + p.getThreshold());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView idText, nameText, qtyText, threshText;

        ViewHolder(View v) {
            super(v);
            idText = v.findViewById(R.id.product_id);
            nameText = v.findViewById(R.id.product_name);
            qtyText = v.findViewById(R.id.product_qty);
            threshText = v.findViewById(R.id.product_threshold);
        }
    }
}