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
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
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

        View.OnClickListener openDialogListener = v -> {
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
        // CORRECTION CRITIQUE : Requête SQL sur un thread secondaire pour éviter le blocage
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getAppDatabase(requireContext());
                List<JoueurBD> joueurs = db.getJeuDAO().getAllJoueurs();

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
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
                                    textView.setTextSize(30f);
                                }
                                return view;
                            }

                            @Override
                            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                android.widget.TextView textView = view.findViewById(android.R.id.text1);

                                if (textView != null) {
                                    view.setBackgroundColor(android.graphics.Color.parseColor("#22A7F0"));
                                    textView.setTextColor(android.graphics.Color.WHITE);
                                    textView.setTypeface(null, android.graphics.Typeface.BOLD);
                                    textView.setTextSize(30f);

                                    int pHorizontal = (int) (24 * parent.getContext().getResources().getDisplayMetrics().density);
                                    textView.setPadding(pHorizontal, 0, pHorizontal, 0);

                                    int hauteurPixels = (int) (70 * parent.getContext().getResources().getDisplayMetrics().density);

                                    ViewGroup.LayoutParams params = view.getLayoutParams();
                                    if (params == null) {
                                        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, hauteurPixels);
                                    } else {
                                        params.height = hauteurPixels;
                                    }
                                    view.setLayoutParams(params);
                                    textView.setGravity(android.view.Gravity.CENTER_VERTICAL);
                                }
                                return view;
                            }
                        };

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinnerJ1.setAdapter(adapter);
                        spinnerJ2.setAdapter(adapter);
                        spinnerJ3.setAdapter(adapter);
                        spinnerJ4.setAdapter(adapter);

                        android.graphics.drawable.GradientDrawable insideBorder = new android.graphics.drawable.GradientDrawable();
                        insideBorder.setColor(android.graphics.Color.parseColor("#22A7F0"));
                        insideBorder.setStroke(8, android.graphics.Color.WHITE);

                        int radiusPixel = (int) (16 * requireContext().getResources().getDisplayMetrics().density);
                        insideBorder.setCornerRadius(radiusPixel);

                        android.graphics.drawable.Drawable[] layers = {insideBorder};
                        android.graphics.drawable.LayerDrawable finalPopupBackground = new android.graphics.drawable.LayerDrawable(layers);

                        int paddingPopup = (int) (12 * requireContext().getResources().getDisplayMetrics().density);
                        finalPopupBackground.setLayerInset(0, paddingPopup, paddingPopup, paddingPopup, paddingPopup);

                        spinnerJ1.setPopupBackgroundDrawable(finalPopupBackground);
                        spinnerJ2.setPopupBackgroundDrawable(finalPopupBackground);
                        spinnerJ3.setPopupBackgroundDrawable(finalPopupBackground);
                        spinnerJ4.setPopupBackgroundDrawable(finalPopupBackground);

                        adapter.notifyDataSetChanged();

                        if (nomsJoueurs.size() >= 4) {
                            spinnerJ1.setSelection(0, false);
                            spinnerJ2.setSelection(1, false);
                            spinnerJ3.setSelection(2, false);
                            spinnerJ4.setSelection(3, false);
                        } else if (nomsJoueurs.size() >= 2) {
                            spinnerJ1.setSelection(0, false);
                            spinnerJ2.setSelection(1, false);
                        }

                        android.widget.AdapterView.OnItemSelectedListener ecouteurAntiDoublon = new android.widget.AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                String joueurSelectionne = parent.getItemAtPosition(position).toString();
                                verifierDoublon((android.widget.Spinner) parent, joueurSelectionne);
                            }

                            @Override
                            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                        };

                        spinnerJ1.setOnItemSelectedListener(ecouteurAntiDoublon);
                        spinnerJ2.setOnItemSelectedListener(ecouteurAntiDoublon);
                        spinnerJ3.setOnItemSelectedListener(ecouteurAntiDoublon);
                        spinnerJ4.setOnItemSelectedListener(ecouteurAntiDoublon);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void verifierDoublon(android.widget.Spinner spinnerModifie, String nomJoueur) {
        android.widget.Spinner[] tousLesSpinners = {spinnerJ1, spinnerJ2, spinnerJ3, spinnerJ4};

        for (int i = 0; i < tousLesSpinners.length; i++) {
            android.widget.Spinner autreSpinner = tousLesSpinners[i];

            if (autreSpinner != spinnerModifie) {
                if (autreSpinner.getSelectedItem() != null && autreSpinner.getSelectedItem().toString().equals(nomJoueur)) {
                    android.widget.Toast.makeText(requireContext(), nomJoueur + " est déjà sélectionné !", android.widget.Toast.LENGTH_SHORT).show();

                    int positionLibre = trouverPositionLibre(tousLesSpinners, spinnerModifie);
                    spinnerModifie.setSelection(positionLibre);
                    break;
                }
            }
        }
    }

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
            if (!positionPrise) return pos;
        }
        return 0;
    }

    public Spinner getSpinnerJ1() { return spinnerJ1; }
    public Spinner getSpinnerJ2() { return spinnerJ2; }
    public Spinner getSpinnerJ3() { return spinnerJ3; }
    public Spinner getSpinnerJ4() { return spinnerJ4; }
}