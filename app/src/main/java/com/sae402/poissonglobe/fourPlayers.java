package com.sae402.poissonglobe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;import java.util.ArrayList;
import java.util.List;

public class fourPlayers extends Fragment implements AddUserDialogFragment.OnUserAddedListener {

    private Spinner spinnerJ1, spinnerJ2, spinnerJ3, spinnerJ4;
    private List<String> nomsJoueurs = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_four_players, container, false);


        spinnerJ1 = view.findViewById(R.id.spinnerJ1);
        spinnerJ2 = view.findViewById(R.id.spinnerJ2);
        spinnerJ3 = view.findViewById(R.id.spinnerJ3);
        spinnerJ4 = view.findViewById(R.id.spinnerJ4);
        View btnAddJ1 = view.findViewById(R.id.btnAddJ1);
        View btnAddJ2 = view.findViewById(R.id.btnAddJ2);
        View btnAddJ3 = view.findViewById(R.id.btnAddJ3);
        View btnAddJ4 = view.findViewById(R.id.btnAddJ4);


        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nomsJoueurs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerJ1.setAdapter(adapter);
        spinnerJ2.setAdapter(adapter);
        spinnerJ3.setAdapter(adapter);
        spinnerJ4.setAdapter(adapter);

        refreshSpinners();

        View.OnClickListener openDialogListener = v ->{
            AddUserDialogFragment dialog = new AddUserDialogFragment();
            dialog.show(getChildFragmentManager(), "AddUser");
        };

        btnAddJ1.setOnClickListener(openDialogListener);
        btnAddJ2.setOnClickListener(openDialogListener);
        btnAddJ3.setOnClickListener(openDialogListener);
        btnAddJ4.setOnClickListener(openDialogListener);

        return view;
    }
    @Override
    public void onUserAdded() {
        refreshSpinners();
    }private void refreshSpinners() {
        AppDatabase db = AppDatabase.getAppDatabase(requireContext());List<JoueurBD> joueurs = db.getJeuDAO().getAllJoueurs();
        nomsJoueurs.clear();
        for (JoueurBD j : joueurs) {
            nomsJoueurs.add(j.nom);
        }
        adapter.notifyDataSetChanged();
    }}