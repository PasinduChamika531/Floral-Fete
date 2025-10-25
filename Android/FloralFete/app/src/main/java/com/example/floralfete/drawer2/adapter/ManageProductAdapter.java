package com.example.floralfete.drawer2.adapter;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;
import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.floralfete.R;
import com.example.floralfete.drawer2.fragment.EditProductDialog;
import com.example.floralfete.model.ProductModel;
import com.example.floralfete.drawer2.fragment.AddProductDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ManageProductAdapter extends RecyclerView.Adapter<ManageProductAdapter.ProductViewHolder> {

    private Context context;
    private List<ProductModel> productList;
    private FirebaseFirestore db;

    public ManageProductAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText("Rs." + product.getPrice());

        if (!product.getImageUrls().isEmpty()) {
            Glide.with(context).load(product.getImageUrls().get(0)).into(holder.productImage);
        }

        holder.editProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProductDialog editDialog = EditProductDialog.newInstance(product);
                editDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditProductDialog");
            }
        });


        holder.deleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productId = product.getId();
                StorageReference productFolder = FirebaseStorage.getInstance().getReference("products/" + productId + "/");

                deleteProductImages(productFolder, productId, position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        ImageView productImage, editProduct, deleteProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textProductName);
            productPrice = itemView.findViewById(R.id.textProductPrice);
            productImage = itemView.findViewById(R.id.imageProduct);
            editProduct = itemView.findViewById(R.id.imageEdit);
            deleteProduct = itemView.findViewById(R.id.imageDelete);
        }
    }



    private void deleteProductImages ( final StorageReference productFolder, final String productId, final int position){
        productFolder.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                final int totalFiles = listResult.getItems().size();

                if (totalFiles == 0) {

                    deleteProductFromFirestore(productId, position);
                    return;
                }

                final int[] deleteCount = {0};

                for (StorageReference fileRef : listResult.getItems()) {
                    fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            deleteCount[0]++;
                            if (deleteCount[0] == totalFiles) {

                                deleteProductFromFirestore(productId, position);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FloralFeteLog", "Failed to delete image: " + fileRef.getPath(), e);
                            errorCustomToast("Failed to delete product images",context);

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FloralFeteLog", "Failed to list product images for deletion", e);
                //Toast.makeText(context, "Failed to retrieve product images", Toast.LENGTH_SHORT).show();
                errorCustomToast("Failed to retrieve product images",context);

            }
        });
    }


    private void deleteProductFromFirestore ( final String productId, final int position){
        db.collection("products").document(productId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        if (position >= 0 && position < productList.size()) {
                            productList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position,productList.size());
                            successCustomToast("Product Deleted",context);

                        }

                        successCustomToast("Product Deleted",context);


                        if (productList.isEmpty()) {
                            notifyDataSetChanged();
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FloralFeteLog", "Failed to delete product", e);
                        errorCustomToast("Delete Failed",context);
                    }
                });
    }
}
