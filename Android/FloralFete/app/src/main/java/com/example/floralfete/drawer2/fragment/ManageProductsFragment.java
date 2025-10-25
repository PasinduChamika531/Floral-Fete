package com.example.floralfete.drawer2.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floralfete.R;
import com.example.floralfete.model.ProductModel;
import com.example.floralfete.drawer2.fragment.AddProductDialog;
import com.example.floralfete.drawer2.adapter.ManageProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;



public class ManageProductsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ManageProductAdapter productAdapter;
    private List<ProductModel> productList;
    private FirebaseFirestore db;
    private FloatingActionButton addProductFab;

    public ManageProductsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_products, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewManageProducts);
        addProductFab = view.findViewById(R.id.fab_add_product);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productList = new ArrayList<>();
        productAdapter = new ManageProductAdapter(getContext(), productList);
        recyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();
        loadProducts();

        addProductFab.setOnClickListener(v -> {

            AddProductDialog addProductDialog = AddProductDialog.newInstance();
            addProductDialog.show(requireActivity().getSupportFragmentManager(), "AddProductDialog");

        });



        return view;
    }

    private void loadProducts() {
        CollectionReference productsRef = db.collection("products");
        productsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getContext(), "Error loading products", Toast.LENGTH_SHORT).show();
                    return;
                }

                productList.clear();
                if (value != null) {
                    productList.addAll(value.toObjects(ProductModel.class));
                }
                productAdapter.notifyDataSetChanged();
            }
        });
    }
}
