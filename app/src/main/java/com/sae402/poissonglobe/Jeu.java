package com.sae402.poissonglobe;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Jeu  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);

        GameView terrainJeu = findViewById(R.id.calqueJeu);

        terrainJeu.setOnGameOverListener(new GameView.OnGameOverListener() {
            @Override
            public void onGameOver(final String pseudoVainqueur) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Jeu.this);

                        builder.setTitle("Terminé !");

                        String message = "Victoire de " + pseudoVainqueur + " !\n\n"
                                + terrainJeu.nomJoueurGau + "   " + terrainJeu.scoreJoueurGau
                                + "  -  "
                                + terrainJeu.scoreJoueurDro + "   " + terrainJeu.nomJoueurDro;

                        builder.setMessage(message);

                        builder.setPositiveButton("Retour à l'accueil", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                // ex: enregistrerPartieDansBDD();

                                finish();
                            }
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
