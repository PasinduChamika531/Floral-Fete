package com.example.floralfete.fragments;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floralfete.CheckoutActivity;
import com.example.floralfete.R;
import com.example.floralfete.adapter.CartAdapter;
import com.example.floralfete.database.CartDatabaseHelper;
import com.example.floralfete.model.CartItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.CartUpdateListener {
    private RecyclerView recyclerCart;
    private TextView textTotalPrice;
    private View emptyCartLayout, cartTotalLayout;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private CartDatabaseHelper dbHelper;
    private FirebaseFirestore firestore;
    private String userId;
    private boolean isLoggedIn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerCart = view.findViewById(R.id.recycler_cart);
        textTotalPrice = view.findViewById(R.id.text_total_price);
        emptyCartLayout = view.findViewById(R.id.empty_cart_layout);
        cartTotalLayout = view.findViewById(R.id.cart_total_layout);
        Button checkoutButton = view.findViewById(R.id.button_checkout);

        dbHelper = new CartDatabaseHelper(getContext());
        firestore = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);  // Make sure the key matches
        isLoggedIn = (userId != null);

        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));

        cartAdapter = new CartAdapter(getContext(), cartItems, this);
        recyclerCart.setAdapter(cartAdapter);

        if (isLoggedIn) {
            loadCartFromFirestore();
            syncCartWithFirestore();

        } else {
            loadCartFromSQLite();
        }


        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoggedIn) {
                    Intent intentCheckout = new Intent(getContext(), CheckoutActivity.class);
                    startActivity(intentCheckout);
                }else{
                    errorCustomToast("You need to SignIn before Checkout",getContext());
                }
            }
        });
    }

    private void loadCartFromFirestore() {
        firestore.collection("cart").document(userId).collection("items")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItems.clear();
                    for (var doc : queryDocumentSnapshots) {
                        cartItems.add(doc.toObject(CartItem.class));
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateCartUI();
                });
    }

    private void loadCartFromSQLite() {
        cartItems.clear();
        cartItems.addAll(dbHelper.getAllCartItems());
        cartAdapter.notifyDataSetChanged();
        updateCartUI();
    }


    private void syncCartWithFirestore() {
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        if (userId != null) {
            List<CartItem> sqliteCartItems = dbHelper.getAllCartItems();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            for (CartItem sqliteItem : sqliteCartItems) {
                String productId = sqliteItem.getProductId();
                int sqliteQuantity = sqliteItem.getQuantity();

                firestore.collection("cart")
                        .document(userId)
                        .collection("items")
                        .document(productId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Product exists in Firestore, update the quantity
                                int firestoreQuantity = documentSnapshot.getLong("quantity").intValue();
                                int updatedQuantity = firestoreQuantity + sqliteQuantity;

                                firestore.collection("cart")
                                        .document(userId)
                                        .collection("items")
                                        .document(productId)
                                        .update("quantity", updatedQuantity)
                                        .addOnSuccessListener(aVoid -> {
                                            // Remove item from SQLite after updating Firestore
                                            dbHelper.removeCartItem(productId);
                                            loadCartFromFirestore();

                                        })
                                        .addOnFailureListener(e -> Log.e("SyncCart", "Failed to update quantity in Firestore", e));

                            } else {
                                // Product does not exist in Firestore, add it
                                firestore.collection("cart")
                                        .document(userId)
                                        .collection("items")
                                        .document(productId)
                                        .set(sqliteItem.toMap()) // Assuming toMap() converts CartItem to a Map
                                        .addOnSuccessListener(aVoid -> {
                                            // Remove item from SQLite after adding to Firestore
                                            dbHelper.removeCartItem(productId);
                                            loadCartFromFirestore();

                                        })
                                        .addOnFailureListener(e -> Log.e("SyncCart", "Failed to add cart item to Firestore", e));
                            }
                        })
                        .addOnFailureListener(e -> Log.e("SyncCart", "Failed to check Firestore for existing cart item", e));
            }
        }
    }



    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        item.setQuantity(newQuantity);
        if (isLoggedIn) {
            firestore.collection("cart").document(userId)
                    .collection("items").document(item.getProductId())
                    .update("quantity", newQuantity);
        } else {
            dbHelper.updateCartItemQuantity(item.getProductId(), newQuantity);
        }
        updateCartUI();
        // Update the item in the adapter and notify it
        int position = cartItems.indexOf(item);
        if (position != -1) {
            cartAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onRemoveClick(CartItem item, int position) {
        updateCartUI();
    }

    @Override
    public void onCartUpdated() {
        updateCartUI();
    }

    private void updateCartUI() {
        if (cartItems.isEmpty()) {
            emptyCartLayout.setVisibility(View.VISIBLE);
            recyclerCart.setVisibility(View.GONE);
            cartTotalLayout.setVisibility(View.GONE);
        } else {
            emptyCartLayout.setVisibility(View.GONE);
            recyclerCart.setVisibility(View.VISIBLE);
            cartTotalLayout.setVisibility(View.VISIBLE);
            double total = 0;
            for (CartItem item : cartItems) {
                total += item.getProductPrice() * item.getQuantity();
            }
            textTotalPrice.setText("Total: Rs." + total);
        }
    }
}
