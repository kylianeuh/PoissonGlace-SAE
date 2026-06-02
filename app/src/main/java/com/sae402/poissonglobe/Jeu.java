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

            // Protection anti-null : Valeurs par défaut directes si les extras reviennent vides
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

                    // SAUVEGARDE DANS LA BDD (Exécutée sur un thread secondaire pour ne pas figer l'affichage)
                    new Thread(() -> {
                        enregistrerPartieEnBdd(nbJoueurs, terrainJeu);
                    }).start();

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

// LOCALISATION : Dans Jeu.java, remplace TOUTE ta méthode enregistrerPartieEnBdd par celle-ci :

    private void enregistrerPartieEnBdd(int nbJoueurs, GameView terrainJeu) {
        AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
        JeuDAO dao = db.getJeuDAO();

        // 1. Déterminer les statuts de victoire/défaite pour chaque camp
        String resultatGauche = (terrainJeu.scoreJoueurGau >= 6) ? "VICTOIRE" : "DEFAITE";
        String resultatDroit = (terrainJeu.scoreJoueurDro >= 6) ? "VICTOIRE" : "DEFAITE";

        // 2. Création et insertion de la manche de jeu principale
        PartieBD nouvellePartie = new PartieBD(System.currentTimeMillis(), nbJoueurs);
        int partieId = (int) dao.insertPartie(nouvellePartie);

        // 3. Traitement de l'équipe Gauche (Joueur 1 obligatoire)
        JoueurBD j1 = dao.getJoueurParNom(terrainJeu.nomJoueurGau);
        if (j1 != null) {
            // Insertion du rapport de match
            dao.insertJoueurPartie(new JoueurPartieBD(j1.id, partieId, terrainJeu.scoreJoueurGau, resultatGauche));
            // Mise à jour de ses points globaux (1 but marqué = 1 point global ajouté)
            dao.ajouterPointsGlobaux(j1.id, terrainJeu.scoreJoueurGau);
        }

        // Si mode 2v2 : Traitement du Joueur 3 (Coéquipier gauche)
        if (nbJoueurs == 4) {
            JoueurBD j3 = dao.getJoueurParNom(terrainJeu.nomJoueurGau2);
            if (j3 != null) {
                dao.insertJoueurPartie(new JoueurPartieBD(j3.id, partieId, terrainJeu.scoreJoueurGau, resultatGauche));
                dao.ajouterPointsGlobaux(j3.id, terrainJeu.scoreJoueurGau);
            }
        }

        // 4. Traitement de l'équipe Droite (Joueur 2 obligatoire)
        JoueurBD j2 = dao.getJoueurParNom(terrainJeu.nomJoueurDro);
        if (j2 != null) {
            dao.insertJoueurPartie(new JoueurPartieBD(j2.id, partieId, terrainJeu.scoreJoueurDro, resultatDroit));
            dao.ajouterPointsGlobaux(j2.id, terrainJeu.scoreJoueurDro);
        }

        // Si mode 2v2 : Traitement du Joueur 4 (Coéquipier droit)
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
        // Libération de la mémoire du SoundPool pour éviter les fuites de ressources en tâche de fond
        GameView terrainJeu = findViewById(R.id.calqueJeu);
        if (terrainJeu != null) {
            terrainJeu.couperLesSons();
        }
    }
}