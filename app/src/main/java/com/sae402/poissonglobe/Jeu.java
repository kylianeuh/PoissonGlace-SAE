package com.sae402.poissonglobe;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Jeu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);

        // 1. Récupération de la vue du terrain
        GameView terrainJeu = findViewById(R.id.calqueJeu);

        if (terrainJeu != null) {
            // 2. Extraction des données envoyées depuis l'activité précédente
            int nbJoueurs = getIntent().getIntExtra("NB_JOUEURS", 2);
            terrainJeu.nombreDeJoueursConfig = nbJoueurs;

            terrainJeu.nomJoueurGau = getIntent().getStringExtra("J1_NOM");
            terrainJeu.nomJoueurDro = getIntent().getStringExtra("J2_NOM");

            if (nbJoueurs == 4) {
                terrainJeu.nomJoueurGau2 = getIntent().getStringExtra("J3_NOM");
                terrainJeu.nomJoueurDro2 = getIntent().getStringExtra("J4_NOM");
            }

            // 3. Gestionnaire d'événement de fin de partie avec Pop-up
            terrainJeu.setOnGameOverListener(new GameView.OnGameOverListener() {
                @Override
                public void onGameOver(final String pseudoVainqueur) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Jeu.this);
                            builder.setTitle("Terminé !");

                            // Construction dynamique des scores pour le message selon le mode
                            String équipeGauche = (nbJoueurs == 4) ? terrainJeu.nomJoueurGau + " + " + terrainJeu.nomJoueurGau2 : terrainJeu.nomJoueurGau;
                            String équipeDroite = (nbJoueurs == 4) ? terrainJeu.nomJoueurDro + " + " + terrainJeu.nomJoueurDro2 : terrainJeu.nomJoueurDro;

                            String message = "Victoire de " + pseudoVainqueur + " !\n\n"
                                    + équipeGauche + "   " + terrainJeu.scoreJoueurGau
                                    + "  -  "
                                    + terrainJeu.scoreJoueurDro + "   " + équipeDroite;

                            builder.setMessage(message);

                            builder.setPositiveButton("Retour à l'accueil", (dialog, which) -> {
                                finish();
                            });

                            builder.setCancelable(false);
                            android.app.AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
            });
        }
    }
}