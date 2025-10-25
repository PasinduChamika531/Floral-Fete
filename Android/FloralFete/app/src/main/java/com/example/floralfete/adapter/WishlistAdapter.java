package com.example.floralfete.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.floralfete.fragments.WishlistFragment;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private Context context;
    private List<WishlistFragment.WishlistItem> wishlistItems;
    private OnItemClickListener onItemClickListener;

    public WishlistAdapter(Context context, List<WishlistFragment.WishlistItem> wishlistItems, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.wishlistItems = wishlistItems;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        WishlistFragment.WishlistItem item = wishlistItems.get(position);
        Glide.with(context).load(item.getProductImageUrl()).into(holder.productImage);
        holder.productName.setText(item.getProductName());

        holder.productImage.setOnClickListener(v -> {
            Intent gotoSingleProduct = new Intent(context, SingleProductViewActivity.class);
            gotoSingleProduct.putExtra("productId",item.getProductId());
            context.startActivity(gotoSingleProduct);
        });

        holder.removeButton.setOnClickListener(v -> onItemClickListener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(WishlistFragment.WishlistItem item);
    }

    public static class WishlistViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage,removeButton;
        TextView productName,productPrice;

        public WishlistViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.wishlist_productimage);
            productName = itemView.findViewById(R.id.wishlist_productname);
            removeButton = itemView.findViewById(R.id.wishlist_button_heart);
        }
    }
}

