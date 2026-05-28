package com.sae402.poissonglobe;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddUserDialogFragment extends DialogFragment {

    public interface OnUserAddedListener {
        void onUserAdded();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_add_user, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText editPseudo = view.findViewById(R.id.edit_pseudo);
        View btnAnnuler = view.findViewById(R.id.btn_dialog_annuler);
        View btnCreer = view.findViewById(R.id.btn_dialog_creer);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnCreer.setOnClickListener(v -> {
            String pseudo = editPseudo.getText().toString().trim();

            if (!pseudo.isEmpty()) {
                JoueurBD nouveauJoueur = new JoueurBD();
                nouveauJoueur.nom = pseudo;

                AppDatabase db = AppDatabase.getAppDatabase(requireContext());
                db.getJeuDAO().insertJoueur(nouveauJoueur);

                if (getParentFragment() instanceof OnUserAddedListener) {
                    ((OnUserAddedListener) getParentFragment()).onUserAdded();
                }

                dialog.dismiss();
            }
        });

        return dialog;
    }
}