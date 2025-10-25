package com.example.floralfete;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.example.floralfete.fragments.CartFragment;
import com.example.floralfete.fragments.HomeFragment;
import com.example.floralfete.fragments.ProfileFragment;
import com.example.floralfete.fragments.WishlistFragment;

import com.example.floralfete.utils.NetworkChangeReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.white));

        }


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        loadFragment(new HomeFragment());


        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            String fragmentTag = "";

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
                fragmentTag = "HomeFragment";
            } else if (itemId == R.id.nav_cart) {
                fragment = new CartFragment();
                fragmentTag = "CartFragment";
            } else if (itemId == R.id.nav_wishlist) {
                fragment = new WishlistFragment();
                fragmentTag = "WishlistFragment";
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
                fragmentTag = "ProfileFragment";
            }

            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (currentFragment == null || !currentFragment.getClass().getName().equals(fragment.getClass().getName())) {
                // Perform the fragment transaction if it's not the same fragment
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.fragment_enter,
                                R.anim.fragment_exit,
                                R.anim.fragment_pop_enter,
                                R.anim.fragment_pop_exit)
                        .replace(R.id.fragment_container, fragment, fragmentTag)
                        .addToBackStack(null)
                        .commit();
            }


            return true;
        });





    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment) // Make sure fragment_container exists in activity_main.xml
                    .commit();
            return true;
        }
        return false;
    }



}
