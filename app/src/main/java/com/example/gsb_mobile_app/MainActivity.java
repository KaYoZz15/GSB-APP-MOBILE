package com.example.gsb_mobile_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etPseudo, etPassword;
    private Button btnLogin;
    private static final String LOGIN_URL = "https://www.kevinechallier.fr/gsb/Connexion/connexion.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPseudo = findViewById(R.id.et_pseudo);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String pseudo = etPseudo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (pseudo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject json = new JSONObject();
        try {
            json.put("pseudo", pseudo);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show());
                Log.e("LOGIN_ERROR", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);

                        if (jsonResponse.getInt("status") == 200) {
                            String token = jsonResponse.getString("token");

                            // Sauvegarder le token dans SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("appData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("token", token);
                            editor.apply();  // Sauvegarder le token


                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                intent.putExtra("TOKEN", token);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            String errorMessage = jsonResponse.getString("message");
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("LOGIN_ERROR", "Erreur JSON: " + e.getMessage());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erreur d'authentification", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
