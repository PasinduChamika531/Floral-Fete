package com.example.floralfete;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.floralfete.drawer2.AdminPanelActivity;
import com.example.floralfete.validation.Validation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.floralfete.validation.Validation;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class UserSigninActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_signin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user_signin_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button signin_button2 = findViewById(R.id.signin_button2);
        signin_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSigninActivity.this, UserSigupActivity.class);
                startActivity(intent);
            }
        });

        Button signin_button1 = findViewById(R.id.signin_button1);
        signin_button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText signInEditText1 = findViewById(R.id.signin_edittext1);
                EditText signInEditPassword1 = findViewById(R.id.signin_editpassword1);

                String useremail = signInEditText1.getText().toString().trim();
                String userpassword = signInEditPassword1.getText().toString().trim();

                if (useremail.isEmpty()) {
                    signInEditText1.setError("Enter your email address");

                } else if (!Validation.isValidEmail(useremail)) {
                    signInEditText1.setError("Enter a valid email address");
                } else if (userpassword.isEmpty()) {
                    signInEditPassword1.setError("Enter a Password");
                } else {

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    firestore.collection("user")
                            .whereEqualTo("email", useremail)
                            .whereEqualTo("password", userpassword)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {


                                    Toast t = new Toast(UserSigninActivity.this);
                                    t.setDuration(Toast.LENGTH_LONG);
                                    t.setGravity(Gravity.BOTTOM,0,200);

                                    LayoutInflater inflater = LayoutInflater.from(UserSigninActivity.this);



                                    if (task.isSuccessful() && task.getResult() != null) {
                                        List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                                        if (documentSnapshotList.isEmpty()) {

                                            View view1 = inflater.inflate(R.layout.custom_toast2,null,false);
                                            TextView textView = view1.findViewById(R.id.customToastTextView2);
                                            textView.setText("Invalid credentials");
                                            t.setView(view1);
                                            t.show();
                                        } else {
                                            for (DocumentSnapshot document : documentSnapshotList) {

                                                if (document.get("status").equals("inactive")) {
                                                    View view1 = inflater.inflate(R.layout.custom_toast2,null,false);
                                                    TextView textView = view1.findViewById(R.id.customToastTextView2);
                                                    textView.setText("Your Account is Inactive!");
                                                    t.setView(view1);
                                                    t.show();

                                                }else{

                                                    View view1 = inflater.inflate(R.layout.custom_toast1,null,false);
                                                    TextView textView = view1.findViewById(R.id.customToastTextView1);
                                                    textView.setText("Login Success!");
                                                    t.setView(view1);
                                                    t.show();

                                                    //Save User to Shared Preference

                                                    String userId = document.getId();  // Firestore User ID
                                                    String userFname = document.getString("fname");
                                                    String userLname = document.getString("lname");
                                                    String userEmail = document.getString("email");
                                                    String userMobile = document.getString("mobile");
                                                    String userProfileImageUrl = document.getString("profileImageUrl");


                                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putString("user_id", userId);
                                                    editor.putString("user_fname", userFname);
                                                    editor.putString("user_lname", userLname);
                                                    editor.putString("user_email", userEmail);
                                                    editor.putString("user_mobile", userMobile);
                                                    editor.putString("user_profile_url", userProfileImageUrl);
                                                    editor.apply();

                                                    //message token
                                                    FirebaseMessaging.getInstance().getToken()
                                                            .addOnCompleteListener(task1 -> {
                                                                if (!task1.isSuccessful()) {
                                                                    return;
                                                                }
                                                                String token = task1.getResult();

                                                                saveTokenInFirestore(token,userId);
                                                            });


                                                    Intent intent1 = new Intent(UserSigninActivity.this, MainActivity.class);
                                                    startActivity(intent1);

                                                }



                                            }
                                        }
                                    } else {
                                        Log.e("FloralLog", "Error fetching user data", task.getException());

                                        View view1 = inflater.inflate(R.layout.custom_toast2,null,false);
                                        TextView textView = view1.findViewById(R.id.customToastTextView2);
                                        textView.setText("Error Occured");
                                        t.setView(view1);
                                        t.show();
                                    }

                                }
                            });



                }



            }
        });

        Button goToAdminPanel = findViewById(R.id.signin_button3);
        goToAdminPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSigninActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void saveTokenInFirestore(String token,String loggedUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = loggedUser;
        db.collection("user").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> {
                    // Token saved successfully
                    Log.i("FloralFeteLog","Token Saved");
                })
                .addOnFailureListener(e -> {

                    Log.i("FloralFeteLog","Token Failed");

                });
    }

}