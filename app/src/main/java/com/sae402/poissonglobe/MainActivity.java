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

        findViewById(R.id.btnRegles).setOnClickListener(v -> startActivity(new Intent(this, ReglesActivity.class)));
        findViewById(R.id.btnStats).setOnClickListener(v -> startActivity(new Intent(this, StatsActivity.class)));

        findViewById(R.id.btnJoueurs2).setOnClickListener(v -> {
            Intent intent = new Intent(this, GestionJoueurActivity.class);
            intent.putExtra("NB_JOUEURS", 2);
            startActivity(intent);
        });

        findViewById(R.id.btnJoueurs4).setOnClickListener(v -> {
            Intent intent = new Intent(this, GestionJoueurActivity.class);
            intent.putExtra("NB_JOUEURS", 4);
            startActivity(intent);
        });

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        ClassementFragment fragmentClassement = new ClassementFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.zoneClassement, fragmentClassement)
                    .commit();
        }

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getAppDatabase(MainActivity.this);
                List<JoueurBD> listeJoueurs = db.getJeuDAO().getAllJoueurs();

                int attente = 0;
                while ((listeJoueurs == null || listeJoueurs.isEmpty()) && attente < 10) {
                    Thread.sleep(150);
                    listeJoueurs = db.getJeuDAO().getAllJoueurs();
                    attente++;
                }

                if (listeJoueurs != null && !listeJoueurs.isEmpty()) {
                    List<Joueur> joueursPourClassement = new ArrayList<>();
                    for (JoueurBD jBD : listeJoueurs) {
                        joueursPourClassement.add(new Joueur(jBD.nom, jBD.scoreGlobal));
                    }
                    runOnUiThread(() -> {
                        if (fragmentClassement != null) {
                            fragmentClassement.majListeJoueurs(joueursPourClassement);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("MA_BASE", "Erreur BDD", e);
            }
        }).start();

        startService(new Intent(this, MusiqueService.class));
    }
}