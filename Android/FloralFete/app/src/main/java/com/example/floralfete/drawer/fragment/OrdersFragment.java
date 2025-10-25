package com.example.floralfete.drawer.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floralfete.R;
import com.example.floralfete.drawer.adapter.OrdersAdapter;
import com.example.floralfete.drawer.adapter.PopupOrderAdapter;
import com.example.floralfete.model.Order;
import com.example.floralfete.model.Product;
import com.example.floralfete.model.ProductModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private com.example.floralfete.drawer.adapter.OrdersAdapter ordersAdapter;
    private List<Order> orderList;
    private View tvNoOrders;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        tvNoOrders = view.findViewById(R.id.tv_no_orders);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recycler_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(getContext(),orderList);
        recyclerView.setAdapter(ordersAdapter);

        loadOrders();

        setUpRecyclerViewClickListener();

        return view;
    }

    private void loadOrders() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        if (userId == null) {
            return;
        }

        firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .whereNotEqualTo("orderStatus", "Delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            orderList.add(order);
                        }
                    }
                    ordersAdapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvNoOrders.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoOrders.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.e("OrdersFragment", "Error fetching orders", e));
    }

    // Show the Popup window
    private void showOrderDetailsPopup(Order order) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_order_details, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        RecyclerView productRecyclerView = popupView.findViewById(R.id.recycler_product_details);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Product> productList = order.getProducts();
        PopupOrderAdapter productAdapter = new PopupOrderAdapter(getContext(), productList);
        productRecyclerView.setAdapter(productAdapter);

        Button closeButton = popupView.findViewById(R.id.close_popup_button);
        closeButton.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
    }

    private void setUpRecyclerViewClickListener() {
        ordersAdapter.setOnItemClickListener(new OrdersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Order order) {
                showOrderDetailsPopup(order);
            }
        });
    }

}
