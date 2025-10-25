package com.example.floralfete.drawer;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.example.floralfete.MainActivity;
import com.example.floralfete.R;
import com.example.floralfete.UserSigninActivity;
import com.example.floralfete.drawer.fragment.AddressFragment;
import com.example.floralfete.drawer.fragment.OrderHistoryFragment;
import com.example.floralfete.drawer.fragment.OrdersFragment;
import com.example.floralfete.drawer.fragment.UpdateProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.theme_color));

        }

        // Initialize Drawer & NavigationView
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        Toolbar user_profile_toolbar = findViewById(R.id.user_profile_toolbar);

        TextView toolbar_title = findViewById(R.id.toolbar_title);

//        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
//        String userFName = sharedPreferences.getString("user_fname", "Guest");
//        String userLName = sharedPreferences.getString("user_lname", "");
//        String userEmail = sharedPreferences.getString("user_email", "No Email");
//        String userProfileImage = sharedPreferences.getString("user_profile_url", "No Image");
//
//        NavigationView navigationView = findViewById(R.id.navigationView);
//        View headerView = navigationView.getHeaderView(0);
//
//
//        TextView userProfileText1 = headerView.findViewById(R.id.user_profile_text1);
//        TextView userProfileText2 = headerView.findViewById(R.id.user_profile_text2);
//        ImageView userProfileImageView1 = headerView.findViewById(R.id.user_profile_image1);
//
//        if (!userProfileImage.equals("No Image")) {
//            Glide.with(UserProfileActivity.this).load(userProfileImage).into(userProfileImageView1);
//        }else{
//            userProfileImageView1.setImageResource(R.drawable.ic_user_profile);
//        }
//
//        userProfileText1.setText(userFName + " " + userLName);
//        userProfileText2.setText(userEmail);



        // Setup Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.user_profile_toolbar),
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        // Load Default Fragment
        if (savedInstanceState == null) {
            loadFragment(new UpdateProfileFragment());
            navigationView.setCheckedItem(R.id.nav_update_profile);
            toolbar_title.setText("Update Profile");

        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_update_profile) {
                    selectedFragment = new UpdateProfileFragment();
                } else if (item.getItemId() == R.id.nav_address) {
                    selectedFragment = new AddressFragment();
                } else if (item.getItemId() == R.id.nav_orders) {
                    selectedFragment = new OrdersFragment();
                } else if (item.getItemId() == R.id.nav_order_history) {
                    selectedFragment = new OrderHistoryFragment();
                } else if (item.getItemId() == R.id.nav_logout) {
                    logoutUser();
                    return true;
                }


                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.profile_fragment_container, selectedFragment)
                            .commit();
                }

                // Close the drawer
                toolbar_title.setText(item.getTitle());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            }
        });

    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.profile_fragment_container, fragment)
                .commit();
    }


    private void logoutUser() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_logout_dialog, null);
        builder.setView(dialogView);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Make background transparent
        alertDialog.show();

        // Cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        // Confirm logout button
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear SharedPreferences (User Data)
                SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String userId = preferences.getString("user_id",null);
                FirebaseFirestore.getInstance().collection("user").document(userId)
                        .update("fcmToken", FieldValue.delete());

                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

}