package com.sae402.poissonglobe;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PauseDialogFragment extends DialogFragment {

    private final GameView gameView;

    public PauseDialogFragment(GameView gameView) {
        this.gameView = gameView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_pause, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setCancelable(false);
        }

        View btnQuitter = view.findViewById(R.id.btn_dialog_quitter);
        View btnReprendre = view.findViewById(R.id.btn_dialog_reprendre);

        btnReprendre.setOnClickListener(v -> {
            if (gameView != null) {
                gameView.reprendreJeu();
            }
            dismiss();
        });

        btnQuitter.setOnClickListener(v -> {
            if (gameView != null) {
                gameView.couperLesSons();
            }
            dismiss();

            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}