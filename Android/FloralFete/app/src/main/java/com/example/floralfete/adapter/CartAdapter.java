package com.example.floralfete.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.floralfete.R;
import com.example.floralfete.SingleProductViewActivity;
import com.example.floralfete.database.CartDatabaseHelper;
import com.example.floralfete.model.CartItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private CartUpdateListener cartUpdateListener;
    private String userId;
    private boolean isLoggedIn;

    public CartAdapter(Context context, List<CartItem> cartItems, CartUpdateListener cartUpdateListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.cartUpdateListener = cartUpdateListener;

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        this.isLoggedIn = sharedPreferences.contains("user_id");  // Fix SharedPreferences key here
        this.userId = sharedPreferences.getString("user_id", "");
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.textProductName.setText(item.getProductName());
        holder.textProductPrice.setText("Rs." + item.getProductPrice());
        holder.textQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context).load(item.getProductImage()).into(holder.imageProduct);

        holder.imageProduct.setOnClickListener(v -> {
            Intent gotoSingleProduct = new Intent(context, SingleProductViewActivity.class);
            gotoSingleProduct.putExtra("productId",item.getProductId());
            context.startActivity(gotoSingleProduct);
        });

        holder.buttonIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            holder.textQuantity.setText(String.valueOf(newQuantity));
            updateCartDatabase(item);

            if (cartUpdateListener != null) {
                cartUpdateListener.onQuantityChanged(item, newQuantity);  // Notify fragment to update total price
            }
        });

        holder.buttonDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                item.setQuantity(newQuantity);
                holder.textQuantity.setText(String.valueOf(newQuantity));
                updateCartDatabase(item);


                if (cartUpdateListener != null) {
                    cartUpdateListener.onQuantityChanged(item, newQuantity);  // Notify fragment to update total price
                }
            }
        });

        holder.buttonRemove.setOnClickListener(v -> {

            removeItem(item, position);

        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName, textProductPrice, textQuantity;
        Button buttonRemove, buttonIncrease, buttonDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product);
            textProductName = itemView.findViewById(R.id.text_product_name);
            textProductPrice = itemView.findViewById(R.id.text_product_price);
            textQuantity = itemView.findViewById(R.id.text_quantity);
            buttonRemove = itemView.findViewById(R.id.button_remove);
            buttonIncrease = itemView.findViewById(R.id.button_increase);
            buttonDecrease = itemView.findViewById(R.id.button_decrease);
        }
    }

    private void updateCartDatabase(CartItem item) {
        if (isLoggedIn) {
            updateFirestoreCart(item);
        } else {
            updateSQLiteCart(item);
        }
    }

    private void updateFirestoreCart(CartItem item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (item.getQuantity() > 0) {
            db.collection("cart")
                    .document(userId)
                    .collection("items")
                    .document(item.getProductId())
                    .set(item.toMap(), com.google.firebase.firestore.SetOptions.merge());
        } else {
            db.collection("cart")
                    .document(userId)
                    .collection("items")
                    .document(item.getProductId())
                    .delete();
        }
    }

    private void updateSQLiteCart(CartItem item) {
        CartDatabaseHelper dbHelper = new CartDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (item.getQuantity() > 0) {
            ContentValues values = new ContentValues();
            values.put("quantity", item.getQuantity());
            db.update("cart", values, "productId=?", new String[]{item.getProductId()});
        } else {
            db.delete("cart", "productId=?", new String[]{item.getProductId()});
        }

        db.close();
    }

    private void removeItem(CartItem item, int position) {
        if (isLoggedIn) {
            FirebaseFirestore.getInstance()
                    .collection("cart")
                    .document(userId)
                    .collection("items")
                    .document(item.getProductId())
                    .delete();
        } else {
            SQLiteDatabase db = new CartDatabaseHelper(context).getWritableDatabase();
            db.delete("cart", "productId=?", new String[]{item.getProductId()});
            db.close();
        }

        cartItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,cartItems.size());

        if (cartUpdateListener != null) {
            cartUpdateListener.onCartUpdated();

        }
    }

    public interface CartUpdateListener {
        void onQuantityChanged(CartItem item, int newQuantity);

        void onRemoveClick(CartItem item, int position);

        void onCartUpdated();
    }
}
