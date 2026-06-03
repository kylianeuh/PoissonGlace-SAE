package com.sae402.poissonglobe;

import android.content.Context;
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

public class twoPlayers extends Fragment implements AddUserDialogFragment.OnUserAddedListener {

    private Spinner spinnerJ1, spinnerJ2;
    private List<String> nomsJoueurs = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two_players, container, false);

        spinnerJ1 = view.findViewById(R.id.spinnerJ1);
        spinnerJ2 = view.findViewById(R.id.spinnerJ2);
        View btnAddJ1 = view.findViewById(R.id.btnAddJ1);
        View btnAddJ2 = view.findViewById(R.id.btnAddJ2);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nomsJoueurs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerJ1.setAdapter(adapter);
        spinnerJ2.setAdapter(adapter);

        refreshSpinners();

        View.OnClickListener openDialogListener = v -> {
            AddUserDialogFragment dialog = new AddUserDialogFragment();
            dialog.setTargetFragment(this, 0);
            dialog.show(getChildFragmentManager(), "AddUser");
        };

        btnAddJ1.setOnClickListener(openDialogListener);
        btnAddJ2.setOnClickListener(openDialogListener);

        return view;
    }

    @Override
    public void onUserAdded() {
        refreshSpinners();
    }

    private void refreshSpinners() {
        new Thread(() -> {
            try {
                if (!isAdded() || getContext() == null) return;

                AppDatabase db = AppDatabase.getAppDatabase(getContext());
                List<JoueurBD> joueurs = db.getJeuDAO().getAllJoueurs();

                if (getActivity() != null) {
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

                        android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
                        border.setColor(android.graphics.Color.parseColor("#22A7F0"));
                        border.setStroke(8, android.graphics.Color.WHITE);

                        int radiusPixel = (int) (16 * requireContext().getResources().getDisplayMetrics().density);
                        border.setCornerRadius(radiusPixel);

                        spinnerJ1.setPopupBackgroundDrawable(border);
                        spinnerJ2.setPopupBackgroundDrawable(border);

                        adapter.notifyDataSetChanged();

                        if (nomsJoueurs.size() >= 2) {
                            spinnerJ1.setSelection(0);
                            spinnerJ2.setSelection(1);
                        }

                        spinnerJ1.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                if (spinnerJ1.getSelectedItemPosition() == spinnerJ2.getSelectedItemPosition()) {
                                    android.widget.Toast.makeText(requireContext(), "Ce joueur est déjà sélectionné par le Joueur 2 !", android.widget.Toast.LENGTH_SHORT).show();
                                    if (position == 0 && parent.getCount() > 1) {
                                        spinnerJ1.setSelection(1);
                                    } else {
                                        spinnerJ1.setSelection(0);
                                    }
                                }
                            }
                            @Override
                            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                        });

                        spinnerJ2.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                if (spinnerJ2.getSelectedItemPosition() == spinnerJ1.getSelectedItemPosition()) {
                                    android.widget.Toast.makeText(requireContext(), "Ce joueur est déjà sélectionné par le Joueur 1 !", android.widget.Toast.LENGTH_SHORT).show();
                                    if (position == 0 && parent.getCount() > 1) {
                                        spinnerJ2.setSelection(1);
                                    } else {
                                        spinnerJ2.setSelection(0);
                                    }
                                }
                            }
                            @Override
                            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                        });
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("SAE_ERR_BDD", "Le thread BDD du Fragment a échoué", e);
            }
        }).start();
    }

    public Spinner getSpinnerJ1() { return spinnerJ1; }
    public Spinner getSpinnerJ2() { return spinnerJ2; }

    @Override
    public void onResume() {
        super.onResume();
        refreshSpinners();
    }
}