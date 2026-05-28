package com.sae402.poissonglobe;

public class PoissonGlobe {
    public float x, y;
    public float vitesseX, vitesseY;
    public float rayon;

    public PoissonGlobe(float x, float y, float rayon) {
        this.x = x;
        this.y = y;
        this.rayon = rayon;
        this.vitesseX = 0f;
        this.vitesseY = 0f;
    }
}
