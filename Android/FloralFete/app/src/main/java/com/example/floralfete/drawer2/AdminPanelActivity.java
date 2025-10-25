package com.example.floralfete.drawer2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;


import com.example.floralfete.R;
import com.example.floralfete.drawer2.fragment.DashboardFragment;
import com.example.floralfete.drawer2.fragment.ManageOrdersFragment;
import com.example.floralfete.drawer2.fragment.ManageProductsFragment;
import com.example.floralfete.drawer2.fragment.ManageUsersFragment;
import com.google.android.material.navigation.NavigationView;

public class AdminPanelActivity extends AppCompatActivity {


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_panel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.theme_color));

        }


        drawerLayout = findViewById(R.id.drawerLayout2);
        navigationView = findViewById(R.id.navigationView2);
        Toolbar admin_panel_toolbar = findViewById(R.id.admin_panel_toolbar);

        TextView toolbar_title = findViewById(R.id.toolbar_title2);





        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.admin_panel_toolbar),
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();



        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            navigationView.setCheckedItem(R.id.nav_dashboard);
            toolbar_title.setText("Dashboard");

        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_dashboard) {
                    selectedFragment = new DashboardFragment();
                } else if (item.getItemId() == R.id.nav_manage_users) {
                    selectedFragment = new ManageUsersFragment();
                } else if (item.getItemId() == R.id.nav_manage_products) {
                    selectedFragment = new ManageProductsFragment();
                } else if (item.getItemId() == R.id.nav_manage_orders) {
                    selectedFragment = new ManageOrdersFragment();
                }


                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.admin_fragment_container, selectedFragment)
                            .commit();
                }


                toolbar_title.setText(item.getTitle());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            }
        });

    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }


}