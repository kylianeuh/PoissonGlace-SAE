package com.sae402.poissonglobe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

public class GameView extends View {

    private Paint pinceauLignes;
    private Paint pinceauButs;

    // Nos pinceaux pour les textes (style PoissonGlaceTextView)
    private Paint pinceauTexteJaune;
    private Paint pinceauTexteContour;

    // Les données de match (Le "Back" de l'arbitrage)
    public String nomJoueurGau = "Joueur 1";
    public String nomJoueurDro = "Joueur 2";
    public int scoreJoueurGau = 0;
    public int scoreJoueurDro = 0;

    // Repères du terrain
    public float ligneCentraleX, centreX, centreY;
    public float rayonCercleCentral, limiteSableGauche, limiteSableDroite;
    public RectF rectangleButGauche, rectangleButDroite;

    // --- NOS OBJETS DE JEU (BACK) ---
    public PoissonGlobe poissonGlobe;
    public Bulle bulleJoueur1; // Joueur Gauche
    public Bulle bulleJoueur2; // Joueur Droit

    // --- NOS ASSETS GRAPHIQUES ---
    private Bitmap imgPoissonGlobe;
    private Bitmap imgBulle;

    private boolean initialisationFaite = false;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialiserTerrain(context);
    }

    private void initialiserTerrain(Context context) {
        pinceauLignes = new Paint();
        pinceauLignes.setColor(Color.WHITE);
        pinceauLignes.setStyle(Paint.Style.STROKE);
        pinceauLignes.setStrokeWidth(12f);
        pinceauLignes.setAntiAlias(true);

        pinceauButs = new Paint();
        pinceauButs.setColor(Color.parseColor("#5C4033"));
        pinceauButs.setStyle(Paint.Style.FILL);

        // On récupère la police Cherry Bomb
        Typeface typoCherry = ResourcesCompat.getFont(context, R.font.cherry_bomb);

        // CONTOUR BLANC : On baisse l'épaisseur pour éviter qu'il n'étouffe le centre
        pinceauTexteContour = new Paint();
        pinceauTexteContour.setTypeface(typoCherry);
        pinceauTexteContour.setColor(Color.WHITE);
        pinceauTexteContour.setTextSize(65f); // Légèrement plus grand pour englober
        pinceauTexteContour.setStyle(Paint.Style.STROKE);
        pinceauTexteContour.setStrokeWidth(12f); // Épaisseur réduite (12 au lieu de 20) pour libérer le jaune
        pinceauTexteContour.setStrokeJoin(Paint.Join.ROUND);
        pinceauTexteContour.setAntiAlias(true);
        pinceauTexteContour.setTextAlign(Paint.Align.CENTER);

// INTÉRIEUR JAUNE : On le passe en FILL pur et on augmente sa taille !
        pinceauTexteJaune = new Paint();
        pinceauTexteJaune.setTypeface(typoCherry);
        pinceauTexteJaune.setColor(Color.parseColor("#FFCC00"));
        pinceauTexteJaune.setTextSize(65f); // Même taille de base que le contour
        pinceauTexteJaune.setStyle(Paint.Style.FILL); // FILL pur pour éviter les conflits de tracés
        pinceauTexteJaune.setAntiAlias(true);
        pinceauTexteJaune.setTextAlign(Paint.Align.CENTER);

        rectangleButGauche = new RectF();
        rectangleButDroite = new RectF();

        // CHARGEMENT DE TES IMAGES
        imgPoissonGlobe = BitmapFactory.decodeResource(context.getResources(), R.drawable.fish_brown);
        imgBulle = BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_c);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int largeur = getWidth();
        int hauteur = getHeight();

        // =======================================================================
        // 1. CALCULS DES REPERES DU TERRAIN (Si pas encore fait)
        // =======================================================================
        if (!initialisationFaite) {
            centreX = largeur / 2f;
            centreY = hauteur / 2f;
            ligneCentraleX = centreX;
            limiteSableGauche = largeur * 0.10f;
            limiteSableDroite = largeur - (largeur * 0.10f);

            // Création des objets au centre de leurs zones respectives
            float rayonPoisson = hauteur * 0.08f;
            float rayonBulle = hauteur * 0.13f;

            poissonGlobe = new PoissonGlobe(centreX, centreY, rayonPoisson);
            bulleJoueur1 = new Bulle(largeur * 0.20f, centreY, rayonBulle); // Positionné dans son camp gauche
            bulleJoueur2 = new Bulle(largeur * 0.80f, centreY, rayonBulle); // Positionné dans son camp droit

            initialisationFaite = true;
        }

        float hauteurBut = hauteur / 3f;
        float epaisseurBut = 40f;

        // =======================================================================
        // 2. DESSIN DES BUTS MARRONS
        // =======================================================================
        rectangleButGauche.set(0, centreY - (hauteurBut / 2f), epaisseurBut, centreY + (hauteurBut / 2f));
        rectangleButDroite.set(largeur - epaisseurBut, centreY - (hauteurBut / 2f), largeur, centreY + (hauteurBut / 2f));
        canvas.drawRect(rectangleButGauche, pinceauButs);
        canvas.drawRect(rectangleButDroite, pinceauButs);

        // =======================================================================
        // 3. DESSIN DES LIGNES BLANCHES
        // =======================================================================
        canvas.drawLine(ligneCentraleX, 0, ligneCentraleX, hauteur, pinceauLignes);
        rayonCercleCentral = hauteur * 0.09f;
        canvas.drawCircle(centreX, centreY, rayonCercleCentral, pinceauLignes);

        float rayonZoneBut = hauteur * 0.22f;
        RectF zoneButGauche = new RectF(-rayonZoneBut, centreY - rayonZoneBut, rayonZoneBut, centreY + rayonZoneBut);
        canvas.drawArc(zoneButGauche, 270, 180, false, pinceauLignes);

        RectF zoneButDroit = new RectF(largeur - rayonZoneBut, centreY - rayonZoneBut, largeur + rayonZoneBut, centreY + rayonZoneBut);
        canvas.drawArc(zoneButDroit, 90, 180, false, pinceauLignes);

        // =======================================================================
        // 4. DESSIN DES ASSETS (Poisson-Globe et Bulles)
        // =======================================================================

        // Dessin du Palet (Poisson Globe) centré sur ses coordonnées (x,y)
        if (imgPoissonGlobe != null) {
            RectF positionPoisson = new RectF(
                    poissonGlobe.x - poissonGlobe.rayon,
                    poissonGlobe.y - poissonGlobe.rayon,
                    poissonGlobe.x + poissonGlobe.rayon,
                    poissonGlobe.y + poissonGlobe.rayon
            );
            canvas.drawBitmap(imgPoissonGlobe, null, positionPoisson, null);
        }

        // Dessin des Bulles
        if (imgBulle != null) {
            // Dessin de la Bulle du Joueur 1 (Gauche)
            RectF positionBulleJ1 = new RectF(
                    bulleJoueur1.x - bulleJoueur1.rayon,
                    bulleJoueur1.y - bulleJoueur1.rayon,
                    bulleJoueur1.x + bulleJoueur1.rayon,
                    bulleJoueur1.y + bulleJoueur1.rayon
            );
            canvas.drawBitmap(imgBulle, null, positionBulleJ1, null);

            // Dessin de la Bulle du Joueur 2 (Droite)
            RectF positionBulleJ2 = new RectF(
                    bulleJoueur2.x - bulleJoueur2.rayon,
                    bulleJoueur2.y - bulleJoueur2.rayon,
                    bulleJoueur2.x + bulleJoueur2.rayon,
                    bulleJoueur2.y + bulleJoueur2.rayon
            );
            canvas.drawBitmap(imgBulle, null, positionBulleJ2, null);
        }

        // =======================================================================
        // 5. TABLEAU D'AFFICHAGE (Noms aux extrémités, Scores au centre)
        // =======================================================================
        float positionYTextes = 80f;
        float margeExtremite = largeur * 0.10f;
        float ecartScore = 120f;

        // --- JOUEUR GAUCHE ---
        canvas.drawText(nomJoueurGau, margeExtremite, positionYTextes, pinceauTexteContour);
        canvas.drawText(nomJoueurGau, margeExtremite, positionYTextes, pinceauTexteJaune);

        // --- JOUEUR DROIT ---
        canvas.drawText(nomJoueurDro, largeur - margeExtremite, positionYTextes, pinceauTexteContour);
        canvas.drawText(nomJoueurDro, largeur - margeExtremite, positionYTextes, pinceauTexteJaune);

        // --- SCORES (Applique le même effet Cherry Bomb Jaune et Blanc) ---
        canvas.drawText(String.valueOf(scoreJoueurGau), centreX - ecartScore, positionYTextes + 15f, pinceauTexteContour);
        canvas.drawText(String.valueOf(scoreJoueurGau), centreX - ecartScore, positionYTextes + 15f, pinceauTexteJaune);

        canvas.drawText(String.valueOf(scoreJoueurDro), centreX + ecartScore, positionYTextes + 15f, pinceauTexteContour);
        canvas.drawText(String.valueOf(scoreJoueurDro), centreX + ecartScore, positionYTextes + 15f, pinceauTexteJaune);
    }
}