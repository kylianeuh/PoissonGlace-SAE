package com.sae402.poissonglobe;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Jeu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);

        // CORRECTION ICI : On utilise bien l'ID "calqueJeu" de ton XML
        GameView gameView = findViewById(R.id.calqueJeu);

        if (gameView != null) {
            // Récupérer le nombre de joueurs (2 par défaut)
            int nbJoueurs = getIntent().getIntExtra("NB_JOUEURS", 2);
            gameView.nombreDeJoueursConfig = nbJoueurs;

            // Récupérer et attribuer les pseudos (1v1)
            gameView.nomJoueurGau = getIntent().getStringExtra("J1_NOM");
            gameView.nomJoueurDro = getIntent().getStringExtra("J2_NOM");

            // Si on est en mode 4 joueurs (2v2), on récupère aussi les coéquipiers
            if (nbJoueurs == 4) {
                gameView.nomJoueurGau2 = getIntent().getStringExtra("J3_NOM");
                gameView.nomJoueurDro2 = getIntent().getStringExtra("J4_NOM");
            }
        }
    }
}