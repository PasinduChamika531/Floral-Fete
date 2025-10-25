package com.example.floralfete;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.floralfete.validation.Validation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class UserSigupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_user_sigup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user_signup_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = new Intent(UserSigupActivity.this,UserSigninActivity.class);

        Button signup_button2 = findViewById(R.id.signup_button2);
        signup_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });


        Button signup_button1 = findViewById(R.id.signup_button1);
        signup_button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText signUpEditText1 = findViewById(R.id.signup_edittext1);
                EditText signUpEditText2 = findViewById(R.id.signup_edittext2);
                EditText signUpEditText3 = findViewById(R.id.signup_edittext3);
                EditText signUpEditText4 = findViewById(R.id.signup_edittext4);
                EditText signUpEditPassword1 = findViewById(R.id.signup_editpassword1);
                EditText signUpEditPassword2 = findViewById(R.id.signup_editpassword2);

                String fname = signUpEditText1.getText().toString().trim();
                String lname = signUpEditText2.getText().toString().trim();
                String usermobile = signUpEditText3.getText().toString().trim();
                String useremail = signUpEditText4.getText().toString().trim();
                String userpassword = signUpEditPassword1.getText().toString().trim();
                String confirmpassword = signUpEditPassword2.getText().toString().trim();
                String userstatus = "active";


                if (fname.isEmpty() || !fname.matches("^[A-Za-z][A-Za-z .'-]{1,49}$")) {
                    signUpEditText1.setError("Enter a valid first name (letters, spaces, ' . - allowed)");
                } else if (lname.isEmpty() || !lname.matches("^[A-Za-z][A-Za-z .'-]{1,49}$")) {
                    signUpEditText2.setError("Enter a valid last name (letters, spaces, ' . - allowed)");
                } else if (!usermobile.matches("^07[01245678]\\d{7}$")) {
                    signUpEditText3.setError("Enter a valid 10-digit Sri Lankan mobile number");
                } else if (!Validation.isValidEmail(useremail)) {
                    signUpEditText4.setError("Enter a valid email address");
                } else if (!Validation.isValidPassword(userpassword)) {
                    signUpEditPassword1.setError("Password must be at least 6 characters, include an uppercase letter, number, and special character (@#$%^&*!)");
                } else if (!userpassword.equals(confirmpassword)) {
                    signUpEditPassword2.setError("Passwords do not match");
                } else {

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    firestore.collection("user")
                            .whereEqualTo("email", useremail)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                        signUpEditText4.setError("Email is already registered. Try another.");
                                    } else {


                                        HashMap<String, Object> user = new HashMap<>();
                                        user.put("fname", fname);
                                        user.put("lname", lname);
                                        user.put("mobile", usermobile);
                                        user.put("email", useremail);
                                        user.put("password", userpassword);
                                        user.put("status", userstatus);
                                        user.put("profileImageUrl","");

                                        firestore.collection("user").add(user)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {


                                                        Toast t = new Toast(UserSigupActivity.this);

                                                        LayoutInflater inflater = LayoutInflater.from(UserSigupActivity.this);
                                                        View view1 = inflater.inflate(R.layout.custom_toast1,null,false);
                                                        TextView textView = view1.findViewById(R.id.customToastTextView1);
                                                        textView.setText("Registration Success!");
                                                        t.setView(view1);

                                                        t.setDuration(Toast.LENGTH_LONG);
                                                        t.setGravity(Gravity.BOTTOM,0,200);
                                                        t.show();

                                                        startActivity(intent);

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(UserSigupActivity.this, "Registratoin Failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                    }
                                }
                            });

                }



            }
        });


    }
}