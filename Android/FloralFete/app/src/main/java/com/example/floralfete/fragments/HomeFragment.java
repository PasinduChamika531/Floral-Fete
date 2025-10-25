package com.example.floralfete.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import com.example.floralfete.R;
import com.example.floralfete.SearchActivity;
import com.example.floralfete.adapter.BannerAdapter;
import com.example.floralfete.adapter.CategoryAdapter;
import com.example.floralfete.adapter.ProductAdapter;
import com.example.floralfete.model.CategoryModel;
import com.example.floralfete.model.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView categoryRecyclerView, productRecyclerView;
    private ViewPager2 bannerViewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        productRecyclerView = view.findViewById(R.id.productRecyclerView);
        TextView searchView = view.findViewById(R.id.searchView);

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
            }
        });

        setupBannerSlider();
        setupCategoryRecyclerView();
        setupProductRecyclerView();

        return view;
    }

    private void setupBannerSlider() {
        List<Integer> bannerImages = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        );
        BannerAdapter adapter = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(adapter);
    }

    private void setupCategoryRecyclerView() {
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        List<CategoryModel> categories = Arrays.asList(
                new CategoryModel("Roses", R.drawable.ic_rose),
                new CategoryModel("Sunflower", R.drawable.ic_sunflower),
                new CategoryModel("Orchids", R.drawable.ic_orchids),
                new CategoryModel("Carnation", R.drawable.ic_carnation),
                new CategoryModel("Lily", R.drawable.ic_lilies),
                new CategoryModel("Tulip", R.drawable.ic_tulip),
                new CategoryModel("Peony", R.drawable.ic_peonies)

        );
        categoryRecyclerView.setAdapter(new CategoryAdapter(categories));
    }

    private void setupProductRecyclerView() {
        productRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<ProductModel> productList = new ArrayList<>();
        ProductAdapter productAdapter = new ProductAdapter(productList,getContext());
        productRecyclerView.setAdapter(productAdapter);

        db.collection("products").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    productList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        ProductModel product = document.toObject(ProductModel.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
