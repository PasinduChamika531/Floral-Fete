package com.example.floralfete;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;
import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.example.floralfete.adapter.FeedbackAdapter;
import com.example.floralfete.adapter.ImageSliderAdapter;
import com.example.floralfete.database.CartDatabaseHelper;
import com.example.floralfete.model.CartItem;
import com.example.floralfete.model.FeedbackModel;
import com.example.floralfete.utils.NetworkChangeReceiver;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleProductViewActivity extends AppCompatActivity implements SensorEventListener {

    private ViewPager2 productImageViewPager;
    private TabLayout imageIndicator;
    private TextView productName, productPrice, productAvailableQty, quantityText, productDescription;
    private Button decreaseQuantity, increaseQuantity, selectDeliveryDate, addToWishlistButton, addToCartButton,productFlowerType;
    private RecyclerView feedbackRecyclerView;

    private int quantity = 1;
    private int availableQuantity = 10;
    private List<String> imageUrls = new ArrayList<>();
    private FirebaseFirestore db;
    private String productId;
    private boolean isLiked = false;
    private LottieAnimationView likeButton;
    private String wishuserId;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration, currentAcceleration, lastAcceleration;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_single_product_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.white));

        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        db = FirebaseFirestore.getInstance();


        productImageViewPager = findViewById(R.id.productImageViewPager);
        imageIndicator = findViewById(R.id.imageIndicator);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productAvailableQty = findViewById(R.id.productAvailableQty);
        quantityText = findViewById(R.id.quantityText);
        decreaseQuantity = findViewById(R.id.decreaseQuantity);
        increaseQuantity = findViewById(R.id.increaseQuantity);

        productDescription = findViewById(R.id.productDescription);
        productFlowerType = findViewById(R.id.productFlowerType);
        feedbackRecyclerView = findViewById(R.id.feedbackRecyclerView);
        addToCartButton = findViewById(R.id.addToCartButton);
        likeButton = findViewById(R.id.likeButton);



        productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            fetchProductDetails(productId);
            fetchProductFeedback(productId); // Load feedback after product details
        } else {
            Toast.makeText(this, "Product not found!", Toast.LENGTH_SHORT).show();
            finish();
        }


        setupQuantitySelector();


        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs",MODE_PRIVATE);
        wishuserId = sharedPreferences.getString("user_id",null);


        checkWishlistStatus();

        //shake detection
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLiked) {
                    removeFromWishlist();
                } else {
                    addToWishlist();
                }
            }
        });


        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double price = 0.0;
                try {
                    price = Double.parseDouble(productPrice.getText().toString().replace("Rs.", "").trim());
                } catch (NumberFormatException e) {
                    Log.e("SingleProductView", "Invalid price format");
                    Toast.makeText(SingleProductViewActivity.this, "Invalid price format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get login status
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.contains("user_id");

                CartItem cartItem = new CartItem(
                        productId,
                        productName.getText().toString(),
                        imageUrls.get(0),
                        price,
                        quantity
                );

                if (isLoggedIn) {
                    addToFirestoreCart(cartItem);
                } else {
                    addToSQLiteCart(cartItem);
                }
                Log.e("CartActivity", "Displaying toast with productId: " + cartItem.getProductId());
                Log.d("CartActivity", "Item added to cart: " + cartItem.getProductName());
                Log.d("CartActivity", "Item added to cart: " + cartItem.getProductImage());


            }
        });

    }

    private void addToSQLiteCart(CartItem item) {
        CartDatabaseHelper dbHelper = new CartDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if the product already exists in the cart
        Cursor cursor = db.query("cart", new String[]{"productId", "quantity"},
                "productId = ?", new String[]{item.getProductId()},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Product exists, update quantity
            int currentQuantity = cursor.getInt(cursor.getColumnIndex("quantity"));
            int updatedQuantity = currentQuantity + item.getQuantity();

            ContentValues values = new ContentValues();
            values.put("quantity", updatedQuantity);

            db.update("cart", values, "productId=?", new String[]{item.getProductId()});
            successCustomToast("Quantity Updated",this);

        } else {
            // Product doesn't exist, add new item
            ContentValues values = new ContentValues();
            values.put("productId", item.getProductId());
            values.put("productName", item.getProductName());
            values.put("productPrice", item.getProductPrice());
            values.put("productImage", item.getProductImage());
            values.put("quantity", item.getQuantity());

            db.insert("cart", null, values);

            successCustomToast("Product Added To Cart",this);
        }

        cursor.close();
        db.close();
    }


    private void addToFirestoreCart(CartItem item) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.contains("user_id");
        String userId = sharedPreferences.getString("user_id", null);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (isLoggedIn) {
            // Check if the product already exists in the cart
            db.collection("cart")
                    .document(userId)
                    .collection("items")
                    .document(item.getProductId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                // Product already in the cart, update quantity
                                CartItem existingItem = task.getResult().toObject(CartItem.class);
                                int updatedQuantity = existingItem.getQuantity() + item.getQuantity();
                                existingItem.setQuantity(updatedQuantity);
                                db.collection("cart")
                                        .document(userId)
                                        .collection("items")
                                        .document(item.getProductId())
                                        .set(existingItem.toMap(), SetOptions.merge()); // Update the existing item
                                successCustomToast("Quantity Updated",this);
                            } else {
                                // Product not in the cart, add new item
                                db.collection("cart")
                                        .document(userId)
                                        .collection("items")
                                        .document(item.getProductId())
                                        .set(item.toMap());

                                successCustomToast("Product Added To Cart",this);
                            }
                        }
                    });
        }
    }



    private void fetchProductDetails(String productId) {
        DocumentReference productRef = db.collection("products").document(productId);
        productRef.get().addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    productName.setText(documentSnapshot.getString("name"));
                    productPrice.setText("Rs." + documentSnapshot.getDouble("price"));
                    availableQuantity = documentSnapshot.getLong("qty").intValue();
                    productAvailableQty.setText("Available: " + availableQuantity);
                    productDescription.setText(documentSnapshot.getString("description"));
                    productFlowerType.setText(documentSnapshot.getString("flowerTypeId"));

                    // Load Images
                    List<String> images = (List<String>) documentSnapshot.get("imageUrls");
                    if (images != null && !images.isEmpty()) {
                        imageUrls.clear();
                        imageUrls.addAll(images);
                        setupImageSlider();
                    }
                } else {
                    Toast.makeText(SingleProductViewActivity.this, "Product details not found!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "Failed to fetch product details", e);
                Toast.makeText(SingleProductViewActivity.this, "Error fetching product details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkWishlistStatus() {
        String docId = wishuserId + "_" + productId;
        db.collection("wishlist").document(docId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            isLiked = true;
                            likeButton.setProgress(0.5f); // Show as liked
                        } else {
                            isLiked = false;
                            likeButton.setProgress(0.0f); // Show as unliked

                        }
                    }
                });
    }

    private void addToWishlist() {
        if (wishuserId != null) {

            String docId = wishuserId + "_" + productId;
            Map<String, Object> wishlistItem = new HashMap<>();
            wishlistItem.put("productId", productId);
            wishlistItem.put("productImageUrl", imageUrls.get(0));
            wishlistItem.put("userId", wishuserId);
            wishlistItem.put("productName", productName.getText().toString());

            db.collection("wishlist").document(docId).set(wishlistItem)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            likeButton.setSpeed(1f); // Play forward animation
                            likeButton.playAnimation();
                            isLiked = true;
                        }
                    });
        }else{
            errorCustomToast("To use Wishlist please Signin",SingleProductViewActivity.this);
        }
    }

    private void removeFromWishlist() {
        String docId = wishuserId + "_" + productId;
        db.collection("wishlist").document(docId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        likeButton.setSpeed(-2f); // Play animation in reverse
                        likeButton.playAnimation();
                        isLiked = false;
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // Shake detection
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            acceleration = currentAcceleration - lastAcceleration;

            // Detect a shake
            if (acceleration > 12) {
                if (!isLiked) {
                    addToWishlist();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void fetchProductFeedback(String productId) {
        db.collection("feedbacks")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        List<FeedbackModel> feedbackList = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            FeedbackModel model = document.toObject(FeedbackModel.class);
                            String feedback = document.getString("feedback"); // Fetch feedback field
                            if (model != null) {
                                feedbackList.add(model);
                            }
                        }
                        setupFeedbackSection(feedbackList);
                    }
                }).addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Failed to fetch feedback", e);
                    }
                });
    }


    private void setupImageSlider() {
        ImageSliderAdapter adapter = new ImageSliderAdapter(SingleProductViewActivity.this,imageUrls);
        productImageViewPager.setAdapter(adapter);

        new TabLayoutMediator(imageIndicator, productImageViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
            }
        }).attach();
    }

    private void setupQuantitySelector() {
        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    quantity--;
                    quantityText.setText(String.valueOf(quantity));
                }
            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity < availableQuantity) {
                    quantity++;
                    quantityText.setText(String.valueOf(quantity));
                } else {
                    errorCustomToast("Max quantity Reached!",SingleProductViewActivity.this);
                }
            }
        });
    }



    private void setupFeedbackSection(List<FeedbackModel> feedbackList) {
        FeedbackAdapter feedbackAdapter = new FeedbackAdapter(feedbackList);
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackRecyclerView.setAdapter(feedbackAdapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}
