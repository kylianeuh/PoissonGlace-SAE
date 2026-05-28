package com.sae402.poissonglobe;

public class Bulle {
    public float x, y;
    public float rayon;

    public float vX = 0f;
    public float vY = 0f;
    private float derniereX, derniereY;
    private boolean premierMouvement = true;

    public Bulle(float x, float y, float rayon) {
        this.x = x;
        this.y = y;
        this.rayon = rayon;
        this.derniereX = x;
        this.derniereY = y;
    }

    public boolean estTouche(float touchX, float touchY) {
        float hitboxX = this.x + GameView.CONFIG_HITBOX_BULLE_DECALAGE_X;
        float hitboxY = this.y + GameView.CONFIG_HITBOX_BULLE_DECALAGE_Y;

        float dx = touchX - hitboxX;
        float dy = touchY - hitboxY;
        return (dx * dx + dy * dy) <= (this.rayon * this.rayon);
    }

    public void calculerVitesse() {
        if (premierMouvement) {
            derniereX = x;
            derniereY = y;
            premierMouvement = false;
            return;
        }
        vX = x - derniereX;
        vY = y - derniereY;
        derniereX = x;
        derniereY = y;
    }

    public void reinitialiserVitesse() {
        derniereX = x;
        derniereY = y;
        vX = 0f;
        vY = 0f;
        premierMouvement = true;
    }

    public void contraindreDansLimites(float largeurTerrain, float hauteurTerrain, float ligneCentraleX, boolean estJoueurGauche) {
        if (y - rayon < 0) y = rayon;
        if (y + rayon > hauteurTerrain) y = hauteurTerrain - rayon;

        if (estJoueurGauche) {
            if (x - rayon < 0) x = rayon;
            if (x + rayon > ligneCentraleX) x = ligneCentraleX - rayon;
        } else {
            if (x - rayon < ligneCentraleX) x = ligneCentraleX + rayon;
            if (x + rayon > largeurTerrain) x = largeurTerrain - rayon;
        }
    }
}