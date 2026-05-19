package com.sae402.poissonglobe;

import androidx.room.Entity;import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "JoueurPartieBD",
        primaryKeys = {"joueur_id", "partie_id"},foreignKeys = {
        @ForeignKey(
                entity = JoueurBD.class,
                parentColumns= "id",
                childColumns = "joueur_id",
                onDelete = ForeignKey.CASCADE),
        @ForeignKey(
                entity = PartieBD.class,
                parentColumns = "id",childColumns = "partie_id",
                onDelete = ForeignKey.CASCADE
        )},
        indices = {
                @Index(value = {"joueur_id"}),
                @Index(value = {"partie_id"})
        }
)
public class JoueurPartieBD {public int joueur_id;
    public int partie_id;
    public int score;
    public String resultat; // "Gagné", "Perdu", etc.

    // Constructeur vide requis par Roompublic JoueurPartieBD() {}

    // Constructeur pratique pour votre code
    public JoueurPartieBD(int joueur_id, int partie_id, int score, String resultat) {
        this.joueur_id = joueur_id;
        this.partie_id = partie_id;this.score = score;
        this.resultat = resultat;
    }
}