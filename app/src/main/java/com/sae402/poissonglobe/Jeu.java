package com.sae402.poissonglobe;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class Jeu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);

        GameView terrainJeu = findViewById(R.id.calqueJeu);

        if (terrainJeu != null) {
            int nbJoueurs = getIntent().getIntExtra("NB_JOUEURS", 2);
            terrainJeu.nombreDeJoueursConfig = nbJoueurs;

            // Récupération des pseudos (avec sécurité si null)
            String j1 = getIntent().getStringExtra("J1_NOM");
            String j2 = getIntent().getStringExtra("J2_NOM");
            terrainJeu.nomJoueurGau = (j1 != null) ? j1 : "Joueur 1";
            terrainJeu.nomJoueurDro = (j2 != null) ? j2 : "Joueur 2";

            if (nbJoueurs == 4) {
                String j3 = getIntent().getStringExtra("J3_NOM");
                String j4 = getIntent().getStringExtra("J4_NOM");
                terrainJeu.nomJoueurGau2 = (j3 != null) ? j3 : "Joueur 3";
                terrainJeu.nomJoueurDro2 = (j4 != null) ? j4 : "Joueur 4";
            }

            // Gestion de l'événement d'arbitrage de fin de partie
            terrainJeu.setOnGameOverListener(new GameView.OnGameOverListener() {
                @Override
                public void onGameOver(final String pseudoVainqueur) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 1. Instancier le constructeur d'AlertDialog natif
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Jeu.this);

                            // 2. Gonfler (inflate) le layout personnalisé créé précédemment
                            LayoutInflater inflater = getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.dialog_fin_partie, null);
                            builder.setView(dialogView);

                            // 3. Récupérer les pointeurs vers tes composants graphiques personnalisés
                            PoissonGlaceTextView txtVictoire = dialogView.findViewById(R.id.txt_message_victoire);
                            PoissonGlaceTextView txtEquipeGau = dialogView.findViewById(R.id.txt_popup_equipe_gauche);
                            PoissonGlaceTextView txtEquipeDro = dialogView.findViewById(R.id.txt_popup_equipe_droite);
                            PoissonGlaceTextView txtScores = dialogView.findViewById(R.id.txt_popup_scores);
                            PoissonGlaceTextView btnAccueil = dialogView.findViewById(R.id.btn_dialog_accueil);

                            // 4. Injecter dynamiquement les textes à afficher
                            if (txtVictoire != null) {
                                txtVictoire.setText("Victoire de " + pseudoVainqueur + " !");
                            }

                            // Assemblage des chaînes d'équipe selon le mode 1v1 ou 2v2
                            String equipeGaucheStr = (nbJoueurs == 4) ? terrainJeu.nomJoueurGau + " + " + terrainJeu.nomJoueurGau2 : terrainJeu.nomJoueurGau;
                            String equipeDroiteStr = (nbJoueurs == 4) ? terrainJeu.nomJoueurDro + " + " + terrainJeu.nomJoueurDro2 : terrainJeu.nomJoueurDro;

                            if (txtEquipeGau != null) txtEquipeGau.setText(equipeGaucheStr);
                            if (txtEquipeDro != null) txtEquipeDro.setText(equipeDroiteStr);
                            if (txtScores != null) {
                                txtScores.setText(" " + terrainJeu.scoreJoueurGau + " - " + terrainJeu.scoreJoueurDro + " ");
                            }

                            // 5. Verrouiller la fermeture intempestive et créer l'AlertDialog
                            builder.setCancelable(false);
                            final android.app.AlertDialog dialog = builder.create();

                            // TRUC ERGONOMIQUE : Supprimer l'arrière-plan par défaut d'Android
                            // pour rendre les coins de ton asset "bouton_arrondi_bordure" parfaitement lisses
                            if (dialog.getWindow() != null) {
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            }

                            // 6. Brancher l'action de fermeture sur le bouton textuel
                            if (btnAccueil != null) {
                                btnAccueil.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        finish(); // Ferme l'activité du jeu et retourne à l'accueil
                                    }
                                });
                            }

                            // 7. Affichage final du Pop-up
                            dialog.show();
                        }
                    });
                }
            });
        }
    }
}