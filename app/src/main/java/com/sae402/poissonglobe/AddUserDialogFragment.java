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

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                    nouveauJoueur.scoreGlobal = 0;

                    new Thread(() -> {
                        try {
                            android.content.Context ctx = requireContext().getApplicationContext();

                            java.io.File dbDir = new java.io.File(ctx.getApplicationInfo().dataDir + "/databases");
                            if (!dbDir.exists()) {
                                dbDir.mkdir();
                            }

                            AppDatabase db = AppDatabase.getAppDatabase(ctx);
                            db.getJeuDAO().insertJoueur(nouveauJoueur);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (getTargetFragment() instanceof OnUserAddedListener) {
                                        ((OnUserAddedListener) getTargetFragment()).onUserAdded();
                                    } else if (getParentFragment() instanceof OnUserAddedListener) {
                                        ((OnUserAddedListener) getParentFragment()).onUserAdded();
                                    }
                                    dismiss();
                                });
                            }
                        } catch (Exception e) {
                            android.util.Log.e("SAE_BDD", "Erreur d'insertion", e);
                        }
                    }).start();
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