package com.example.floralfete.drawer.fragment;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;
import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.floralfete.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddressFragment extends Fragment {
    private EditText addressEditText;
    private Button addressButton;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);

        addressEditText = view.findViewById(R.id.address_edittext1);
        addressButton = view.findViewById(R.id.address_button1);
        firestore = FirebaseFirestore.getInstance();

        // Get userId
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);

        if (userId != null) {
            loadAddress();
        }

        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrUpdateAddress();
            }
        });

        return view;
    }

    private void loadAddress() {
        firestore.collection("address").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String address = task.getResult().getString("address");
                            addressEditText.setText(address);
                        }
                    }
                });
    }

    private void saveOrUpdateAddress() {
        String address = addressEditText.getText().toString().trim();

        if (address.isEmpty()) {
            errorCustomToast("Please enter an Address",getContext());
            return;
        }

        Map<String, Object> addressData = new HashMap<>();
        addressData.put("userId", userId);
        addressData.put("address", address);

        firestore.collection("address").document(userId)
                .set(addressData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            successCustomToast("Address saved",getContext());
                        } else {
                            errorCustomToast("Failed to save address",getContext());

                        }
                    }
                });
    }
}
