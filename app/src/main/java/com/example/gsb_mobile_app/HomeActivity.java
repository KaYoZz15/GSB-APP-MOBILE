package com.example.gsb_mobile_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String API_URL = "https://www.kevinechallier.fr/gsb/api/protected.php?comptes_rendus=true";
    private LinearLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Récupérer le token de l'utilisateur connecté
        SharedPreferences sharedPreferences = getSharedPreferences("appData", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        parentLayout = findViewById(R.id.parent_layout);

        Button btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("token"); // Supprimer le token
                editor.apply();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Charger les comptes rendus depuis l'API
        if (!token.isEmpty()) {
            getComptesRendus(token);
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }
    }

    private void getComptesRendus(String token) {
        if (token.isEmpty()) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class); // Redirection vers l'écran de connexion
            startActivity(intent);
            finish();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int status = response.getInt("status");

                            if (status == 200) {
                                JSONArray comptesRendus = response.getJSONArray("comptes_rendus");

                                if (comptesRendus.length() == 0) {
                                    Toast.makeText(HomeActivity.this, "Aucun compte rendu trouvé", Toast.LENGTH_SHORT).show();
                                } else {
                                    afficherComptesRendus(comptesRendus);
                                }
                            } else {
                                Toast.makeText(HomeActivity.this, "Erreur de récupération des données", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(HomeActivity.this, "Erreur de parsing des données", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                Log.e("API Error", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    private void afficherComptesRendus(JSONArray comptesRendus) throws JSONException {
        Log.d("DISPLAY_CR", "Affichage des comptes rendus : " + comptesRendus.length());
        Toast.makeText(this, "Affichage des comptes rendus", Toast.LENGTH_SHORT).show();

        LayoutInflater inflater = LayoutInflater.from(this);
        parentLayout.removeAllViews(); // Nettoie l'affichage avant d'ajouter les nouveaux éléments

        for (int i = 0; i < comptesRendus.length(); i++) {
            JSONObject compteRendu = comptesRendus.getJSONObject(i);

            // Vérifier si les champs existent
            String dateVisite = compteRendu.optString("date_visite", "Date inconnue");
            String praticienNom = compteRendu.optString("praticien_nom", "Praticien inconnu");
            String detailsCR = compteRendu.optString("details", "Détails non disponibles");  // Ajouter le champ des détails du CR

            // Récupérer l'ID du compte rendu
            int compteRenduId = compteRendu.optInt("id", -1); // Si l'ID est dans la réponse JSON, sinon passez -1

            // Inflate le layout item_compte_rendu.xml
            View crView = inflater.inflate(R.layout.item_compte_rendu, parentLayout, false);

            TextView tvDate = crView.findViewById(R.id.tv_date);
            TextView tvPraticien = crView.findViewById(R.id.tv_praticien);

            // Remplir les TextViews
            tvDate.setText(dateVisite);
            tvPraticien.setText(praticienNom);

            // Ajouter un listener sur le TextView pour les détails du compte rendu
            crView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, DetailsCompteRenduActivity.class);
                intent.putExtra("compteRenduId", compteRenduId);
                startActivity(intent);
            });

            parentLayout.addView(crView);
        }
    }
}
