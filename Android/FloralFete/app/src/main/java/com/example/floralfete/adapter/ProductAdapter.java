package com.example.floralfete.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.floralfete.R;
import com.example.floralfete.SingleProductViewActivity;
import com.example.floralfete.model.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<ProductModel> productList;
    private Context context;

    public ProductAdapter(List<ProductModel> productList) {
        this.productList = productList;
    }

    public ProductAdapter(List<ProductModel> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);
        holder.name.setText(product.getName());
        holder.price.setText("Rs." + product.getPrice());

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(product.getImageUrls().get(0)) // Load the first image
                    .placeholder(R.drawable.ic_lilies)
                    .into(holder.imageView);
        }

        //search Wishlist for items
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id","");

        String wishlistId = userId+"_"+product.getId();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("wishlist").document(wishlistId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                           DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.i("FloralFeteLog","have wishlist");
                                holder.wishlistButton.setImageResource(R.drawable.ic_filled_heart);
                            }
                        }
                    }
                });



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context != null) {  // Prevent NullPointerException
                    Intent intent = new Intent(context, SingleProductViewActivity.class);
                    intent.putExtra("productId", product.getId());
                    context.startActivity(intent);
                }else{
                    Log.i("FloralFeteLog","Context is Null");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        ImageView imageView,wishlistButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            imageView = itemView.findViewById(R.id.productImage);
            wishlistButton = itemView.findViewById(R.id.addToCartButton);
        }
    }
}

