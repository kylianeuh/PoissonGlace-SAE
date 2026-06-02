package com.sae402.poissonglobe;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class FinPartieDialogFragment extends DialogFragment {

    private final String vainqueur;
    private final String nomEquipeGau;
    private final String nomEquipeDro;
    private final String texteScores;

    public FinPartieDialogFragment(String vainqueur, String nomEquipeGau, String nomEquipeDro, String texteScores) {
        this.vainqueur = vainqueur;
        this.nomEquipeGau = nomEquipeGau;
        this.nomEquipeDro = nomEquipeDro;
        this.texteScores = texteScores;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fin_partie, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setCancelable(false);
        }

       android.widget.TextView txtVictoire = view.findViewById(R.id.txt_message_victoire);
        android.widget.TextView txtEquipeGau = view.findViewById(R.id.txt_popup_equipe_gauche);
        android.widget.TextView txtEquipeDro = view.findViewById(R.id.txt_popup_equipe_droite);
        android.widget.TextView txtScoresCentraux = view.findViewById(R.id.txt_popup_scores);
        View btnAccueil = view.findViewById(R.id.btn_dialog_accueil);

        if (txtVictoire != null) {
            txtVictoire.setText("Victoire de " + vainqueur + " !");
        }

        if (txtEquipeGau != null) {
            txtEquipeGau.setText(nomEquipeGau);
        }

        if (txtEquipeDro != null) {
            txtEquipeDro.setText(nomEquipeDro);
        }

        if (txtScoresCentraux != null) {
            txtScoresCentraux.setText(texteScores);
        }

        if (btnAccueil != null) {
            btnAccueil.setOnClickListener(v -> {
                dismiss();
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int largeurPixels = (int) (850 * getResources().getDisplayMetrics().density);
            getDialog().getWindow().setLayout(largeurPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}