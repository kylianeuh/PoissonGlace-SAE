package com.sae402.poissonglobe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class GameView extends View {

    // Nos outils de dessin
    private Paint pinceauLignes;
    private Paint pinceauButs;

    // Nos variables de coordonnées (Accessibles pour la future interactivité)
    public float ligneCentraleX;
    public float centreX, centreY;
    public float rayonCercleCentral;
    public float limiteSableGauche;
    public float limiteSableDroite;

    // Nos rectangles de buts (Pour détecter les futurs scores)
    public RectF rectangleButGauche;
    public RectF rectangleButDroite;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialiserTerrain();
    }

    private void initialiserTerrain() {
        // 1. Configuration du pinceau blanc pour les lignes du terrain
        pinceauLignes = new Paint();
        pinceauLignes.setColor(Color.WHITE);
        pinceauLignes.setStyle(Paint.Style.STROKE); // STROKE = contours uniquement
        pinceauLignes.setStrokeWidth(12f);           // Épaisseur de la ligne
        pinceauLignes.setAntiAlias(true);           // Lissage des bords pour éviter les pixels carrés

        // 2. Configuration du pinceau marron pour l'intérieur des buts
        pinceauButs = new Paint();
        pinceauButs.setColor(Color.parseColor("#5C4033")); // Marron terre
        pinceauButs.setStyle(Paint.Style.FILL);

        // Initialisation à vide des rectangles de buts
        rectangleButGauche = new RectF();
        rectangleButDroite = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Étape A : Récupérer la taille réelle de l'écran en pixels
        int largeur = getWidth();
        int hauteur = getHeight();

        // Repères pour la physique du sable
        limiteSableGauche = largeur * 0.10f;
        limiteSableDroite = largeur - (largeur * 0.10f);

        // Le centre mathématique de la table
        centreX = largeur / 2f;
        centreY = hauteur / 2f;
        ligneCentraleX = centreX;

        // Dimensions proportionnelles pour les buts
        float hauteurBut = hauteur / 3f;  // Le but prend 1/3 de la hauteur de l'écran
        float epaisseurBut = 40f;          // Largeur du rectangle marron

        // =======================================================================
        // 1. DESSIN DES RECTANGLES DE BUTS MARRONS (Collés aux bords physiques)
        // =======================================================================
        // But Gauche : Collé à l'extrémité gauche de l'écran (0)
        rectangleButGauche.set(0, centreY - (hauteurBut / 2f), epaisseurBut, centreY + (hauteurBut / 2f));

        // But Droit : Collé à l'extrémité droite de l'écran (largeur)
        rectangleButDroite.set(largeur - epaisseurBut, centreY - (hauteurBut / 2f), largeur, centreY + (hauteurBut / 2f));

        canvas.drawRect(rectangleButGauche, pinceauButs);
        canvas.drawRect(rectangleButDroite, pinceauButs); // (Attention à l'orthographe du pinceau si tu as corrigé "pinceauButs")

        // =======================================================================
        // 2. TRACÉ DES LIGNES DU TERRAIN
        // =======================================================================

        // Ligne médiane
        canvas.drawLine(ligneCentraleX, 0, ligneCentraleX, hauteur, pinceauLignes);

        // Cercle Central REDUIT (12% de la hauteur au lieu de 18%)
        rayonCercleCentral = hauteur * 0.09f;
        canvas.drawCircle(centreX, centreY, rayonCercleCentral, pinceauLignes);

        // Zone de protection devant le but Gauche (Demi-cercle collé au bord gauche)
        float rayonZoneBut = hauteur * 0.22f;
        RectF zoneButGauche = new RectF(-rayonZoneBut, centreY - rayonZoneBut, rayonZoneBut, centreY + rayonZoneBut);
        canvas.drawArc(zoneButGauche, 270, 180, false, pinceauLignes);

        // Zone de protection devant le but Droit (Demi-cercle collé au bord droit)
        RectF zoneButDroit = new RectF(largeur - rayonZoneBut, centreY - rayonZoneBut, largeur + rayonZoneBut, centreY + rayonZoneBut);
        canvas.drawArc(zoneButDroit, 90, 180, false, pinceauLignes);
    }
}