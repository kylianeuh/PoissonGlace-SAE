package com.sae402.poissonglobe;

import android.app.AlertDialog;
import android.app.Dialog;import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;import androidx.fragment.app.DialogFragment;

public class AddUserDialogFragment extends DialogFragment {

    public interface OnUserAddedListener {
        void onUserAdded();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_add_user, null);
        EditText inputPseudo = view.findViewById(R.id.edit_pseudo);

        builder.setView(view)
                .setTitle("Nouveau Joueur")
                .setPositiveButton("Créer", (dialog, id) -> {
                    String pseudo = inputPseudo.getText().toString().trim();
                    if (!pseudo.isEmpty()) {
                        AppDatabase db = AppDatabase.getAppDatabase(requireContext());
                        db.getJeuDAO().insertJoueur(new JoueurBD(pseudo));if (getParentFragment() instanceof OnUserAddedListener) {
                            ((OnUserAddedListener) getParentFragment()).onUserAdded();
                        }
                    }
                })
                .setNegativeButton("Annuler", null);

        return builder.create();
    }
}