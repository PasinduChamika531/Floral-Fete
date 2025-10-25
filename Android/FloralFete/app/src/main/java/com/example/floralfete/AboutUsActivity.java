package com.example.floralfete;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.floralfete.utils.CustomMapView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class AboutUsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private CustomMapView customMapView;
    private LatLng shopLocation = new LatLng(7.932141640720306, 80.70824774213277);
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }

        scrollView = findViewById(R.id.about_us_scrollview);

        // Initialize CustomMapView
        customMapView = findViewById(R.id.custom_map);
        customMapView.onCreate(savedInstanceState);
        customMapView.getMapAsync(this);


        TextView phoneText = findViewById(R.id.tv_phone);
        phoneText.setText("Contact: 011 2233444");


        Button callButton = findViewById(R.id.btn_call);
        callButton.setOnClickListener(v -> makePhoneCall());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions()
                .position(shopLocation)
                .title("Floral Fete Shop")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flower_location1))
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 15));
    }

    private void makePhoneCall() {
        String phoneNumber = "tel:+1234567890";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
            startActivity(dialIntent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                errorCustomToast("Call Permission denied!", this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        customMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        customMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customMapView.onDestroy();
    }

}
