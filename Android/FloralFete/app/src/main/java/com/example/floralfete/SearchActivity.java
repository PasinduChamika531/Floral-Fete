package com.example.floralfete;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floralfete.adapter.ProductAdapter;
import com.example.floralfete.model.ProductModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchBar;
    private Spinner categorySpinner, occasionSpinner, sortSpinner;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<ProductModel> allProducts = new ArrayList<>();
    private List<ProductModel> filteredProducts = new ArrayList<>();

    private List<String> categoryList = new ArrayList<>();
    private List<String> occasionList = new ArrayList<>();

    private FirebaseFirestore db;
    private CollectionReference productsRef, flowerTypesRef, occasionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.white));

        }

        searchBar = findViewById(R.id.search_bar);
        categorySpinner = findViewById(R.id.category_spinner);
        occasionSpinner = findViewById(R.id.occasion_spinner);
        sortSpinner = findViewById(R.id.sort_spinner);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(filteredProducts,this);
        recyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();
        productsRef = db.collection("products");
        flowerTypesRef = db.collection("flowerTypes");
        occasionsRef = db.collection("occasions");

        loadCategories();
        loadOccasions();
        loadAllProducts();
        setupSearchBar();
        setupSortSpinner();
    }

    private void loadCategories() {
        categoryList.clear();
        categoryList.add("By FlowerType"); // Default option
        flowerTypesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    categoryList.add(document.getId()); // Document ID is the category name
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner1, categoryList);
                adapter.setDropDownViewResource(R.layout.custom_spinner_item1);
                categorySpinner.setAdapter(adapter);

                categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        filterProducts();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        });
    }

    private void loadOccasions() {
        occasionList.clear();
        occasionList.add("By Occasion"); // Default option
        occasionsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    occasionList.add(document.getString("name")); // Document ID is the occasion name
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner1, occasionList);
                adapter.setDropDownViewResource(R.layout.custom_spinner_item1);
                occasionSpinner.setAdapter(adapter);

                occasionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        filterProducts();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        });
    }

    private void loadAllProducts() {
        productsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allProducts.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    ProductModel product = document.toObject(ProductModel.class);
                    allProducts.add(product);
                }
                filteredProducts.addAll(allProducts);
                productAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, R.layout.custom_spinner1);
        sortAdapter.setDropDownViewResource(R.layout.custom_spinner_item1);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterProducts() {
        String searchText = searchBar.getText().toString().toLowerCase();
        String selectedCategory = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : "By FlowerType";
        String selectedOccasion = occasionSpinner.getSelectedItem() != null ? occasionSpinner.getSelectedItem().toString() : "By Occasion";

        filteredProducts.clear();

        for (ProductModel product : allProducts) {
            boolean matchesSearch = product.getName() != null && product.getName().toLowerCase().contains(searchText);

            boolean matchesCategory = selectedCategory.equals("By FlowerType") ||
                    (product.getFlowerTypeId() != null && product.getFlowerTypeId().equals(selectedCategory));

            boolean matchesOccasion = selectedOccasion.equals("By Occasion") ||
                    (product.getOccasionIds() != null && product.getOccasionIds().contains(selectedOccasion));

            if (matchesSearch && matchesCategory && matchesOccasion) {
                filteredProducts.add(product);
            }
        }

        productAdapter.notifyDataSetChanged();
    }


    private void sortProducts() {
        String sortOption = sortSpinner.getSelectedItem().toString();

        if (sortOption.equals("Price: Low to High")) {
            Collections.sort(filteredProducts, Comparator.comparingDouble(ProductModel::getPrice));
        } else if (sortOption.equals("Price: High to Low")) {
            Collections.sort(filteredProducts, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        }

        productAdapter.notifyDataSetChanged();
    }
}
