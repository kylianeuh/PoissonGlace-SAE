package com.sae402.poissonglobe;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddUserDialogFragment extends DialogFragment {

    public interface OnUserAddedListener {
        void onUserAdded();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_user, container, false);

        // CONFIGURATION CORRECTE DE LA FENÊTRE VIA LA MÉTHODE
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // CORRECTION ICI : Utilisation de la méthode setSoftInputMode
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        EditText editPseudo = view.findViewById(R.id.edit_pseudo);

        if (editPseudo != null) {
            editPseudo.setFocusable(true);
            editPseudo.setFocusableInTouchMode(true);
            editPseudo.requestFocus();
        }

        View btnAnnuler = view.findViewById(R.id.btn_dialog_annuler);
        View btnCreer = view.findViewById(R.id.btn_dialog_creer);

        btnAnnuler.setOnClickListener(v -> dismiss());

        btnCreer.setOnClickListener(v -> {
            if (editPseudo != null) {
                String pseudo = editPseudo.getText().toString().trim();

                if (!pseudo.isEmpty()) {
                    JoueurBD nouveauJoueur = new JoueurBD();
                    nouveauJoueur.nom = pseudo;

                    AppDatabase db = AppDatabase.getAppDatabase(requireContext());
                    db.getJeuDAO().insertJoueur(nouveauJoueur);

                    if (getParentFragment() instanceof OnUserAddedListener) {
                        ((OnUserAddedListener) getParentFragment()).onUserAdded();
                    }

                    dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int largeurPixels = (int) (750 * getResources().getDisplayMetrics().density);
            getDialog().getWindow().setLayout(largeurPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}