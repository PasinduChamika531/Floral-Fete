package com.example.floralfete;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;
import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floralfete.R;
import com.example.floralfete.adapter.CheckoutCartAdapter;
import com.example.floralfete.fragments.HomeFragment;
import com.example.floralfete.model.CartItem;
import com.example.floralfete.utils.NetworkChangeReceiver;
import com.example.floralfete.utils.PayHerePaymentHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutActivity extends AppCompatActivity {
    private TextView textUserAddress, textSubtotal, textShipping, textTotal;
    private RecyclerView recyclerViewCart;
    private Button buttonPlaceOrder, buttonChangeAddress, selectDeliveryDate;
    private FirebaseFirestore firestore;
    private String userId, tempAddress, orderId, firstName, lastName, email, mobile;
    private double totalAmount;
    private List<CartItem> cartItems;
    private CheckoutCartAdapter cartAdapter;
    private static final double SHIPPING_FEE = 300.00;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.white));

        }

        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        textUserAddress = findViewById(R.id.text_user_address);
        textSubtotal = findViewById(R.id.text_subtotal);
        textShipping = findViewById(R.id.text_shipping);
        textTotal = findViewById(R.id.text_total_price);
        recyclerViewCart = findViewById(R.id.recycler_checkout_cart);
        buttonPlaceOrder = findViewById(R.id.button_place_order);
        buttonChangeAddress = findViewById(R.id.button_change_address);
        selectDeliveryDate = findViewById(R.id.selectDeliveryDate);

        firestore = FirebaseFirestore.getInstance();
        cartItems = new ArrayList<>();
        cartAdapter = new CheckoutCartAdapter(this, cartItems);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);

        // Delivery Date Picker
        setupDeliveryDatePicker();

        // Get userId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);

        if (userId != null) {
            loadUserAddress();
            loadCartItems();
        }

        buttonChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddressDialog();
            }
        });

        buttonPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectDeliveryDate.getText().equals("Choose Date")) {
                    errorCustomToast("Please Select a Delivary Date", CheckoutActivity.this);
                } else if (tempAddress.isEmpty()) {
                    errorCustomToast("Please add a Address and Contact Detials", CheckoutActivity.this);
                } else {
                    placeOrder();
                }

            }
        });
    }

//    private void loadUserAddress() {
//        firestore.collection("address").document(userId)
//                .get().addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot.exists()) {
//                            tempAddress = documentSnapshot.getString("address");
//                            textUserAddress.setText(tempAddress);
//                        } else {
//                            textUserAddress.setText("No address found. Please add one.");
//                        }
//                    }
//                });
//    }

    private void loadUserAddress() {
        // Get user details from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        firstName = prefs.getString("user_fname", "");
        lastName = prefs.getString("user_lname", "");
        mobile = prefs.getString("user_mobile", "");
        email = prefs.getString("email", "");

        firestore.collection("address").document(userId)
                .get().addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String address = documentSnapshot.getString("address");

                            String formattedAddress = "";
                            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                                formattedAddress += firstName + " " + lastName + ", ";
                            }
                            if (!mobile.isEmpty()) {
                                formattedAddress += mobile + "\n";
                            }
                            if (address != null && !address.isEmpty()) {
                                formattedAddress += address;
                            }

                            textUserAddress.setText(formattedAddress);
                            tempAddress = formattedAddress;
                        } else {
                            textUserAddress.setHint("No address found. Please add one.");
                            tempAddress = "";
                        }
                    }
                });
    }


    private void loadCartItems() {
        firestore.collection("cart").document(userId).collection("items")
                .get().addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
                        cartItems.clear();
                        double subtotal = 0;
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null) {
                                cartItems.add(item);
                                subtotal += item.getProductPrice() * item.getQuantity();
                            }
                        }
                        cartAdapter.notifyDataSetChanged();
                        updatePriceDetails(subtotal);
                    }
                });
    }

    private void updatePriceDetails(double subtotal) {
        double total = subtotal + SHIPPING_FEE;
        totalAmount = total;
        textSubtotal.setText("Subtotal: Rs." + String.format("%.2f", subtotal));
        textShipping.setText("Shipping: Rs." + String.format("%.2f", SHIPPING_FEE));
        textTotal.setText("Total: Rs." + String.format("%.2f", total));
    }

    private void showAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_address, null);
        EditText editAddress = view.findViewById(R.id.edit_address);
        Button saveAddressButton = view.findViewById(R.id.button_save_address);
        editAddress.setText(tempAddress);

        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        saveAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempAddress = editAddress.getText().toString().trim();
                textUserAddress.setText(tempAddress);
                alertDialog.dismiss();

            }
        });

        // builder.show();
    }

    private void setupDeliveryDatePicker() {
        selectDeliveryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        CheckoutActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                            selectDeliveryDate.setText(selectedDate);
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });
    }


    private void processOrderAfterPayment() {
        if (cartItems.isEmpty()) {
            errorCustomToast("No items in the cart!", CheckoutActivity.this);
            return;
        }

        String orderId = firestore.collection("orders").document().getId(); // Generate a new order ID
        List<Map<String, Object>> orderProducts = new ArrayList<>();

        for (CartItem item : cartItems) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", item.getProductId());
            productData.put("name", item.getProductName());
            productData.put("price", item.getProductPrice());
            productData.put("quantity", item.getQuantity());
            orderProducts.add(productData);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", userId);
        orderData.put("products", orderProducts);
        orderData.put("totalAmount", totalAmount);
        orderData.put("deliveryAddress", tempAddress);
        orderData.put("orderStatus", "Processing");
        orderData.put("userMobile", mobile);
        orderData.put("orderedDatetime", System.currentTimeMillis());
        orderData.put("deliveryDate", selectDeliveryDate.getText().toString());



        firestore.collection("orders").document(orderId).set(orderData)
                .addOnSuccessListener(aVoid -> {

                    removeCartItems();

                    updateProductQuantities();
                    successCustomToast("Order placed successfully!", CheckoutActivity.this);
                    finish(); // Close the checkout activity
                    Intent gotointent = new Intent(CheckoutActivity.this, MainActivity.class);
                    startActivity(gotointent);
                })
                .addOnFailureListener(e -> errorCustomToast("Failed to place order!", CheckoutActivity.this));
    }


    private void removeCartItems() {
        for (CartItem item : cartItems) {
            firestore.collection("cart").document(userId)
                    .collection("items").document(item.getProductId())
                    .delete();
        }
    }


    private void updateProductQuantities() {
        for (CartItem item : cartItems) {
            firestore.collection("products").document(item.getProductId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            long currentQty = documentSnapshot.getLong("qty");
                            long newQty = currentQty - item.getQuantity();
                            if (newQty < 0) newQty = 0; // Prevent negative stock

                            firestore.collection("products").document(item.getProductId())
                                    .update("qty", newQty);
                        }
                    });
        }
    }


    private void placeOrder() {

        orderId = userId;

        InitRequest req = new InitRequest();
        req.setMerchantId("1221214");       // Merchant ID
        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
        req.setAmount(totalAmount);             // Final Amount to be charged
        req.setOrderId("230000123");        // Unique Reference ID
        req.setItemsDescription("Floral Fete Cart");  // Item description title
        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName(firstName);
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, 11001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11001 && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                String msg;
                if (response != null) {
                    if (response.isSuccess()) {
                        msg = "Activity result:" + response.getData().toString();
                        Log.d("FloarlFeteLog", msg);
                        successCustomToast("Payment Successful!", CheckoutActivity.this);
                        processOrderAfterPayment();

                    } else {
                        msg = "Result:" + response.toString();
                        Log.d("FloarlFeteLog", msg);
                    }
                } else {
                    msg = "Result: no response";
                    Log.d("FloarlFeteLog", msg);
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null) {

                    Log.d("FloarlFeteLog", response.toString());
                    Log.d("FloarlFeteLog", "respose.toString");


                } else {

                    Log.d("FloarlFeteLog", "User Canceled the request");
                }

            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

}
