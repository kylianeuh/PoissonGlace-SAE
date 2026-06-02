package com.sae402.poissonglobe;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Jeu extends AppCompatActivity {

    private GameView terrainJeu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                0
        );
        setContentView(R.layout.activity_jeu);

        terrainJeu = findViewById(R.id.calqueJeu);

        if (terrainJeu != null) {
            int nbJoueurs = getIntent().getIntExtra("NB_JOUEURS", 2);
            terrainJeu.nombreDeJoueursConfig = nbJoueurs;

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

            terrainJeu.setOnGameOverListener(new GameView.OnGameOverListener() {
                @Override
                public void onGameOver(final String pseudoVainqueur) {

                    new Thread(() -> {
                        enregistrerPartieEnBdd(nbJoueurs, terrainJeu);
                    }).start();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String équipeGauche = (nbJoueurs == 4) ? terrainJeu.nomJoueurGau + " + " + terrainJeu.nomJoueurGau2 : terrainJeu.nomJoueurGau;
                            String équipeDroite = (nbJoueurs == 4) ? terrainJeu.nomJoueurDro + " + " + terrainJeu.nomJoueurDro2 : terrainJeu.nomJoueurDro;
                            String coreScores = terrainJeu.scoreJoueurGau + " - " + terrainJeu.scoreJoueurDro;

                            FinPartieDialogFragment dialogFin = new FinPartieDialogFragment(
                                    pseudoVainqueur,
                                    équipeGauche,
                                    équipeDroite,
                                    coreScores
                            );

                            dialogFin.show(getSupportFragmentManager(), "GameOverDialog");
                        }
                    });
                }
            });

            terrainJeu.setOnPauseRequestedListener(new GameView.OnPauseRequestedListener() {
                @Override
                public void onPauseRequested() {
                    PauseDialogFragment dialogPause = new PauseDialogFragment(terrainJeu);
                    dialogPause.show(getSupportFragmentManager(), "PauseDialog");
                }
            });
        }
    }

    private void enregistrerPartieEnBdd(int nbJoueurs, GameView terrainJeu) {
        AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
        JeuDAO dao = db.getJeuDAO();

        String resultatGauche = (terrainJeu.scoreJoueurGau >= 6) ? "VICTOIRE" : "DEFAITE";
        String resultatDroit = (terrainJeu.scoreJoueurDro >= 6) ? "VICTOIRE" : "DEFAITE";

        PartieBD nouvellePartie = new PartieBD(System.currentTimeMillis(), nbJoueurs);
        int partieId = (int) dao.insertPartie(nouvellePartie);

        JoueurBD j1 = dao.getJoueurParNom(terrainJeu.nomJoueurGau);
        if (j1 != null) {
            dao.insertJoueurPartie(new JoueurPartieBD(j1.id, partieId, terrainJeu.scoreJoueurGau, resultatGauche));
            dao.ajouterPointsGlobaux(j1.id, terrainJeu.scoreJoueurGau);
        }

        if (nbJoueurs == 4) {
            JoueurBD j3 = dao.getJoueurParNom(terrainJeu.nomJoueurGau2);
            if (j3 != null) {
                dao.insertJoueurPartie(new JoueurPartieBD(j3.id, partieId, terrainJeu.scoreJoueurGau, resultatGauche));
                dao.ajouterPointsGlobaux(j3.id, terrainJeu.scoreJoueurGau);
            }
        }

        JoueurBD j2 = dao.getJoueurParNom(terrainJeu.nomJoueurDro);
        if (j2 != null) {
            dao.insertJoueurPartie(new JoueurPartieBD(j2.id, partieId, terrainJeu.scoreJoueurDro, resultatDroit));
            dao.ajouterPointsGlobaux(j2.id, terrainJeu.scoreJoueurDro);
        }

        if (nbJoueurs == 4) {
            JoueurBD j4 = dao.getJoueurParNom(terrainJeu.nomJoueurDro2);
            if (j4 != null) {
                dao.insertJoueurPartie(new JoueurPartieBD(j4.id, partieId, terrainJeu.scoreJoueurDro, resultatDroit));
                dao.ajouterPointsGlobaux(j4.id, terrainJeu.scoreJoueurDro);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (terrainJeu != null) {
            terrainJeu.couperLesSons();
        }
    }
}