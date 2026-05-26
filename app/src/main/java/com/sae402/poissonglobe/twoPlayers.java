package com.sae402.poissonglobe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // Import manquant
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;import java.util.ArrayList;
import java.util.List;

public class twoPlayers extends Fragment implements AddUserDialogFragment.OnUserAddedListener {

    private Spinner spinnerJ1, spinnerJ2;
    private List<String> nomsJoueurs = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {// Correction : "LayoutInflater inflater" (avec un espace)
        View view = inflater.inflate(R.layout.fragment_two_players, container, false);

        spinnerJ1 = view.findViewById(R.id.spinnerJ1);
        spinnerJ2 = view.findViewById(R.id.spinnerJ2);
        View btnAddJ1 = view.findViewById(R.id.btnAddJ1);
        View btnAddJ2 =view.findViewById(R.id.btnAddJ2);

        // Correction : "new ArrayAdapter" (avec un espace)
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nomsJoueurs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerJ1.setAdapter(adapter);
        spinnerJ2.setAdapter(adapter);refreshSpinners();

        View.OnClickListener openDialogListener = v -> {
            AddUserDialogFragment dialog= new AddUserDialogFragment();
            dialog.show(getChildFragmentManager(), "AddUser");
        };btnAddJ1.setOnClickListener(openDialogListener);
        btnAddJ2.setOnClickListener(openDialogListener);return view;
    }

    @Override
    public void onUserAdded() {
        refreshSpinners();
    }

    private void refreshSpinners() {
        AppDatabase db = AppDatabase.getAppDatabase(requireContext());
        List<JoueurBD> joueurs = db.getJeuDAO().getAllJoueurs();

        nomsJoueurs.clear();
        for (JoueurBD j : joueurs){
            nomsJoueurs.add(j.nom);
        }
        adapter.notifyDataSetChanged();
    }
}