package com.example.floralfete;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;
import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.io.IOException;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText forgotEmail, forgotCode, forgotNewPassword;
    private Button sendCodeButton, updatePasswordButton;
    private String receivedCode = "",userEmail;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        forgotEmail = findViewById(R.id.forgotp_edittext1);
        forgotCode = findViewById(R.id.forgotp_edittext2);
        forgotNewPassword = findViewById(R.id.forgotp_edittext3);
        sendCodeButton = findViewById(R.id.forgotp_button1);
        updatePasswordButton = findViewById(R.id.forgotp_button2);

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodeToBackend();
            }
        });


        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCodeAndResetPassword();
            }
        });
    }

    private void sendCodeToBackend() {
         userEmail = forgotEmail.getText().toString().trim();

        if (userEmail.isEmpty()) {
            errorCustomToast("Enter your email",ForgotPasswordActivity.this);
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("email", userEmail)
                .build();

        Request request = new Request.Builder()
                .url("https://735b-188-166-219-165.ngrok-free.app/FloralFete/ForgotPassword")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->errorCustomToast("Failed to Send Code",ForgotPasswordActivity.this));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    receivedCode = response.body().string().replace("{\"code\": \"", "").replace("\"}", "");
                    runOnUiThread(() -> successCustomToast("Code Sent!",ForgotPasswordActivity.this));
                } else {
                    runOnUiThread(() ->errorCustomToast("Failed to Send Code",ForgotPasswordActivity.this));
                }
            }
        });
    }


    private void verifyCodeAndResetPassword() {
        String enteredCode = forgotCode.getText().toString().trim();
        String newPassword = forgotNewPassword.getText().toString().trim();

        if (enteredCode.isEmpty() || newPassword.isEmpty()) {
            errorCustomToast("Enter both Code and New Password",ForgotPasswordActivity.this);
            return;
        }

        if (!enteredCode.equals(receivedCode)) {
            errorCustomToast("Invalid Code",ForgotPasswordActivity.this);
            return;
        }


        updatePasswordInFirestore(userEmail,newPassword);
    }


    private void updatePasswordInFirestore(String email, String newPassword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);

                            userDoc.getReference().update("password", newPassword)
                                    .addOnSuccessListener(aVoid -> {
                                        successCustomToast("Password Updated!",ForgotPasswordActivity.this);
                                        startActivity(new Intent(ForgotPasswordActivity.this,UserSigninActivity.class));
                                    })
                                    .addOnFailureListener(e -> {
                                        errorCustomToast("Fail to update Password",ForgotPasswordActivity.this);
                                    });
                        } else {
                            errorCustomToast("User Not Found!",ForgotPasswordActivity.this);
                        }
                    }
                });
    }
}
