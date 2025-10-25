package com.example.floralfete.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.floralfete.AboutUsActivity;
import com.example.floralfete.R;
import com.example.floralfete.UserSigninActivity;
import com.example.floralfete.drawer.UserProfileActivity;

public class ProfileFragment extends Fragment {

    private TextView tvWelcomeMessage;
    private Button btnLoginOrProfile, btnAbout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvWelcomeMessage = view.findViewById(R.id.profile_textview1);
        btnLoginOrProfile = view.findViewById(R.id.profile_button1);
        btnAbout = view.findViewById(R.id.profile_button2);

        // Load user details from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_fname", null);

        if (userName != null) {
            // User is logged in
            tvWelcomeMessage.setText("Welcome, " + userName);
            btnLoginOrProfile.setText("Go to User Profile");
        } else {
            // No user logged in
            tvWelcomeMessage.setText("Sign in for the best experience");
            btnLoginOrProfile.setText("Login");
        }

        //Login or Go to Profile
        btnLoginOrProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userName != null) {
                    // Open User Profile
                    Intent intent1 = new Intent(getActivity(), UserProfileActivity.class);
                    startActivity(intent1);
                } else {
                    // Open Login Page
                    Intent intent = new Intent(getActivity(), UserSigninActivity.class);
                    startActivity(intent);
                }
            }
        });

        // About Floral Fete
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutIntent = new Intent(getContext(), AboutUsActivity.class);
                startActivity(aboutIntent);
            }
        });

        return view;
    }
}
