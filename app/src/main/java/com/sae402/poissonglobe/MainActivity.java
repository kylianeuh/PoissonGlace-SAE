package com.sae402.poissonglobe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
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

        // --- CONFIGURATION DES ÉCOUTEURS DE BOUTONS ---

        View btnRegles = findViewById(R.id.btnRegles);
        if (btnRegles != null) {
            btnRegles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, ReglesActivity.class);
                    startActivity(intent);
                }
            });
        }

        View btnStats = findViewById(R.id.btnStats);
        if (btnStats != null) {
            btnStats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                    startActivity(intent);
                }
            });
        }

        View btn2Joueurs = findViewById(R.id.btnJoueurs2);
        if (btn2Joueurs != null) {
            btn2Joueurs.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GestionJoueurActivity.class);
                intent.putExtra("NB_JOUEURS", 2);
                startActivity(intent);
            });
        }

        View btn4Joueurs = findViewById(R.id.btnJoueurs4);
        if (btn4Joueurs != null) {
            btn4Joueurs.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GestionJoueurActivity.class);
                intent.putExtra("NB_JOUEURS", 4);
                startActivity(intent);
            });
        }

        // --- GESTION DES BARRES SYSTÈME (EDGE TO EDGE) ---
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // --- GESTION DU FRAGMENT DE CLASSEMENT (VISUEL) ---
        ClassementFragment fragmentClassement = new ClassementFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.zoneClassement, fragmentClassement)
                    .commit();
        }

        // --- CHARGEMENT ASYNCHRONE DE LA BDD ET DES SCORES ---
        // Isole l'accès SQL pour éviter de figer l'interface et de déclencher l'erreur SurfaceSyncGroup
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getAppDatabase(MainActivity.this);
                List<JoueurBD> listeJoueurs = db.getJeuDAO().getAllJoueurs();

                List<Joueur> joueursPourClassement = new ArrayList<>();

                if (listeJoueurs == null || listeJoueurs.isEmpty()) {
                    Log.d("MA_BASE", "La base est vide...");
                } else {
                    for (JoueurBD jBD : listeJoueurs) {
                        joueursPourClassement.add(new Joueur(jBD.nom, jBD.scoreGlobal));
                        Log.d("MA_BASE", "Joueur trouvé : " + jBD.nom + " (ID: " + jBD.id + ")");
                    }
                }

                // Une fois les données récupérées, on met à jour le fragment sur le thread principal
                runOnUiThread(() -> {
                    fragmentClassement.majListeJoueurs(joueursPourClassement);
                });

            } catch (Exception e) {
                Log.e("MA_BASE", "Erreur lors du chargement de la base de données", e);
            }
        }).start();

        // --- AMBIANCE SONORE GLOBALE ---
        Intent intentMusique = new Intent(this, MusiqueService.class);
        startService(intentMusique);
    }
}