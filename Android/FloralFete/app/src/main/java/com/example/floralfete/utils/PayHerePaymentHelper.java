package com.example.floralfete.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class PayHerePaymentHelper {
    private static final String TAG = "PayHerePaymentHelper";

    private static final String MERCHANT_ID = "1221214";
    private static final String MERCHANT_SECRET = "MTMzMzcxMTE4MjI3MTk2MDYzMjMzMTIwNDQzOTcwMTkxOTQwOTcxMg==";
    private static final String CURRENCY = "LKR";
    private static final boolean IS_SANDBOX = true;


    public static void startPayment(Activity activity, double totalAmount, String orderId,
                                    String firstName, String lastName, String email,
                                    String phone) {
        // Create the InitRequest with order details
        InitRequest request = new InitRequest();
        request.setMerchantId(MERCHANT_ID);
        request.setMerchantSecret(MERCHANT_SECRET);
        request.setAmount(totalAmount);
        request.setCurrency(CURRENCY);
        request.setOrderId(orderId);
        request.setItemsDescription("Flower Purchase");

        // Customer details
        request.getCustomer().setFirstName(firstName);
        request.getCustomer().setLastName(lastName);
        request.getCustomer().setEmail(email);
        request.getCustomer().setPhone(phone);
        request.getCustomer().getAddress().setAddress("No.1, Galle Road");
        request.getCustomer().getAddress().setCity("Colombo");
        request.getCustomer().getAddress().setCountry("Sri Lanka");



        request.setSandBox(IS_SANDBOX);


        Intent intent = new Intent(activity, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, request);


        activity.startActivityForResult(intent, 1001);
    }


    // Handle Payment Result (Success or Failure)
//    public static void handlePaymentResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 1001) {
//            if (resultCode == Activity.RESULT_OK) {
//                // Payment was successful
//                StatusResponse statusResponse = data.getParcelableExtra(PHConstants.INTENT_EXTRA_RESULT);
//                if (statusResponse != null && statusResponse.getStatusCode().equals("2")) {
//                    Log.d(TAG, "Payment Success: " + statusResponse.toString());
//                } else {
//                    Log.e(TAG, "Payment Failed: " + statusResponse);
//                }
//            } else {
//                Log.e(TAG, "Payment was cancelled or failed.");
//            }
//        }
//    }
}
