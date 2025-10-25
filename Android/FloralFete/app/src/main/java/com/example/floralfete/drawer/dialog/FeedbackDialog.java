package com.example.floralfete.drawer.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.floralfete.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FeedbackDialog extends Dialog {

    private EditText editFeedback;
    private Button btnSubmit, btnCancel;
    private String productId;
    private Context context;

    public FeedbackDialog(@NonNull Context context, String productId) {
        super(context);
        this.context = context;
        this.productId = productId;

        setContentView(R.layout.dialog_feedback);
        setCancelable(false);

        editFeedback = findViewById(R.id.order_history_edit_feedback);
        btnSubmit = findViewById(R.id.order_history_btn_submit);
        btnCancel = findViewById(R.id.order_history_btn_cancel);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String feedbackText = editFeedback.getText().toString().trim();

        if (feedbackText.isEmpty()) {
            Toast.makeText(context, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userFname = prefs.getString("user_fname", "");
        String userLname = prefs.getString("user_lname", "");

        if (userFname.isEmpty() || userLname.isEmpty()) {
            Toast.makeText(context, "User information missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = userFname + " " + userLname;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("productId", productId);
        feedbackData.put("feedback", feedbackText);
        feedbackData.put("username", username);

        firestore.collection("feedbacks")
                .add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to submit feedback", Toast.LENGTH_SHORT).show());
    }
}
