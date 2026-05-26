package com.sae402.poissonglobe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Vérifie bien cet import !
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClassementFragment extends Fragment {

    private RecyclerView rvClassement;
    private ClassementAdapter adapter;
    private List<Joueur> listeJoueurs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classement, container, false);

        rvClassement = view.findViewById(R.id.rvClassement);

        // CORRECTION 1 : On s'assure que le LayoutManager est bien configuré en Java aussi
        rvClassement.setLayoutManager(new LinearLayoutManager(getContext()));

        // On initialise l'adapter avec notre liste de joueurs
        adapter = new ClassementAdapter(listeJoueurs);
        rvClassement.setAdapter(adapter);

        // CORRECTION 2 : Si des joueurs ont été envoyés avant que la vue ne soit prête,
        // on force l'affichage maintenant qu'elle l'est !
        if (!listeJoueurs.isEmpty()) {
            adapter.notifyDataSetChanged();
        }

        return view;
    }

    /**
     * Reçoit la liste des joueurs, la trie du plus grand au plus petit score,
     * et rafraîchit l'affichage du tableau.
     */
    public void majListeJoueurs(List<Joueur> nouveauxJoueurs) {
        if (nouveauxJoueurs != null) {
            this.listeJoueurs.clear();
            this.listeJoueurs.addAll(nouveauxJoueurs);

            // Tri automatique décroissant
            Collections.sort(this.listeJoueurs, new Comparator<Joueur>() {
                @Override
                public int compare(Joueur j1, Joueur j2) {
                    return Integer.compare(j2.getScore(), j1.getScore());
                }
            });

            // Si l'interface est déjà prête, on redessine tout de suite
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }
}