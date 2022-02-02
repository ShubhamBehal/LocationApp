package com.example.locapp2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final int REQUEST_CODE = 1;
    public static final int UPDATE_INTERVAL = 5000;
    public static final int FASTEST_INTERVAL = 3000;
    public static String lat;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private List<String> permissionToRequest;
    private final List<String> permission = new ArrayList<>();
    private final List<String> permissionsRejected = new ArrayList<>();

    TextView latTV,lonTV,altTV,accTV,addTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        permission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionToRequest = permissionToRequest(permission);
        if (permissionToRequest.size() > 0) {
            requestPermissions(permissionToRequest.toArray(new String[0]), REQUEST_CODE);
        }
        latTV = findViewById(R.id.latTV);
        lonTV = findViewById(R.id.lonTV);
        altTV = findViewById(R.id.altTV);
        accTV = findViewById(R.id.accTV);
        addTV = findViewById(R.id.addTV);

    }



    @Override
    protected void onPostResume() {
        super.onPostResume();
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this,
                    REQUEST_CODE, REQUEST_CODE, dialog -> Toast.makeText(this, "The service is not available",
                            Toast.LENGTH_SHORT).show());
            if (errorDialog != null) {
                errorDialog.show();
            }
        } else {
            findLocation();
        }
    }

    private void findLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d(TAG, "onSuccess" + location.getLatitude() + " " + location.getLongitude());
            }
        });
        startUpdatingLocation();


    }
    private void startUpdatingLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationCallback locationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                Log.d(TAG, "onLocationResult: " + location.getLatitude() + " " + location.getLongitude());

                lat = Double.toString(location.getLatitude());
                latTV.setText(lat);
                lonTV.setText(Double.toString(location.getLongitude()));
                altTV.setText(Double.toString(location.getAltitude()));
                accTV.setText(Double.toString(location.getAccuracy()));
                String address = "";
                Geocoder gg = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = gg.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    Address returnAddress = addresses.get(0);
                    StringBuilder stringBuilderReturnAddress = new StringBuilder();

                    for (int i = 0; i <= returnAddress.getMaxAddressLineIndex(); i++) {
                        stringBuilderReturnAddress.append(returnAddress.getAddressLine(i)).append("\n");
                    }

                    address = stringBuilderReturnAddress.toString();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "invalid address", Toast.LENGTH_SHORT).show();
                }
                addTV.setText(address);

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private List<String> permissionToRequest(List<String> permission)
    {
        ArrayList<String> results = new ArrayList<>();
        for (String p : permission) {
            if(isGranted(p)) {
                results.add(p);
            }
        }
        return results;
    }

    private boolean isGranted(String permission) {
        return checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(REQUEST_CODE == requestCode) {
            for(String permission : permissions){
                if(isGranted(permission)){
                    permissionsRejected.add(permission);
                }
            }
        }

        if(permissionsRejected.size() > 0 ) {

            if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))){
                new AlertDialog.Builder(this).setMessage("Accessing the location is Mandatory").setPositiveButton("OK", (dialog, which) -> requestPermissions(permissionsRejected.toArray(new String[permissionToRequest.size()]),REQUEST_CODE)).setNegativeButton("Cancel", null).create().show();
            }
        }
    }
}


