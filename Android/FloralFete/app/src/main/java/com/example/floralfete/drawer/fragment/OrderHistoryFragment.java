package com.example.floralfete.drawer.fragment;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;
import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floralfete.R;
import com.example.floralfete.drawer.adapter.OrderHistoryAdapter;
import com.example.floralfete.model.Order;
//import com.example.floralfete.model.OrderProduct;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<Order> orderList;
    private FirebaseFirestore firestore;
    private String userId;
    private String userFullName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        recyclerView = view.findViewById(R.id.order_history_recycler_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        orderHistoryAdapter = new OrderHistoryAdapter(getContext(), orderList, this::showFeedbackPopup);
        recyclerView.setAdapter(orderHistoryAdapter);

        firestore = FirebaseFirestore.getInstance();

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        userFullName = prefs.getString("user_fname", "") + " " + prefs.getString("user_lname", "");

        if (userId != null) {
            loadDeliveredOrders();
        }

        return view;
    }

    private void loadDeliveredOrders() {
        firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("orderStatus", "Delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            orderList.add(order);
                        }
                    }
                    orderHistoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("OrderHistoryFragment", "Error fetching delivered orders", e));
    }

    private void showFeedbackPopup(String productId,String orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_feedback, null);
        builder.setView(dialogView);

        EditText editTextFeedback = dialogView.findViewById(R.id.order_history_edit_feedback);
        Button buttonSubmit = dialogView.findViewById(R.id.order_history_btn_submit);
        Button buttonCancel = dialogView.findViewById(R.id.order_history_btn_cancel);

        AlertDialog dialog = builder.create();
        dialog.show();

        //load existing feedback
        firestore.collection("feedbacks")
                .whereEqualTo("productId", productId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String existingFeedback = document.getString("feedback");
                        editTextFeedback.setText(existingFeedback);  // Pre-fill feedback
                        buttonSubmit.setOnClickListener(v -> updateFeedback(document.getId(), editTextFeedback.getText().toString().trim(), dialog));
                    } else {
                        buttonSubmit.setOnClickListener(v -> saveFeedbackToFirestore(productId, editTextFeedback.getText().toString().trim(), dialog,orderId));
                    }
                })
                .addOnFailureListener(e -> Log.e("OrderHistoryFragment", "Error checking feedback", e));

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
    }


    private void saveFeedbackToFirestore(String productId, String feedback, AlertDialog dialog,String orderId) {
        if (TextUtils.isEmpty(feedback)) {
            errorCustomToast("Please enter your feedback", getContext());
            return;
        }

        Feedback feedbackObject = new Feedback(productId, feedback, userFullName, userId,orderId);
        firestore.collection("feedbacks").add(feedbackObject)
                .addOnSuccessListener(documentReference -> {
                    successCustomToast("Thanks for Your Feedback", getContext());
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> Log.e("OrderHistoryFragment", "Error saving feedback", e));
    }

    private void updateFeedback(String feedbackId, String newFeedback, AlertDialog dialog) {
        if (TextUtils.isEmpty(newFeedback)) {
            errorCustomToast("Please enter your feedback", getContext());
            return;
        }

        firestore.collection("feedbacks").document(feedbackId)
                .update("feedback", newFeedback)
                .addOnSuccessListener(aVoid -> {
                    successCustomToast("Feedback updated successfully", getContext());
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> Log.e("OrderHistoryFragment", "Error updating feedback", e));
    }



    public static class Feedback {
        private String productId, feedback, username,userId,orderId;

        public Feedback() { }

        public Feedback(String productId, String feedback, String username, String userId) {
            this.productId = productId;
            this.feedback = feedback;
            this.username = username;
            this.userId = userId;
        }

        public Feedback(String productId, String feedback, String username, String userId, String orderId) {
            this.productId = productId;
            this.feedback = feedback;
            this.username = username;
            this.userId = userId;
            this.orderId = orderId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }
}
