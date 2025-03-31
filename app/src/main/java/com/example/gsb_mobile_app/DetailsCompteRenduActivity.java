package com.example.gsb_mobile_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DetailsCompteRenduActivity extends AppCompatActivity {

    private TextView tvDate, tvPraticien, tvMedecin, tvCommentaires, tvPieceJointe, tvEchantillons;
    private ProgressBar progressBar;
    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_compte_rendu);

        // Récupérer la Toolbar et l'ajouter comme ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Activer la flèche de retour
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialisation des TextViews
        tvDate = findViewById(R.id.tv_date);
        tvPraticien = findViewById(R.id.tv_praticien);
        tvCommentaires = findViewById(R.id.tv_commentaires);
        tvPieceJointe = findViewById(R.id.tv_piece_jointe);
        tvEchantillons = findViewById(R.id.tv_echantillons);
        progressBar = findViewById(R.id.progress_bar);

        // Initialisation du bouton de suppression
        btnDelete = findViewById(R.id.btn_delete);

        // Récupérer l'ID du compte rendu à partir de l'Intent
        int compteRenduId = getIntent().getIntExtra("compteRenduId", -1);

        if (compteRenduId != -1) {
            // Vérifier si un token est présent dans SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("appData", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("token", "");

            if (token.isEmpty()) {
                Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            // Afficher le ProgressBar pendant le chargement
            progressBar.setVisibility(View.VISIBLE);

            // Appel API pour récupérer les détails du compte rendu
            fetchCompteRenduDetails(compteRenduId, token);

            // Ajouter un écouteur au bouton de suppression
            btnDelete.setOnClickListener(v -> deleteCompteRendu(compteRenduId, token));
        } else {
            // Si l'ID n'est pas valide, afficher un message d'erreur
            Toast.makeText(this, "ID de compte rendu invalide", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gérer l'action de la flèche de retour
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();  // Appel à la fonction pour revenir en arrière
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchCompteRenduDetails(int compteRenduId, String token) {
        String url = "https://www.kevinechallier.fr/gsb/api/protected.php?comptes_rendus=true&id_cr=" + compteRenduId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Affiche la réponse JSON brute dans le Log pour le débogage
                            Log.d("API Response", response);

                            // Conversion de la réponse en JSONObject
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");

                            if (status == 200) {
                                // Récupérer le tableau 'comptes_rendus'
                                JSONArray comptesRendusArray = jsonObject.getJSONArray("comptes_rendus");

                                if (comptesRendusArray.length() > 0) {
                                    // Récupérer le premier élément du tableau (vous pouvez aussi boucler si vous avez plusieurs éléments)
                                    JSONObject cr = comptesRendusArray.getJSONObject(0);

                                    // Mise à jour des TextViews avec les données du premier compte rendu
                                    tvDate.setText("Date: " + cr.optString("date_visite", "Non spécifiée"));
                                    tvPraticien.setText("Praticien: " + cr.optString("praticien_nom", "Non spécifié"));
                                    tvCommentaires.setText("Commentaires: " + cr.optString("commentaires", "Non spécifiés"));
                                    tvEchantillons.setText("Échantillons distribués: " + cr.optString("echantillons_distribues", "Non spécifiés"));
                                    tvPieceJointe.setText("Pièce jointe: " + cr.optString("piece_jointe", "Non spécifiée"));

                                    // Afficher le bouton de suppression si le compte rendu est valide
                                    btnDelete.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Toast.makeText(DetailsCompteRenduActivity.this, "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(DetailsCompteRenduActivity.this, "Erreur de parsing", Toast.LENGTH_SHORT).show();
                        } finally {
                            // Cacher le ProgressBar
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DetailsCompteRenduActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void deleteCompteRendu(int compteRenduId, String token) {
        String url = "https://www.kevinechallier.fr/gsb/api/protected.php?comptes_rendus=true&delete=true&id_cr=" + compteRenduId;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Affiche la réponse JSON brute dans le Log pour le débogage
                            Log.d("API Response", response);

                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");

                            if (status == 200) {
                                Toast.makeText(DetailsCompteRenduActivity.this, "Compte rendu supprimé", Toast.LENGTH_SHORT).show();
                                finish(); // Retourner à l'écran précédent
                            } else {
                                Toast.makeText(DetailsCompteRenduActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(DetailsCompteRenduActivity.this, "Erreur de parsing", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DetailsCompteRenduActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
