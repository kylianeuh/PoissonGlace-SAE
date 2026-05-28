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
    }

    private void refreshSpinners() {
        AppDatabase db = AppDatabase.getAppDatabase(requireContext());
        List<JoueurBD> joueurs = db.getJeuDAO().getAllJoueurs();

        nomsJoueurs.clear();
        for (JoueurBD j : joueurs){
            nomsJoueurs.add(j.nom);
        }

        adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, nomsJoueurs) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(android.graphics.Color.WHITE);
                    textView.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(android.graphics.Color.WHITE);
                    textView.setTypeface(null, android.graphics.Typeface.BOLD);

                    int paddingHorizontal = (int) (16 * parent.getContext().getResources().getDisplayMetrics().density);
                    int paddingVertical = (int) (12 * parent.getContext().getResources().getDisplayMetrics().density);

                    textView.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerJ1.setAdapter(adapter);
        spinnerJ2.setAdapter(adapter);
        spinnerJ3.setAdapter(adapter);
        spinnerJ4.setAdapter(adapter);

        android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
        border.setColor(android.graphics.Color.parseColor("#22A7F0"));
        border.setStroke(8, android.graphics.Color.WHITE);

        int radiusPixel = (int) (16 * requireContext().getResources().getDisplayMetrics().density);
        border.setCornerRadius(radiusPixel);

        spinnerJ1.setPopupBackgroundDrawable(border);
        spinnerJ2.setPopupBackgroundDrawable(border);
        spinnerJ3.setPopupBackgroundDrawable(border);
        spinnerJ4.setPopupBackgroundDrawable(border);

        adapter.notifyDataSetChanged();

        android.widget.AdapterView.OnItemSelectedListener ecouteurAntiDoublon = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String joueurSelectionne = parent.getItemAtPosition(position).toString();
                verifierDoublon((android.widget.Spinner) parent, joueurSelectionne);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        };

// On applique cet écouteur unique sur tes 4 spinners
        spinnerJ1.setOnItemSelectedListener(ecouteurAntiDoublon);
        spinnerJ2.setOnItemSelectedListener(ecouteurAntiDoublon);
        spinnerJ3.setOnItemSelectedListener(ecouteurAntiDoublon);
        spinnerJ4.setOnItemSelectedListener(ecouteurAntiDoublon);


    }

    private void verifierDoublon(android.widget.Spinner spinnerModifie, String nomJoueur) {
        android.widget.Spinner[] tousLesSpinners = {spinnerJ1, spinnerJ2, spinnerJ3, spinnerJ4};

        for (int i = 0; i < tousLesSpinners.length; i++) {
            android.widget.Spinner autreSpinner = tousLesSpinners[i];

            // On ne compare pas le spinner avec lui-même
            if (autreSpinner != spinnerModifie) {

                // Si le joueur sélectionné est déjà pris ailleurs
                if (autreSpinner.getSelectedItem() != null && autreSpinner.getSelectedItem().toString().equals(nomJoueur)) {
                    android.widget.Toast.makeText(requireContext(), nomJoueur + " est déjà sélectionné !", android.widget.Toast.LENGTH_SHORT).show();

                    // On cherche une position libre dans la liste pour y replacer notre spinner
                    int positionLibre = trouverPositionLibre(tousLesSpinners, spinnerModifie);
                    spinnerModifie.setSelection(positionLibre);
                    break;
                }
            }
        }
    }

    // Fonction bonus qui cherche automatiquement un index non utilisé par les autres
    private int trouverPositionLibre(android.widget.Spinner[] spinners, android.widget.Spinner spinnerActuel) {
        int totalItems = spinnerActuel.getCount();
        for (int pos = 0; pos < totalItems; pos++) {
            boolean positionPrise = false;
            for (android.widget.Spinner s : spinners) {
                if (s != spinnerActuel && s.getSelectedItemPosition() == pos) {
                    positionPrise = true;
                    break;
                }
            }
            if (!positionPrise) return pos; // On a trouvé un pseudo de libre !
        }
        return 0; // Par défaut si la base est trop petite
    }
    public Spinner getSpinnerJ1() { return spinnerJ1; }
    public Spinner getSpinnerJ2() { return spinnerJ2; }
    public Spinner getSpinnerJ3() { return spinnerJ3; }
    public Spinner getSpinnerJ4() { return spinnerJ4; }
}