package com.example.floralfete.drawer2.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.floralfete.R;
import com.example.floralfete.drawer2.adapter.ManageOrdersAdapter;
import com.example.floralfete.model.OrderModel;
import com.example.floralfete.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ManageOrdersAdapter adapter;
    private List<OrderModel> orderList;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;

    public ManageOrdersFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_orders, container, false);

        recyclerView = view.findViewById(R.id.manage_orders_recycler_view);
        progressBar = view.findViewById(R.id.manage_orders_progress_bar);
        firestore = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ManageOrdersAdapter(getContext(), orderList);
        recyclerView.setAdapter(adapter);

        loadOrders();
        return view;
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);

        firestore.collection("orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String orderId = document.getId();
                        String userId = document.getString("userId");
                        String userMobile = document.getString("userMobile");
                       // String orderedDatetime = document.getString("orderedDatetime");
                        String deliveryAddress = document.getString("deliveryAddress");
                        String deliveryDate = document.getString("deliveryDate");

                        long currentTimeMillis = document.getLong("orderedDatetime");
                        Date date = new Date(currentTimeMillis);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
                        String orderedDatetime = sdf.format(date);

                        Double totalAmountDouble = document.getDouble("totalAmount");
                        double totalAmount = (totalAmountDouble != null) ? totalAmountDouble : 0.0;
                        String orderStatus = document.getString("orderStatus");

                        // Get product list
                        List<Product> productList = new ArrayList<>();
                        List<?> productsData = (List<?>) document.get("products");
                        if (productsData != null) {
                            for (Object obj : productsData) {
                                if (obj instanceof java.util.Map) {
                                    java.util.Map<?, ?> productMap = (java.util.Map<?, ?>) obj;
                                    String name = (String) productMap.get("name");

                                    
                                    long qty = 1;
                                    Object quantityObject = productMap.get("quantity");
                                    if (quantityObject instanceof Long) {
                                        qty = (Long) quantityObject;
                                    } else if (quantityObject instanceof Integer) {
                                        qty = ((Integer) quantityObject).longValue();
                                    }

                                    productList.add(new Product(name, (int) qty));
                                }
                            }
                        }

                        orderList.add(new OrderModel(orderId, userId, userMobile, orderedDatetime,
                                deliveryAddress, deliveryDate, totalAmount, orderStatus, productList));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
