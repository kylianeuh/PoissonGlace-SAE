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

        // CORRECTION CRITIQUE : Chargement de la liste en tâche de fond pour éviter le crash
        new Thread(() -> {
            listeGlobaleJoueurs = jeuDAO.getAllJoueurs();

            // Une fois la liste chargée, on bascule sur l'interface graphique pour afficher la vue
            runOnUiThread(() -> {
                chargerVueSelection();
            });
        }).start();
    }

    private void chargerVueSelection() {
        setContentView(R.layout.stats_selection);

        View btnRetour = findViewById(R.id.btnRetourDetails);
        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> finish());
        }

        RecyclerView rvChoixJoueurs = findViewById(R.id.rvChoixJoueurs);

        SelectionJoueursAdapter adapterSelection = new SelectionJoueursAdapter(listeGlobaleJoueurs, new SelectionJoueursAdapter.OnJoueurClickListener() {
            @Override
            public void onJoueurClick(String nomClique) {
                chargerVueDetails(nomClique);
            }
        });

        if (rvChoixJoueurs != null) {
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

        JoueurBD joueurSelectionne = jeuDAO.getJoueurParNom(nomJoueur);

        if (joueurSelectionne != null) {
            final int idJoueur = joueurSelectionne.id;

            new Thread(() -> {
                // TODO : Aller chercher les vraies valeurs en BDD à l'aide de idJoueur si besoin !
                int nbParties = 0;
                int nbVictoires = 0;
                int totalPoints = joueurSelectionne.scoreGlobal;

                runOnUiThread(() -> {
                    if (txtParties != null) txtParties.setText(getString(R.string.stat_parties_jouees, nbParties));
                    if (txtGagnees != null) txtGagnees.setText(getString(R.string.stat_parties_gagnees, nbVictoires));
                    if (txtPoints != null) txtPoints.setText(getString(R.string.stat_points_total, totalPoints));
                });
            }).start();
        }

        ClassementFragment fragClassement = new ClassementFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.zoneClassement, fragClassement)
                .commitNow();

        List<Joueur> listePourVisualisation = new ArrayList<>();
        for (JoueurBD jBD : listeGlobaleJoueurs) {
            listePourVisualisation.add(new Joueur(jBD.nom, jBD.scoreGlobal));
        }

        fragClassement.majListeJoueurs(listePourVisualisation);
    }
}