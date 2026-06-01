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

        listeGlobaleJoueurs = jeuDAO.getAllJoueurs();

        chargerVueSelection();
    }

    private void chargerVueSelection() {
        setContentView(R.layout.stats_selection);

        View btnRetour = findViewById(R.id.btnRetourDetails);
        btnRetour.setOnClickListener(v -> finish());

        RecyclerView rvChoixJoueurs = findViewById(R.id.rvChoixJoueurs);

        SelectionJoueursAdapter adapterSelection = new SelectionJoueursAdapter(listeGlobaleJoueurs, new SelectionJoueursAdapter.OnJoueurClickListener() {
            @Override
            public void onJoueurClick(String nomClique) {
                chargerVueDetails(nomClique);
            }
        });

        rvChoixJoueurs.setAdapter(adapterSelection);
    }

    private void chargerVueDetails(String nomJoueur) {
        setContentView(R.layout.stats_details);

        TextView txtNomJoueur = findViewById(R.id.txtNomJoueur);
        txtNomJoueur.setText(nomJoueur);

        View btnRetour = findViewById(R.id.btnRetourDetails);
        btnRetour.setOnClickListener(v -> chargerVueSelection());

        TextView txtParties = findViewById(R.id.txtPartiesJouees);
        TextView txtGagnees = findViewById(R.id.txtPartiesGagnees);
        TextView txtPoints = findViewById(R.id.txtPointsTotal);

        JoueurBD joueurSelectionne = jeuDAO.getJoueurByNom(nomJoueur);

        int nbParties = 0;
        int scoreAffichage = 0;

        if (joueurSelectionne != null) {
            scoreAffichage = joueurSelectionne.scoreGlobal;

            List<JoueurPartieBD> listeScores = jeuDAO.getScoresByJoueur(joueurSelectionne.id);
            if (listeScores != null) {
                nbParties = listeScores.size();
            }
        }

        txtParties.setText("Nombre de parties jouées : " + nbParties);
        txtGagnees.setText("Nombre de parties gagnées : " + (nbParties / 2));
        txtPoints.setText("Nombre de points au total : " + scoreAffichage);

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