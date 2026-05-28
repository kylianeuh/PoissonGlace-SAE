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

        int nbJoueurs = getIntent().getIntExtra("NB_JOUEURS", 2);Fragment fragmentChoisi;
        if (nbJoueurs == 4) {
            fragmentChoisi = new fourPlayers();
        } else {
            fragmentChoisi = new twoPlayers();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragmentChoisi).commit();

        findViewById(R.id.btnRetourDetails).setOnClickListener(v -> finish());

        View btnStart = findViewById(R.id.btnCommencer);

        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GestionJoueurActivity.this, Jeu.class);
                    intent.putExtra("NB_JOUEURS", nbJoueurs);

                    if (nbJoueurs == 4) {
                        fourPlayers frag = (fourPlayers) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                        if (frag != null) {
                            intent.putExtra("J1_NOM", frag.getSpinnerJ1().getSelectedItem() != null ? frag.getSpinnerJ1().getSelectedItem().toString() : "Joueur 1");
                            intent.putExtra("J2_NOM", frag.getSpinnerJ2().getSelectedItem() != null ? frag.getSpinnerJ2().getSelectedItem().toString() : "Joueur 2");
                            intent.putExtra("J3_NOM", frag.getSpinnerJ3().getSelectedItem() != null ? frag.getSpinnerJ3().getSelectedItem().toString() : "Joueur 3");
                            intent.putExtra("J4_NOM", frag.getSpinnerJ4().getSelectedItem() != null ? frag.getSpinnerJ4().getSelectedItem().toString() : "Joueur 4");
                        }
                    } else {
                        twoPlayers frag = (twoPlayers) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                        if (frag != null) {
                            intent.putExtra("J1_NOM", frag.getSpinnerJ1().getSelectedItem() != null ? frag.getSpinnerJ1().getSelectedItem().toString() : "Joueur 1");
                            intent.putExtra("J2_NOM", frag.getSpinnerJ2().getSelectedItem() != null ? frag.getSpinnerJ2().getSelectedItem().toString() : "Joueur 2");
                        }
                    }
                    startActivity(intent);
                }
            });
        }
    }
}