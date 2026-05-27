package com.sae402.poissonglobe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class GestionJoueurActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_joueur);

        // Récupérer le choix (2 ou 4)
        int nbJoueurs = getIntent().getIntExtra("NB_JOUEURS", 2);Fragment fragmentChoisi;
        if (nbJoueurs == 4) {
            fragmentChoisi = new fourPlayers();
        } else {
            fragmentChoisi = new twoPlayers();
        }

        //Affichage
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragmentChoisi).commit();

        // Gestion du bouton retour
        findViewById(R.id.btnRetour).setOnClickListener(v -> finish());

        //Gestion du bouton commencer
        View btnStart = findViewById(R.id.btnCommencer);

        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GestionJoueurActivity.this, Jeu.class);
                    startActivity(intent);
                }
            });
        }
    }
}