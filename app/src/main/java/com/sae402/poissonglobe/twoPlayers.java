package com.sae402.poissonglobe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // <--- ILMANQUAIT CET IMPORT

import androidx.fragment.app.Fragment;

public class twoPlayers extends Fragment {@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Correction : "ViewGroupcontainer" est devenu "ViewGroup container"

    // On lie le fragment à son fichier XML
    View view = inflater.inflate(R.layout.fragment_two_players, container, false);

    // Correction : "return view" doit être sur sa propre ligne
    return view;
}
}