package com.sae402.poissonglobe;

public class Joueur {
    private String pseudo;
    private int score;

    public Joueur(String pseudo, int score) {
        this.pseudo = pseudo;
        this.score = score;
    }

    public String getPseudo() { return pseudo; }
    public int getScore() { return score; }
}