package com.sae402.poissonglobe;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private List<JoueurBD> listeGlobaleJoueurs = new ArrayList<>();
    private JeuDAO jeuDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppDatabase db = AppDatabase.getAppDatabase(this);
        jeuDAO = db.getJeuDAO();

        // CORRECTION ASYNCHRONE : On charge les joueurs en tâche de fond
        new Thread(() -> {
            try {
                listeGlobaleJoueurs = jeuDAO.getAllJoueurs();

                // On met à jour l'interface graphique une fois la requête finie
                runOnUiThread(() -> {
                    chargerVueSelection();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void chargerVueSelection() {
        setContentView(R.layout.stats_selection);

        View btnRetour = findViewById(R.id.btnRetourDetails);
        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> finish());
        }

        RecyclerView rvChoixJoueurs = findViewById(R.id.rvChoixJoueurs);

        if (rvChoixJoueurs != null) {
            SelectionJoueursAdapter adapterSelection = new SelectionJoueursAdapter(listeGlobaleJoueurs, new SelectionJoueursAdapter.OnJoueurClickListener() {
                @Override
                public void onJoueurClick(String nomClique) {
                    chargerVueDetails(nomClique);
                }
            });
            rvChoixJoueurs.setAdapter(adapterSelection);
        }
    }

    private void chargerVueDetails(String nomJoueur) {
        setContentView(R.layout.stats_details);

        TextView txtNomJoueur = findViewById(R.id.txtNomJoueur);
        if (txtNomJoueur != null) {
            txtNomJoueur.setText(nomJoueur);
        }

        View btnRetour = findViewById(R.id.btnRetourDetails);
        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> chargerVueSelection());
        }

        TextView txtParties = findViewById(R.id.txtPartiesJouees);
        TextView txtGagnees = findViewById(R.id.txtPartiesGagnees);
        TextView txtPoints = findViewById(R.id.txtPointsTotal);

        // CORRECTION REQUÊTE : Récupération des détails sur un thread de travail rapide
        new Thread(() -> {
            try {
                final JoueurBD joueurSelectionne = jeuDAO.getJoueurByNom(nomJoueur);
                int nbPartiesCalcule = 0;
                int scoreGlobalCalcule = 0;

                if (joueurSelectionne != null) {
                    scoreGlobalCalcule = joueurSelectionne.scoreGlobal;
                    List<JoueurPartieBD> listeScores = jeuDAO.getScoresByJoueur(joueurSelectionne.id);
                    if (listeScores != null) {
                        nbPartiesCalcule = listeScores.size();
                    }
                }

                final int parties = nbPartiesCalcule;
                final int points = scoreGlobalCalcule;

                runOnUiThread(() -> {
                    if (txtParties != null) txtParties.setText("Nombre de parties jouées : " + parties);
                    if (txtGagnees != null) txtGagnees.setText("Nombre de parties gagnées : " + (parties / 2));
                    if (txtPoints != null) txtPoints.setText("Nombre de points au total : " + points);

                    ClassementFragment fragClassement = new ClassementFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.zoneClassement, fragClassement)
                            .commitNow();

                    List<Joueur> listePourVisualisation = new ArrayList<>();
                    for (JoueurBD jBD : listeGlobaleJoueurs) {
                        listePourVisualisation.add(new Joueur(jBD.nom, jBD.scoreGlobal));
                    }
                    fragClassement.majListeJoueurs(listePourVisualisation);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}