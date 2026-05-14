package com.example.mapapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button btnMap;
    private TextView tvStatus;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    private double derniereLatitude = 0;
    private double derniereLongitude = 0;

    // ⚠️ REMPLACER PAR L'IP DE VOTRE PC
    private final String insertUrl = "http://192.168.0.162/map_project/createPosition.php";

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        btnMap = findViewById(R.id.btnMap);

        requestQueue = Volley.newRequestQueue(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Bouton pour ouvrir la carte
        btnMap.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GoogleMapActivity.class));
        });

        // Vérification des permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PERMISSION_REQUEST_CODE);
        } else {
            demarrerGPS();
        }
    }

    private void demarrerGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                30000,      // 30 secondes
                50,         // 50 mètres
                locationListener
        );

        tvStatus.setText("🟢 GPS activé - En attente de position...");
        Toast.makeText(this, "🟢 GPS activé", Toast.LENGTH_SHORT).show();
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            double alt = location.getAltitude();
            float acc = location.getAccuracy();

            derniereLatitude = lat;
            derniereLongitude = lon;

            String msg = String.format(Locale.FRANCE,
                    "📍 Position capturée\nLat: %.6f\nLon: %.6f\nAlt: %.1fm\nPrécision: %.1fm",
                    lat, lon, alt, acc);

            tvStatus.setText("✅ Dernière position: " + String.format(Locale.FRANCE, "%.6f, %.6f", lat, lon));
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();

            envoyerPosition(lat, lon);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            tvStatus.setText("✅ " + provider + " activé");
            Toast.makeText(MainActivity.this, "✅ " + provider + " activé", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            tvStatus.setText("❌ " + provider + " désactivé");
            Toast.makeText(MainActivity.this, "❌ " + provider + " désactivé", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    private void envoyerPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                insertUrl,
                response -> {
                    // Succès (silencieux pour ne pas spammer)
                },
                error -> {
                    Toast.makeText(MainActivity.this, "❌ Erreur envoi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
                Map<String, String> params = new HashMap<>();
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date", sdf.format(new Date()));

                // Identifiant unique (ANDROID_ID, pas besoin de permission)
                String androidId = Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );
                params.put("imei", androidId);

                return params;
            }
        };
        requestQueue.add(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                demarrerGPS();
            } else {
                tvStatus.setText("❌ Permission refusée");
                Toast.makeText(this, "❌ Permission refusée - L'application ne peut pas fonctionner", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}