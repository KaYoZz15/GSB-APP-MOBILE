package com.example.gsb_mobile_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditCompteRenduActivity extends AppCompatActivity {

    private EditText etTitre, etContenu;
    private Button btnSauvegarder, btnAnnuler;
    private int compteRenduId; // ID du compte rendu (si nécessaire pour modification en base de données)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_compte_rendu);

        // Liaison avec les éléments du layout
        etTitre = findViewById(R.id.etTitre);
        etContenu = findViewById(R.id.etContenu);
        btnSauvegarder = findViewById(R.id.btnSauvegarder);
        btnAnnuler = findViewById(R.id.btnAnnuler);

        // Récupérer les données envoyées par l'intent
        Intent intent = getIntent();
        if (intent.hasExtra("compteRenduId")) {
            compteRenduId = intent.getIntExtra("compteRenduId", -1);
            etTitre.setText(intent.getStringExtra("titre"));
            etContenu.setText(intent.getStringExtra("contenu"));
        }

        // Action du bouton Sauvegarder
        btnSauvegarder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nouveauTitre = etTitre.getText().toString().trim();
                String nouveauContenu = etContenu.getText().toString().trim();

                if (nouveauTitre.isEmpty() || nouveauContenu.isEmpty()) {
                    Toast.makeText(EditCompteRenduActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Retourner les données modifiées à l'activité précédente
                Intent resultIntent = new Intent();
                resultIntent.putExtra("compteRenduId", compteRenduId);
                resultIntent.putExtra("titre", nouveauTitre);
                resultIntent.putExtra("contenu", nouveauContenu);
                setResult(RESULT_OK, resultIntent);
                finish(); // Fermer l'activité
            }
        });

        // Action du bouton Annuler
        btnAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Fermer l'activité sans enregistrer
            }
        });
    }
}
