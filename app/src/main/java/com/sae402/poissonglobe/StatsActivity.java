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

        // Récupération initiale de la liste des joueurs
        listeGlobaleJoueurs = jeuDAO.getAllJoueurs();

        chargerVueSelection();
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

        // Récupération de l'entité du joueur pour obtenir ses points totaux accumulés
        JoueurBD joueurSelectionne = jeuDAO.getJoueurByNom(nomJoueur);

        if (joueurSelectionne != null) {
            final int idJoueur = joueurSelectionne.id;
            final int scoreGlobal = joueurSelectionne.scoreGlobal;

            // INTELLIGENCE BDD : On exécute les calculs de stats en arrière-plan
            new Thread(() -> {
                // Utilisation des requêtes COUNT(*) de ton JeuDAO
                int totalJouees = jeuDAO.getNombrePartiesJouees(idJoueur);
                int totalVictoires = jeuDAO.getNombreVictoires(idJoueur);

                // On renvoie les textes calculés sur le Thread de l'interface graphique
                runOnUiThread(() -> {
                    if (txtParties != null) txtParties.setText("Nombre de parties jouées : " + totalJouees);
                    if (txtGagnees != null) txtGagnees.setText("Nombre de parties gagnées : " + totalVictoires);
                    if (txtPoints != null) txtPoints.setText("Nombre de points au total : " + scoreGlobal);
                });
            }).start();
        }

        // --- GESTION ET CHARGEMENT DU CLASSEMENT GENERAL ---
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