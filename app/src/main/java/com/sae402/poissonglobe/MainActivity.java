package com.sae402.poissonglobe;

import android.os.Bundle;
<<<<<<< HEAD
import android.view.View;
=======
import android.util.Log;import java.util.List;
>>>>>>> 884db78547c3176a2c9bec80b1e3c3d852fee1a1

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

<<<<<<< HEAD
        View btnRegles = findViewById(R.id.btnRegles);
        btnRegles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Un Intent pour aller de MainActivity vers ReglesActivity
                android.content.Intent intent = new android.content.Intent(MainActivity.this, ReglesActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
=======
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);return insets;
>>>>>>> 884db78547c3176a2c9bec80b1e3c3d852fee1a1
        });

        // --- TEST DE LA BASE DE DONNÉES ---// 1. Récupérer l'instance de la base
        AppDatabase db = AppDatabase.getAppDatabase(this);

        // 2. Récupérer la liste des joueurs
        List<JoueurBD> listeJoueurs = db.getJeuDAO().getAllJoueurs();

        // 3. Afficherles joueurs dans le Logcat pour vérifier
        if (listeJoueurs.isEmpty()) {
            Log.d("MA_BASE", "La base est vide...");
        } else {
            for (JoueurBD j : listeJoueurs) {
                Log.d("MA_BASE", "Joueur trouvé : " + j.nom +" (ID: " + j.id + ")");
            }
        }
    }
}