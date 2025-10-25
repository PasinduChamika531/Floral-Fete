package com.example.floralfete.drawer.adapter;

import android.content.Context;
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
import com.example.floralfete.model.Product;
import com.example.floralfete.model.ProductModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class PopupOrderAdapter extends RecyclerView.Adapter<PopupOrderAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public PopupOrderAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(com.example.floralfete.R.layout.item_popup_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText("Rs. " + String.format("%.2f", product.getPrice()));
        holder.productQuantity.setText("Quantity: " + product.getQuantity());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("id", product.getProductId()) // Filter by productId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> imageUrlList = (List<String>) document.get("imageUrls");
                        if (imageUrlList != null && !imageUrlList.isEmpty()) {
                            String firstImageUrl = imageUrlList.get(0);
                            Log.d("FloralFeteLog", "First image URL: " + firstImageUrl);
                            Glide.with(context).load(firstImageUrl).into(holder.productImage);
                        }
                    } else {
                        Log.d("FloralFeteLog", "Product not found with the provided productId");
                    }
                })
                .addOnFailureListener(e -> Log.e("FloralFeteLog", "Error getting documents", e));




    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;
        ImageView productImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.itempopup_product_name);
            productPrice = itemView.findViewById(R.id.itempopup_product_price);
            productQuantity = itemView.findViewById(R.id.itempopup_product_quantity);
            productImage = itemView.findViewById(R.id.itempopup_product_image);
        }
    }
}

