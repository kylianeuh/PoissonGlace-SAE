package com.sae402.poissonglobe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class GestionJoueurActivity extends AppCompatActivity {

    // 1. ON DECLARE LA VARIABLE ICI TOUT EN HAUT
    private Fragment fragmentChoisi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_joueur);

        int nbJoueurs = getIntent().getIntExtra("NB_JOUEURS", 2);

        // 2. ICI ON ENLEVE le mot "Fragment" devant, on fait juste l'assignation
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

                    // Maintenant, Java accepte à 100% d'utiliser fragmentChoisi ici !
                    if (nbJoueurs == 4 && fragmentChoisi instanceof fourPlayers) {
                        fourPlayers frag = (fourPlayers) fragmentChoisi;
                        intent.putExtra("J1_NOM", frag.getSpinnerJ1().getSelectedItem() != null ? frag.getSpinnerJ1().getSelectedItem().toString() : "Joueur 1");
                        intent.putExtra("J2_NOM", frag.getSpinnerJ2().getSelectedItem() != null ? frag.getSpinnerJ2().getSelectedItem().toString() : "Joueur 2");
                        intent.putExtra("J3_NOM", frag.getSpinnerJ3().getSelectedItem() != null ? frag.getSpinnerJ3().getSelectedItem().toString() : "Joueur 3");
                        intent.putExtra("J4_NOM", frag.getSpinnerJ4().getSelectedItem() != null ? frag.getSpinnerJ4().getSelectedItem().toString() : "Joueur 4");
                    } else if (nbJoueurs == 2 && fragmentChoisi instanceof twoPlayers) {
                        twoPlayers frag = (twoPlayers) fragmentChoisi;
                        intent.putExtra("J1_NOM", frag.getSpinnerJ1().getSelectedItem() != null ? frag.getSpinnerJ1().getSelectedItem().toString() : "Joueur 1");
                        intent.putExtra("J2_NOM", frag.getSpinnerJ2().getSelectedItem() != null ? frag.getSpinnerJ2().getSelectedItem().toString() : "Joueur 2");
                    }
                    startActivity(intent);
                }
            });
        }
    }
}