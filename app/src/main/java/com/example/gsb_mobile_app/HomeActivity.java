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
import android.widget.ProgressBar;
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
    private ProgressBar progressBar;

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_home);

        // Initialiser le ProgressBar
        progressBar = findViewById(R.id.progressBar);

        // Afficher le ProgressBar pendant le chargement
        progressBar.setVisibility(View.VISIBLE);

        // Récupérer le token de l'utilisateur connecté
        SharedPreferences sharedPreferences = getSharedPreferences("appData", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        parentLayout = findViewById(R.id.parent_layout);

        Button btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Déconnexion : suppression du token et redirection vers la page de connexion
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
            progressBar.setVisibility(View.GONE); // Cacher le ProgressBar
        }

        Button btnCreateCR = findViewById(R.id.btn_create_cr);
        btnCreateCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, CreateCompteRenduActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getComptesRendus(String token) {
        if (token.isEmpty()) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // Cacher le ProgressBar
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
                        } finally {
                            progressBar.setVisibility(View.GONE); // Cacher le ProgressBar après la réponse
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                Log.e("API Error", error.toString());
                progressBar.setVisibility(View.GONE); // Cacher le ProgressBar en cas d'erreur
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
            String detailsCR = compteRendu.optString("details", "Détails non disponibles");

            // Récupérer l'ID du compte rendu
            final int compteRenduId = compteRendu.optInt("id", -1); // Make it final

            // Inflate le layout item_compte_rendu.xml
            View crView = inflater.inflate(R.layout.item_compte_rendu, parentLayout, false);

            TextView tvDate = crView.findViewById(R.id.tv_date);
            TextView tvPraticien = crView.findViewById(R.id.tv_praticien);

            // Remplir les TextViews avec les données
            tvDate.setText(dateVisite);
            tvPraticien.setText(praticienNom);

            // Ajouter un listener sur l'élément pour afficher plus de détails du compte rendu
            crView.setOnClickListener(new View.OnClickListener() { // Use anonymous class
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, DetailsCompteRenduActivity.class);
                    intent.putExtra("compteRenduId", compteRenduId); // Passage de l'ID du compte rendu à l'activité suivante
                    startActivity(intent);
                }
            });

            parentLayout.addView(crView); // Ajouter la vue au layout principal
        }
    }
}