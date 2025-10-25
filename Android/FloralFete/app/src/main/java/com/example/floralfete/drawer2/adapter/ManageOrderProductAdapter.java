package com.example.floralfete.drawer2.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.floralfete.R;
import com.example.floralfete.model.Product;
import java.util.List;

public class ManageOrderProductAdapter extends RecyclerView.Adapter<ManageOrderProductAdapter.ViewHolder> {

    private List<Product> productList;

    public ManageOrderProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productQty.setText("Qty: " + product.getQuantity());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.manage_order_product_name);
            productQty = itemView.findViewById(R.id.manage_order_product_qty);
        }
    }
}
