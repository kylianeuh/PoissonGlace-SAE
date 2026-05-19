package com.sae402.poissonglobe;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class JoueurBD {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String nom;
    public int scoreGlobal;

    public JoueurBD() {}

    public JoueurBD(String nom) {
        this.nom = nom;this.scoreGlobal = 0;
    }
}