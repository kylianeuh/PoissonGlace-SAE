package com.sae402.poissonglobe;

public class PoissonGlobe {
    public float x, y;          // Position du centre du poisson-globe
    public float vitesseX, vitesseY; // Vitesse de déplacement sur les axes
    public float rayon;         // Taille de sa zone de collision

    public PoissonGlobe(float x, float y, float rayon) {
        this.x = x;
        this.y = y;
        this.rayon = rayon;
        this.vitesseX = 0f; // Immobile au départ
        this.vitesseY = 0f;
    }
}
