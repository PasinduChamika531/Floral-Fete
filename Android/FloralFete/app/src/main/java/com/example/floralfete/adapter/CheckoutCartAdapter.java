package com.example.floralfete.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.floralfete.R;
import com.example.floralfete.model.CartItem;

import java.util.List;

public class CheckoutCartAdapter extends RecyclerView.Adapter<CheckoutCartAdapter.ViewHolder> {
    private Context context;
    private List<CartItem> cartItems;

    public CheckoutCartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.textProductName.setText(item.getProductName());
        holder.textProductPrice.setText("Rs." + item.getProductPrice());
        holder.textProductQuantity.setText("Qty: " + item.getQuantity());

        // Load the first image
        if (item.getProductImage() != null && !item.getProductImage().isEmpty()) {
            Glide.with(context)
                    .load(item.getProductImage())
                    .placeholder(R.drawable.ic_carnation)
                    .into(holder.imageProduct);
        } else {
            holder.imageProduct.setImageResource(R.drawable.ic_carnation);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName, textProductPrice, textProductQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product);
            textProductName = itemView.findViewById(R.id.text_product_name);
            textProductPrice = itemView.findViewById(R.id.text_product_price);
            textProductQuantity = itemView.findViewById(R.id.text_product_quantity);
        }
    }
}
