package com.example.floralfete.drawer.adapter;

import android.content.Context;
import android.util.Log;
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
import com.example.floralfete.model.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OrderHistoryAdapter.FeedbackListener feedbackListener;
    private String orderId;

    public OrderProductAdapter(Context context, List<Product> productList, OrderHistoryAdapter.FeedbackListener feedbackListener,String orderId) {
        this.context = context;
        this.productList = productList;
        this.feedbackListener = feedbackListener;
        this.orderId = orderId;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.textProductName.setText(product.getName());
        holder.textProductPrice.setText("Rs. " + String.format("%.2f", product.getPrice()));
        holder.textProductQuantity.setText("Qty: " + product.getQuantity());

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
                            Glide.with(context).load(firstImageUrl).into(holder.imageProduct);
                        }
                    } else {
                        Log.d("FloralFeteLog", "Product not found with the provided productId");
                    }
                })
                .addOnFailureListener(e -> Log.e("FloralFeteLog", "Error getting documents", e));

       // Glide.with(context).load(product.getImageUrl()).into(holder.imageProduct);

        //feedback button
        holder.buttonGiveFeedback.setOnClickListener(v -> {
            if (feedbackListener != null) {
                feedbackListener.onGiveFeedback(product.getProductId(),orderId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName, textProductPrice, textProductQuantity;
        Button buttonGiveFeedback;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.order_history_product_image);
            textProductName = itemView.findViewById(R.id.order_history_product_name);
            textProductPrice = itemView.findViewById(R.id.order_history_product_price);
            textProductQuantity = itemView.findViewById(R.id.order_history_product_quantity);
            buttonGiveFeedback = itemView.findViewById(R.id.order_history_feedback_button);
        }
    }
}
