package com.sae402.poissonglobe;

import android.os.Bundle;
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
    }
}