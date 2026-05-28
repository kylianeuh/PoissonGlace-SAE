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


        //POUR LES TESTS, A SUPPRIMER A LA FIN !!
        View logoTest = findViewById(R.id.txtTitre);
        if (logoTest != null) {
            logoTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Jeu.class);
                    startActivity(intent);
                }
            });
        }

        View btnRegles = findViewById(R.id.btnRegles);
        btnRegles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent = new android.content.Intent(MainActivity.this, ReglesActivity.class);
                startActivity(intent);
            }
        });

        View btnStats = findViewById(R.id.btnStats);
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

        View btn2Joueurs = findViewById(R.id.btnJoueurs2);
        View btn4Joueurs = findViewById(R.id.btnJoueurs4);

        btn2Joueurs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GestionJoueurActivity.class);
            intent.putExtra("NB_JOUEURS", 2);
            startActivity(intent);
        });


        btn4Joueurs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GestionJoueurActivity.class);
            intent.putExtra("NB_JOUEURS", 4);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
        });

        AppDatabase db = AppDatabase.getAppDatabase(this);
        List<JoueurBD> listeJoueurs = db.getJeuDAO().getAllJoueurs();

        if (listeJoueurs.isEmpty()) {
            Log.d("MA_BASE", "La base est vide...");
        } else {
            for (JoueurBD j : listeJoueurs) {
                Log.d("MA_BASE", "Joueur trouvé : " + j.nom +" (ID: " + j.id + ")");
            }
        }

        ClassementFragment fragmentClassement = new ClassementFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.zoneClassement, fragmentClassement)
                    .commit();
        }

        List<Joueur> joueursDeLaBase = new ArrayList<>();
        joueursDeLaBase.add(new Joueur("Kylian", 150));
        joueursDeLaBase.add(new Joueur("Lindsay", 90));
        joueursDeLaBase.add(new Joueur("Nemo", 210));

        fragmentClassement.majListeJoueurs(joueursDeLaBase);
    }
}