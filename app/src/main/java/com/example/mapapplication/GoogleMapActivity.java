package com.example.mapapplication;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class GoogleMapActivity extends AppCompatActivity {

    private MapView mapView;
    private RequestQueue requestQueue;
    private TextView tvMarkerCount;

    // ⚠️ REMPLACER PAR L'IP DE VOTRE PC (même IP que dans MainActivity)
    private final String showUrl = "http://192.168.0.162/map_project/getPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        // Configuration OSMDroid (obligatoire)
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));

        // Initialisation des vues
        mapView = findViewById(R.id.map);
        tvMarkerCount = findViewById(R.id.tvMarkerCount);

        // Configuration de la carte
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.0);

        // Position par défaut (Marrakech)
        mapView.getController().setCenter(new GeoPoint(31.5442, -8.7709));

        // Initialisation de Volley
        requestQueue = Volley.newRequestQueue(this);

        // Chargement des positions
        chargerPositions();
    }

    private void chargerPositions() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                showUrl,
                null,
                response -> {
                    try {
                        boolean succes = response.getBoolean("success");
                        if (succes) {
                            JSONArray positions = response.getJSONArray("positions");
                            int count = positions.length();

                            // Mise à jour du compteur
                            tvMarkerCount.setText("📍 " + count + " position" + (count > 1 ? "s" : ""));

                            // Nettoyer les anciens marqueurs
                            mapView.getOverlays().clear();

                            // Ajouter chaque marqueur
                            for (int i = 0; i < count; i++) {
                                JSONObject pos = positions.getJSONObject(i);
                                double lat = pos.getDouble("latitude");
                                double lon = pos.getDouble("longitude");
                                String date = pos.optString("date", "Date inconnue");

                                ajouterMarqueur(lat, lon, "Position #" + (i + 1), date);
                            }

                            // Rafraîchir la carte
                            mapView.invalidate();

                            Toast.makeText(this, count + " position(s) chargée(s)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erreur: " + response.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur de lecture des données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(request);
    }

    private void ajouterMarqueur(double latitude, double longitude, String titre, String snippet) {
        Marker marqueur = new Marker(mapView);
        marqueur.setPosition(new GeoPoint(latitude, longitude));
        marqueur.setTitle(titre);
        marqueur.setSubDescription(snippet);

        // Icône personnalisée
        Drawable drawable = getResources().getDrawable(R.drawable.marker, null);
        if (drawable != null) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false);
            marqueur.setIcon(new BitmapDrawable(getResources(), scaledBitmap));
        }

        marqueur.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marqueur);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // Recharger les positions pour avoir les dernières données
        chargerPositions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}