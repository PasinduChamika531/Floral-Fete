package com.example.floralfete;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;
import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.floralfete.drawer2.AdminPanelActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminSignInActivity extends AppCompatActivity {

    private EditText adminSignInText1, adminSignInPassword1;
    private Button adminSignInButton;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_admin_sign_in);

        adminSignInText1 = findViewById(R.id.admin_signin_text1);
        adminSignInPassword1 = findViewById(R.id.admin_signin_password1);
        adminSignInButton = findViewById(R.id.admin_signin_button1);

        adminSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminEmail = adminSignInText1.getText().toString().trim();
                String adminPassword = adminSignInPassword1.getText().toString().trim();

                if (adminEmail.isEmpty()) {
                    errorCustomToast("Please Enter Email",AdminSignInActivity.this);
                }else if (adminPassword.isEmpty()) {
                    errorCustomToast("Please Enter Password",AdminSignInActivity.this);
                }else{
                    sendLoginRequest(adminEmail, adminPassword);
                }

            }
        });
    }


    private void sendLoginRequest(final String email, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("email", email);
                jsonObject.addProperty("password", password);

                RequestBody requestBody = RequestBody.create( gson.toJson(jsonObject), MediaType.get("application/json"));
                Request request = new Request.Builder()
                        .url("https://735b-188-166-219-165.ngrok-free.app/FloralFete/AdminSignIn") // Fixed double slash
                        .post(requestBody)
                        .build();

                try {
                    Response response = okHttpClient.newCall(request).execute();

                    if (!response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                errorCustomToast("Server Error",AdminSignInActivity.this);
                            }
                        });
                        return;
                    }

                    String responseText = "";
                    if (response.body() != null) {
                        responseText = response.body().string();
                    }

                    JsonObject responseJsonObject = gson.fromJson(responseText, JsonObject.class);

                    if (responseJsonObject == null || !responseJsonObject.has("response")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                errorCustomToast("Invalid Server Response",AdminSignInActivity.this);
                            }
                        });
                        return;
                    }

                    String responseStatus = responseJsonObject.get("response").getAsString();

                    if (responseStatus.equals("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                successCustomToast("Login Successful",AdminSignInActivity.this);
                                Intent adminPanelIntent = new Intent(AdminSignInActivity.this, AdminPanelActivity.class);
                                startActivity(adminPanelIntent);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                errorCustomToast("Invalid Credintials",AdminSignInActivity.this);
                            }
                        });
                    }

                } catch (IOException e) {
                    Log.e("AdminSignIn", "Network error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorCustomToast("Network Error",AdminSignInActivity.this);
                        }
                    });
                }
            }
        }).start();
    }
}
