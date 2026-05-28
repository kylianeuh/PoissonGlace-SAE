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

        rvClassement.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ClassementAdapter(listeJoueurs);
        rvClassement.setAdapter(adapter);

        if (!listeJoueurs.isEmpty()) {
            adapter.notifyDataSetChanged();
        }

        return view;
    }

    public void majListeJoueurs(List<Joueur> nouveauxJoueurs) {
        if (nouveauxJoueurs != null) {
            this.listeJoueurs.clear();
            this.listeJoueurs.addAll(nouveauxJoueurs);

            Collections.sort(this.listeJoueurs, new Comparator<Joueur>() {
                @Override
                public int compare(Joueur j1, Joueur j2) {
                    return Integer.compare(j2.getScore(), j1.getScore());
                }
            });

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }
}