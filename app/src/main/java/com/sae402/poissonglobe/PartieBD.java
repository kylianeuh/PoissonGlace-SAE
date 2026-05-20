package com.sae402.poissonglobe;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PartieBD{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long date_partie;
    public int nb_joueurs;

    public PartieBD() {}

    public PartieBD(long date_partie, int nb_joueurs) {
        this.date_partie = date_partie;
        this.nb_joueurs = nb_joueurs;
    }
}
