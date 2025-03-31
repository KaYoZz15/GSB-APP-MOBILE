package com.example.gsb_mobile_app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateCompteRenduActivity extends AppCompatActivity {

    private EditText etDateVisite, etCommentaires;
    private Spinner spinnerPraticien;
    private ListView listViewEchantillons;
    private Button btnSaveCR;
    private ProgressBar progressBar;
    private String token;

    private List<Map<String, String>> praticienList = new ArrayList<>();
    private List<Map<String, String>> echantillonList = new ArrayList<>();
    private ArrayAdapter<String> echantillonAdapter;
    private Calendar selectedDate = Calendar.getInstance(); // Pour stocker la date sélectionnée

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cr);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Afficher la flèche de retour
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Créer un Compte Rendu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Affiche la flèche de retour
        }

        initializeViews();
        loadToken();
        setupEchantillonListView();
        loadDataFromAPI();
        setSaveButtonClickListener();
        setDateEditTextClickListener(); // Ajout du listener pour la sélection de la date
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); // Retour à l'écran précédent
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeViews() {
        etDateVisite = findViewById(R.id.et_date_visite);
        etCommentaires = findViewById(R.id.et_commentaires);
        spinnerPraticien = findViewById(R.id.spinner_praticien);
        listViewEchantillons = findViewById(R.id.list_echantillons);
        btnSaveCR = findViewById(R.id.btn_save_cr);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("appData", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");
    }

    private void setupEchantillonListView() {
        echantillonAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,
                new ArrayList<>()); // Initialement vide
        listViewEchantillons.setAdapter(echantillonAdapter);
    }

    private void loadDataFromAPI() {
        progressBar.setVisibility(View.VISIBLE);
        loadPraticiens();
        loadEchantillons();
    }

    private void loadPraticiens() {
        String url = "https://www.kevinechallier.fr/gsb/api/protected.php?data=praticiens";
        fetchDataFromAPI(url, this::handlePraticiensResponse, this::handlePraticiensError);
    }

    private void loadEchantillons() {
        String url = "https://www.kevinechallier.fr/gsb/api/protected.php?data=echantillons";
        fetchDataFromAPI(url, this::handleEchantillonsResponse, this::handleEchantillonsError);
    }

    private void fetchDataFromAPI(String url, ResponseCallback responseCallback, ErrorCallback errorCallback) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    responseCallback.onResponse(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    errorCallback.onError(error);
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

    private void handlePraticiensResponse(JSONObject response) {
        try {
            JSONArray praticiens = response.getJSONArray("data");
            for (int i = 0; i < praticiens.length(); i++) {
                JSONObject praticien = praticiens.getJSONObject(i);
                Map<String, String> praticienMap = new HashMap<>();
                praticienMap.put("id", praticien.getString("id"));
                praticienMap.put("nom", praticien.getString("nom"));
                praticienList.add(praticienMap);
            }
            populatePraticienSpinner();
        } catch (JSONException e) {
            handleJsonException(e, "Erreur lors de la récupération des praticiens");
        }
    }

    private void handleEchantillonsResponse(JSONObject response) {
        try {
            JSONArray echantillons = response.getJSONArray("data");
            echantillonList.clear();
            List<String> echantillonNoms = new ArrayList<>();
            for (int i = 0; i < echantillons.length(); i++) {
                JSONObject echantillon = echantillons.getJSONObject(i);
                Map<String, String> echantillonMap = new HashMap<>();
                echantillonMap.put("id", echantillon.getString("id"));
                echantillonMap.put("nom", echantillon.getString("nom"));
                echantillonList.add(echantillonMap);
                echantillonNoms.add(echantillon.getString("nom"));
            }
            updateEchantillonListView(echantillonNoms);
        } catch (JSONException e) {
            handleJsonException(e, "Erreur lors de la récupération des échantillons");
        }
    }

    private void handlePraticiensError(VolleyError error) {
        handleVolleyError(error, "Erreur lors de la récupération des praticiens");
    }

    private void handleEchantillonsError(VolleyError error) {
        handleVolleyError(error, "Erreur lors de la récupération des échantillons");
    }

    private void populatePraticienSpinner() {
        ArrayAdapter<String> praticienAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                praticienList.stream().map(map -> map.get("nom")).toArray(String[]::new));
        spinnerPraticien.setAdapter(praticienAdapter);
        spinnerPraticien.setTag(praticienList); // Stocker la liste pour une utilisation ultérieure
    }

    private void updateEchantillonListView(List<String> echantillonNoms) {
        echantillonAdapter.clear();
        echantillonAdapter.addAll(echantillonNoms);
        echantillonAdapter.notifyDataSetChanged();
    }

    private void setSaveButtonClickListener() {
        btnSaveCR.setOnClickListener(v -> saveCompteRendu());
    }

    private void setDateEditTextClickListener() {
        etDateVisite.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    updateDateEditText();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void updateDateEditText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDateVisite.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void saveCompteRendu() {
        if (validateInput()) {
            progressBar.setVisibility(View.VISIBLE);
            sendCompteRenduToAPI();
        }
    }

    private boolean validateInput() {
        String dateVisite = etDateVisite.getText().toString().trim();
        String praticienId = getSelectedPraticienId();
        List<String> selectedEchantillonsIds = getSelectedEchantillonIds();

        if (dateVisite.isEmpty() || praticienId == null || selectedEchantillonsIds.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs et sélectionner au moins un échantillon",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String getSelectedPraticienId() {
        List<Map<String, String>> praticienList = (List<Map<String, String>>) spinnerPraticien.getTag();
        if (praticienList != null && !praticienList.isEmpty() && spinnerPraticien.getSelectedItemPosition() >= 0
                && spinnerPraticien.getSelectedItemPosition() < praticienList.size()) {
            return praticienList.get(spinnerPraticien.getSelectedItemPosition()).get("id");
        }
        return null; // Ou lancez une exception si c'est une erreur critique
    }

    private List<String> getSelectedEchantillonIds() {
        List<String> selectedIds = new ArrayList<>();
        for (int i = 0; i < listViewEchantillons.getCount(); i++) {
            if (listViewEchantillons.isItemChecked(i)) {
                selectedIds.add(echantillonList.get(i).get("id"));
            }
        }
        return selectedIds;
    }

    private void sendCompteRenduToAPI() {
        String url = "https://www.kevinechallier.fr/gsb/api/protected.php?comptes_rendus=true";
        JSONObject params = createCompteRenduJson();

        fetchDataFromAPI(url, params, this::handleSaveResponse, this::handleSaveError);
    }

    private JSONObject createCompteRenduJson() {
        JSONObject params = new JSONObject();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            params.put("date_visite", dateFormat.format(selectedDate.getTime()));
            params.put("praticien_id", getSelectedPraticienId());
            params.put("echantillons", TextUtils.join(",", getSelectedEchantillonIds()));
            params.put("commentaires", etCommentaires.getText().toString().trim());
        } catch (JSONException e) {
            handleJsonException(e, "Erreur lors de la création des données JSON");
        }
        return params;
    }

    private void fetchDataFromAPI(String url, JSONObject params, ResponseCallback responseCallback,
                                  ErrorCallback errorCallback) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    responseCallback.onResponse(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    errorCallback.onError(error);
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

    private void handleSaveResponse(JSONObject response) {
        try {
            int status = response.getInt("status");
            if (status == 200) {
                Toast.makeText(this, "Compte rendu créé avec succès", Toast.LENGTH_SHORT).show();
                finish(); // Retour à l'écran précédent après la sauvegarde réussie
            } else {
                Toast.makeText(this, "Erreur lors de la création du compte rendu", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            handleJsonException(e, "Erreur lors du traitement de la réponse de création du compte rendu");
        }
    }

    private void handleSaveError(VolleyError error) {
        handleVolleyError(error, "Erreur lors de l'enregistrement du compte rendu");
    }

    private void handleJsonException(Exception e, String errorMessage) {
        Log.e("CreateCompteRendu", errorMessage, e);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void handleVolleyError(VolleyError error, String errorMessage) {
        Log.e("CreateCompteRendu", errorMessage, error);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private interface ResponseCallback {
        void onResponse(JSONObject response);
    }

    private interface ErrorCallback {
        void onError(VolleyError error);
    }
}
