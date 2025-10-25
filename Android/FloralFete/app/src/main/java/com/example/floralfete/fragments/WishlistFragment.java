package com.example.floralfete.fragments;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.floralfete.R;
import com.example.floralfete.adapter.WishlistAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private WishlistAdapter wishlistAdapter;
    private List<WishlistItem> wishlistItems;
    private FirebaseFirestore firestore;
    private String userId;
    private View view1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);
        view1 = view;

        recyclerView = view.findViewById(R.id.wishlist_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        wishlistItems = new ArrayList<>();
        wishlistAdapter = new WishlistAdapter(getContext(), wishlistItems, this::removeFromWishlist);
        recyclerView.setAdapter(wishlistAdapter);

        firestore = FirebaseFirestore.getInstance();

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);

        if (userId != null) {
            loadWishlistItems();

        }

        if (userId == null) {
            view1.findViewById(R.id.empty_wishlist_layout).setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void loadWishlistItems() {
        firestore.collection("wishlist")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    wishlistItems.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        WishlistItem item = doc.toObject(WishlistItem.class);
                        if (item != null) {
                            wishlistItems.add(item);
                        }
                    }
                    wishlistAdapter.notifyDataSetChanged();
                    updateWishlistUI();
                })
                .addOnFailureListener(e -> Log.e("WishlistFragment", "Error fetching wishlist items", e));
    }

    private void removeFromWishlist(WishlistItem item) {
        String docId = userId + "_" + item.getProductId();
        firestore.collection("wishlist")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    wishlistItems.remove(item);
                    wishlistAdapter.notifyDataSetChanged();
                    errorCustomToast("Product Removed from WishList", getContext());
                    updateWishlistUI();
                })
                .addOnFailureListener(e -> Log.e("WishlistFragment", "Error removing item from wishlist", e));
    }

    private void updateWishlistUI(){
        if (wishlistItems.isEmpty()) {
            view1.findViewById(R.id.empty_wishlist_layout).setVisibility(View.VISIBLE);
        }else{
            view1.findViewById(R.id.empty_wishlist_layout).setVisibility(View.GONE);
        }
    }

    public static class WishlistItem {
        private String productId;
        private String productImageUrl;
        private String productName;
        private String userId;

        public WishlistItem() {
        }

        public WishlistItem(String productId, String productImageUrl, String productName, String userId) {
            this.productId = productId;
            this.productImageUrl = productImageUrl;
            this.productName = productName;
            this.userId = userId;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductImageUrl() {
            return productImageUrl;
        }

        public String getProductName() {
            return productName;
        }

        public String getUserId() {
            return userId;
        }
    }
}
