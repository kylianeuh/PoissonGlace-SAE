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

            // PROTECTION ANTI-NULL : Valeurs par défaut directes si les extras reviennent vides
            String j1 = getIntent().getStringExtra("J1_NOM");
            String j2 = getIntent().getStringExtra("J2_NOM");
            terrainJeu.nomJoueurGau = (j1 != null && !j1.isEmpty()) ? j1 : "Joueur 1";
            terrainJeu.nomJoueurDro = (j2 != null && !j2.isEmpty()) ? j2 : "Joueur 2";

            if (nbJoueurs == 4) {
                String j3 = getIntent().getStringExtra("J3_NOM");
                String j4 = getIntent().getStringExtra("J4_NOM");
                terrainJeu.nomJoueurGau2 = (j3 != null && !j3.isEmpty()) ? j3 : "Joueur 3";
                terrainJeu.nomJoueurDro2 = (j4 != null && !j4.isEmpty()) ? j4 : "Joueur 4";
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libération de la mémoire du SoundPool pour éviter les fuites de ressources en tâche de fond
        GameView terrainJeu = findViewById(R.id.calqueJeu);
        if (terrainJeu != null) {
            terrainJeu.couperLesSons();
        }
    }
}