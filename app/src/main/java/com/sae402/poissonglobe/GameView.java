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
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

public class GameView extends View {

    // =======================================================================
    // --- CONFIGURATION GLOBALE ET CALIBRATION DE LA PHYSIQUE (STATIC) ---
    // =======================================================================

    // --- LE POISSON GLOBE ---
    public static float FACTEUR_TAILLE_POISSON = 0.08f;
    public static float CONFIG_HITBOX_POISSON_RAYON = 0.70f;
    public static float CONFIG_HITBOX_POISSON_DECALAGE_X = 10f;
    public static float CONFIG_HITBOX_POISSON_DECALAGE_Y = 0f;

    // --- LES JOUEURS (BULLES) ---
    public static float FACTEUR_TAILLE_BULLE = 0.05f;
    public static float CONFIG_HITBOX_BULLE_RAYON = 1.0f;
    public static float CONFIG_HITBOX_BULLE_DECALAGE_X = 0f;
    public static float CONFIG_HITBOX_BULLE_DECALAGE_Y = 0f;

    // --- CONSTANTES DE JEU ---
    public static float FRICTION_TERRAIN = 0.98f;
    public static float CONFIG_RESTITUTION = 0.5f;
    public static float CONFIG_MULT_FORCE_DOIGT = 1.2f;
    public static float CONFIG_VITESSE_MIN_DOIGT = 0.5f;
    public static float CONFIG_VITESSE_MAX_POISSON = 45f;
    public static int PHYSIQUE_SUB_STEPS = 3;

    private Paint pinceauLignes;
    private Paint pinceauButs;
    private Paint pinceauHitboxDebug;
    private Paint pinceauBullesJoueurs;
    private Paint pinceauBoutonPause;
    private Paint pinceauBoutonPauseBordure;
    private Paint pinceauSymbolePause;
    private Paint pinceauSymbolePauseBordure;

    // Pinceaux pour les textes (style PoissonGlaceTextView)
    private Paint pinceauTexteJaune;
    private Paint pinceauTexteContour;

    // Les données de match (Le "Back" de l'arbitrage)
    public String nomJoueurGau = "Joueur 1";
    public String nomJoueurDro = "Joueur 2";
    public int scoreJoueurGau = 0;
    public int scoreJoueurDro = 0;

    public float ligneCentraleX, centreX, centreY;
    public float rayonCercleCentral, limiteSableGauche, limiteSableDroite;

    // Objets graphiques du terrain
    private RectF rectangleButGauche;
    private RectF rectangleButDroite;
    private RectF zoneButGauche;
    private RectF zoneButDroit;

    // Éléments du bouton pause
    private float pauseBtnX, pauseBtnY, pauseBtnRadius;
    private RectF pauseBarLeft;
    private RectF pauseBarRight;

    // --- OBJETS DE JEU (BACK) ---
    public PoissonGlobe poissonGlobe;
    public Bulle bulleJoueur1; // Joueur Gauche
    public Bulle bulleJoueur2; // Joueur Droit

    // --- ASSETS GRAPHIQUES ---
    private Bitmap imgPoissonGlobe;

    private float positionYTextes, margeExtremite, ecartScore, correctionYScore;
    private boolean initialisationFaite = false;

    private android.os.Handler jeuHandler = new android.os.Handler();
    private Runnable boucleJeu;
    private final int FPS = 60;

    // Suivre l'ID du pointeur (doigt) qui contrôle chaque bulle (Multitouch)
    private int idDoigtJ1 = -1;
    private int idDoigtJ2 = -1;

    // pour terminer la partie
    private boolean partieTerminee = false;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialiserTerrain(context);
    }

    public interface OnGameOverListener {
        void onGameOver(String pseudoVainqueur);
    }
    private OnGameOverListener gameOverListener;

    public void setOnGameOverListener(OnGameOverListener listener) {
        this.gameOverListener = listener;
    }

    private void initialiserTerrain(Context context) {
        pinceauLignes = createPaint(Color.WHITE, Paint.Style.STROKE, 12f);
        pinceauButs = createPaint(Color.parseColor("#5C4033"), Paint.Style.FILL, 0f);

        // police Cherry Bomb
        Typeface typoCherry = ResourcesCompat.getFont(context, R.font.cherry_bomb);

        pinceauTexteContour = new Paint();
        pinceauTexteContour.setTypeface(typoCherry);
        pinceauTexteContour.setColor(Color.WHITE);
        pinceauTexteContour.setTextSize(65f);
        pinceauTexteContour.setStyle(Paint.Style.STROKE);
        pinceauTexteContour.setStrokeWidth(12f);
        pinceauTexteContour.setStrokeJoin(Paint.Join.ROUND);
        pinceauTexteContour.setTextAlign(Paint.Align.CENTER);
        pinceauTexteContour.setAntiAlias(true);

        pinceauTexteJaune = new Paint();
        pinceauTexteJaune.setTypeface(typoCherry);
        pinceauTexteJaune.setColor(Color.parseColor("#FFCC00"));
        pinceauTexteJaune.setTextSize(65f);
        pinceauTexteJaune.setStyle(Paint.Style.FILL);
        pinceauTexteJaune.setAntiAlias(true);
        pinceauTexteJaune.setTextAlign(Paint.Align.CENTER);

        pinceauBoutonPause = createPaint(Color.parseColor("#6622A7F0"), Paint.Style.FILL, 0f);
        pinceauBoutonPauseBordure = createPaint(Color.WHITE, Paint.Style.STROKE, 8f);
        pinceauSymbolePause = createPaint(Color.parseColor("#FFCC00"), Paint.Style.FILL, 0f);

        pinceauSymbolePauseBordure = createPaint(Color.WHITE, Paint.Style.STROKE, 12f);
        pinceauSymbolePauseBordure.setStrokeJoin(Paint.Join.ROUND);

        imgPoissonGlobe = BitmapFactory.decodeResource(context.getResources(), R.drawable.fish_brown);

        rectangleButGauche = new RectF();
        rectangleButDroite = new RectF();
        zoneButGauche = new RectF();
        zoneButDroit = new RectF();
        pauseBarLeft = new RectF();
        pauseBarRight = new RectF();

        boucleJeu = new Runnable() {
            @Override
            public void run() {
                if (initialisationFaite) {
                    gererPhysiqueEtArbitrage();
                }
                invalidate(); // Force le redessin (onDraw)
                jeuHandler.postDelayed(this, 1000 / FPS);
            }
        };
        jeuHandler.post(boucleJeu);

        pinceauBullesJoueurs = new Paint();
        pinceauBullesJoueurs.setColor(Color.parseColor("#40E0D0"));
        pinceauBullesJoueurs.setStyle(Paint.Style.STROKE);
        pinceauBullesJoueurs.setStrokeWidth(8f);
        pinceauBullesJoueurs.setAntiAlias(true);

        pinceauHitboxDebug = new Paint();
        pinceauHitboxDebug.setColor(Color.RED);
        pinceauHitboxDebug.setStyle(Paint.Style.STROKE);
        pinceauHitboxDebug.setStrokeWidth(5f);
        pinceauHitboxDebug.setAntiAlias(true);
    }

    private Paint createPaint(int color, Paint.Style style, float strokeWidth) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(style);
        if (strokeWidth > 0) p.setStrokeWidth(strokeWidth);
        return p;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centreX = w / 2f;
        centreY = h / 2f;
        ligneCentraleX = centreX;
        limiteSableGauche = w * 0.10f;
        limiteSableDroite = w - limiteSableGauche;

        poissonGlobe = new PoissonGlobe(centreX, centreY, h * 0.08f);
        bulleJoueur1 = new Bulle(w * 0.20f, centreY, h * 0.13f);
        bulleJoueur2 = new Bulle(w * 0.80f, centreY, h * 0.13f);

        float hauteurBut = h / 3f;
        float epaisseurBut = 40f;
        float rayonZoneBut = h * 0.22f;

        rectangleButGauche.set(0, centreY - (hauteurBut / 2f), epaisseurBut, centreY + (hauteurBut / 2f));
        rectangleButDroite.set(w - epaisseurBut, centreY - (hauteurBut / 2f), w, centreY + (hauteurBut / 2f));

        rayonCercleCentral = h * 0.09f;
        zoneButGauche.set(-rayonZoneBut, centreY - rayonZoneBut, rayonZoneBut, centreY + rayonZoneBut);
        zoneButDroit.set(w - rayonZoneBut, centreY - rayonZoneBut, w + rayonZoneBut, centreY + rayonZoneBut);

        pauseBtnRadius = h * 0.04f;
        pauseBtnX = centreX;
        pauseBtnY = h - pauseBtnRadius - 40f;

        float barWidth = pauseBtnRadius * 0.25f, barHeight = pauseBtnRadius * 0.8f, barSpacing = pauseBtnRadius * 0.2f;
        pauseBarLeft.set(pauseBtnX - barSpacing / 2f - barWidth, pauseBtnY - barHeight / 2f, pauseBtnX - barSpacing / 2f, pauseBtnY + barHeight / 2f);
        pauseBarRight.set(pauseBtnX + barSpacing / 2f, pauseBtnY - barHeight / 2f, pauseBtnX + barSpacing / 2f + barWidth, pauseBtnY + barHeight / 2f);

        positionYTextes = 80f;
        margeExtremite = w * 0.10f;
        ecartScore = 120f;
        correctionYScore = positionYTextes + 15f;

        initialisationFaite = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int hauteur = getHeight();

        canvas.drawRect(rectangleButGauche, pinceauButs);
        canvas.drawRect(rectangleButDroite, pinceauButs);
        canvas.drawLine(ligneCentraleX, 0, ligneCentraleX, hauteur, pinceauLignes);
        canvas.drawCircle(centreX, centreY, rayonCercleCentral, pinceauLignes);
        canvas.drawArc(zoneButGauche, 270, 180, false, pinceauLignes);
        canvas.drawArc(zoneButDroit, 90, 180, false, pinceauLignes);

        // =======================================================================
        // 4. DESSIN DES ASSETS (Calculs basés sur les variables Static)
        // =======================================================================

        // 1. CALCUL DE TOUS LES RAYONS DE BASE VISUELS
        float rayonVisuelPoisson = hauteur * FACTEUR_TAILLE_POISSON;
        float rayonVisuelBulle = hauteur * FACTEUR_TAILLE_BULLE;

        // 2. CORRECTION CRITIQUE : MISE À JOUR DES RAYONS DES OBJETS AVANT TOUT DESSIN
        poissonGlobe.rayon = rayonVisuelPoisson * CONFIG_HITBOX_POISSON_RAYON;
        bulleJoueur1.rayon = rayonVisuelBulle * CONFIG_HITBOX_BULLE_RAYON;
        bulleJoueur2.rayon = rayonVisuelBulle * CONFIG_HITBOX_BULLE_RAYON;

        // --- A. DESSIN DU POISSON (IMAGE) ---
        if (imgPoissonGlobe != null) {
            RectF positionPoisson = new RectF(
                    poissonGlobe.x - rayonVisuelPoisson,
                    poissonGlobe.y - rayonVisuelPoisson,
                    poissonGlobe.x + rayonVisuelPoisson,
                    poissonGlobe.y + rayonVisuelPoisson
            );
            canvas.drawBitmap(imgPoissonGlobe, null, positionPoisson, null);
        }

        // --- B. DESSIN DES JOUEURS (100% VECTORIEL) ---
        Paint pinceauCorpsBulle = new Paint();
        pinceauCorpsBulle.setColor(Color.parseColor("#A0E0FF"));
        pinceauCorpsBulle.setStyle(Paint.Style.FILL);
        pinceauCorpsBulle.setAlpha(80);
        pinceauCorpsBulle.setAntiAlias(true);

        Paint pinceauRefletBulle = new Paint();
        pinceauRefletBulle.setColor(Color.WHITE);
        pinceauRefletBulle.setStyle(Paint.Style.FILL);
        pinceauRefletBulle.setAlpha(180);
        pinceauRefletBulle.setAntiAlias(true);

        // ---- JOUEUR 1 (GAUCHE) ----
        canvas.drawCircle(bulleJoueur1.x, bulleJoueur1.y, rayonVisuelBulle, pinceauCorpsBulle);
        canvas.drawCircle(bulleJoueur1.x, bulleJoueur1.y, rayonVisuelBulle, pinceauBullesJoueurs);

        float decalageReflet1 = rayonVisuelBulle * 0.35f;
        float rayonReflet1 = rayonVisuelBulle * 0.15f;
        canvas.drawCircle(bulleJoueur1.x - decalageReflet1, bulleJoueur1.y - decalageReflet1, rayonReflet1, pinceauRefletBulle);

        // ---- JOUEUR 2 (DROIT) ----
        canvas.drawCircle(bulleJoueur2.x, bulleJoueur2.y, rayonVisuelBulle, pinceauCorpsBulle);
        canvas.drawCircle(bulleJoueur2.x, bulleJoueur2.y, rayonVisuelBulle, pinceauBullesJoueurs);
        canvas.drawCircle(bulleJoueur2.x - decalageReflet1, bulleJoueur2.y - decalageReflet1, rayonReflet1, pinceauRefletBulle);

        // --- C. DESSIN PAUSE & TEXTES ---
        canvas.drawCircle(pauseBtnX, pauseBtnY, pauseBtnRadius, pinceauBoutonPause);
        canvas.drawCircle(pauseBtnX, pauseBtnY, pauseBtnRadius, pinceauBoutonPauseBordure);
        canvas.drawRect(pauseBarLeft, pinceauSymbolePauseBordure);
        canvas.drawRect(pauseBarRight, pinceauSymbolePauseBordure);
        canvas.drawRect(pauseBarLeft, pinceauSymbolePause);
        canvas.drawRect(pauseBarRight, pinceauSymbolePause);

        drawTextWithContour(canvas, nomJoueurGau, margeExtremite, positionYTextes);
        drawTextWithContour(canvas, nomJoueurDro, getWidth() - margeExtremite, positionYTextes);
        drawTextWithContour(canvas, String.valueOf(scoreJoueurGau), centreX - ecartScore, correctionYScore);
        drawTextWithContour(canvas, String.valueOf(scoreJoueurDro), centreX + ecartScore, correctionYScore);

        if (!partieTerminee) {
            if (scoreJoueurGau >= 6 || scoreJoueurDro >= 6) {
                partieTerminee = true; // On bloque le jeu pour arrêter les futurs mouvements

                String vainqueur = (scoreJoueurGau >= 6) ? nomJoueurGau : nomJoueurDro;

                // On prévient l'activité en lui donnant le nom du gagnant
                if (gameOverListener != null) {
                    gameOverListener.onGameOver(vainqueur);
                }
            }
        }
    }

    private void drawTextWithContour(Canvas canvas, String text, float x, float y) {
        canvas.drawText(text, x, y, pinceauTexteContour);
        canvas.drawText(text, x, y, pinceauTexteJaune);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int indexPointeur = event.getActionIndex();
        int idPointeur = event.getPointerId(indexPointeur);

        float touchX = event.getX(indexPointeur);
        float touchY = event.getY(indexPointeur);

        // Interception du bouton Pause en priorité (uniquement sur le premier clic)
        if (action == MotionEvent.ACTION_DOWN) {
            float dx = touchX - pauseBtnX;
            float dy = touchY - pauseBtnY;
            if ((dx * dx + dy * dy) <= (pauseBtnRadius * pauseBtnRadius)) {
                declencherPause();
                return true;
            }
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Vérification J1 (Camp Gauche)
                if (bulleJoueur1.estTouche(touchX, touchY) && idDoigtJ1 == -1) {
                    idDoigtJ1 = idPointeur;
                }
                // Vérification J2 (Camp Droit)
                else if (bulleJoueur2.estTouche(touchX, touchY) && idDoigtJ2 == -1) {
                    idDoigtJ2 = idPointeur;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pId = event.getPointerId(i);
                    float currentX = event.getX(i);
                    float currentY = event.getY(i);

                    if (pId == idDoigtJ1) {
                        bulleJoueur1.x = currentX;
                        bulleJoueur1.y = currentY;
                        bulleJoueur1.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, true);
                    } else if (pId == idDoigtJ2) {
                        bulleJoueur2.x = currentX;
                        bulleJoueur2.y = currentY;
                        bulleJoueur2.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, false);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (idPointeur == idDoigtJ1) {
                    idDoigtJ1 = -1;
                    bulleJoueur1.reinitialiserVitesse();
                }
                if (idPointeur == idDoigtJ2) {
                    idDoigtJ2 = -1;
                    bulleJoueur2.reinitialiserVitesse();
                }
                break;
        }
        return true;
    }

    private void declencherPause() {
        // Ton code pour ouvrir l'overlay ou l'activité de pause
    }

    private void gererPhysiqueEtArbitrage() {
        int largeur = getWidth();
        int hauteur = getHeight();

        bulleJoueur1.calculerVitesse();
        bulleJoueur2.calculerVitesse();

        for (int step = 0; step < PHYSIQUE_SUB_STEPS; step++) {
            poissonGlobe.x += (poissonGlobe.vitesseX / PHYSIQUE_SUB_STEPS);
            poissonGlobe.y += (poissonGlobe.vitesseY / PHYSIQUE_SUB_STEPS);

            gererMursEtButsEtape(largeur, hauteur);

            calculerCollisionBullePoisson(bulleJoueur1);
            calculerCollisionBullePoisson(bulleJoueur2);
        }

        poissonGlobe.vitesseX *= FRICTION_TERRAIN;
        poissonGlobe.vitesseY *= FRICTION_TERRAIN;

        float vitesseActuelle = (float) Math.sqrt(poissonGlobe.vitesseX * poissonGlobe.vitesseX + poissonGlobe.vitesseY * poissonGlobe.vitesseY);
        if (vitesseActuelle > CONFIG_VITESSE_MAX_POISSON) {
            poissonGlobe.vitesseX = (poissonGlobe.vitesseX / vitesseActuelle) * CONFIG_VITESSE_MAX_POISSON;
            poissonGlobe.vitesseY = (poissonGlobe.vitesseY / vitesseActuelle) * CONFIG_VITESSE_MAX_POISSON;
        }
    }

    private void calculerCollisionBullePoisson(Bulle bulle) {
        float poissonHitboxX = poissonGlobe.x + CONFIG_HITBOX_POISSON_DECALAGE_X;
        float poissonHitboxY = poissonGlobe.y + CONFIG_HITBOX_POISSON_DECALAGE_Y;
        float bulleHitboxX = bulle.x + CONFIG_HITBOX_BULLE_DECALAGE_X;
        float bulleHitboxY = bulle.y + CONFIG_HITBOX_BULLE_DECALAGE_Y;

        float dx = poissonHitboxX - bulleHitboxX;
        float dy = poissonHitboxY - bulleHitboxY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float distanceMin = poissonGlobe.rayon + bulle.rayon;

        if (distance < distanceMin && distance > 0) {
            float normalX = dx / distance;
            float normalY = dy / distance;

            float chevauchement = distanceMin - distance;
            poissonGlobe.x += normalX * chevauchement;
            poissonGlobe.y += normalY * chevauchement;

            float vitesseRelativeX = poissonGlobe.vitesseX - bulle.vX;
            float vitesseRelativeY = poissonGlobe.vitesseY - bulle.vY;

            float vitesseSurNormale = vitesseRelativeX * normalX + vitesseRelativeY * normalY;

            if (vitesseSurNormale < 0) {
                float impulsion = -(1.0f + CONFIG_RESTITUTION) * vitesseSurNormale;
                poissonGlobe.vitesseX += normalX * impulsion;
                poissonGlobe.vitesseY += normalY * impulsion;
            }

            float vitesseDoigtMgn = (float) Math.sqrt(bulle.vX * bulle.vX + bulle.vY * bulle.vY);

            if (vitesseDoigtMgn > CONFIG_VITESSE_MIN_DOIGT) {
                float forceAjustee = vitesseDoigtMgn * CONFIG_MULT_FORCE_DOIGT;
                poissonGlobe.vitesseX = normalX * forceAjustee;
                poissonGlobe.vitesseY = normalY * forceAjustee;
            }
        }
    }

    private void remiseEnJeu(boolean auJoueur2) {
        int largeur = getWidth();

        poissonGlobe.y = getHeight() / 2f;
        poissonGlobe.vitesseX = 0;
        poissonGlobe.vitesseY = 0;

        if (auJoueur2) {
            poissonGlobe.x = largeur * 0.70f;
        } else {
            poissonGlobe.x = largeur * 0.30f;
        }

        bulleJoueur1.x = largeur * 0.20f;
        bulleJoueur1.y = getHeight() / 2f;
        bulleJoueur2.x = largeur * 0.80f;
        bulleJoueur2.y = getHeight() / 2f;

        idDoigtJ1 = -1;
        idDoigtJ2 = -1;

        bulleJoueur1.reinitialiserVitesse();
        bulleJoueur2.reinitialiserVitesse();
    }

    private void gererMursEtButsEtape(int largeurTerrain, int hauteurTerrain) {
        if (poissonGlobe.y - poissonGlobe.rayon < 0) {
            poissonGlobe.y = poissonGlobe.rayon;
            poissonGlobe.vitesseY = -poissonGlobe.vitesseY;
        } else if (poissonGlobe.y + poissonGlobe.rayon > hauteurTerrain) {
            poissonGlobe.y = hauteurTerrain - poissonGlobe.rayon;
            poissonGlobe.vitesseY = -poissonGlobe.vitesseY;
        }

        float hauteurBut = hauteurTerrain / 3f;
        float hautBut = (hauteurTerrain / 2f) - (hauteurBut / 2f);
        float basBut = (hauteurTerrain / 2f) + (hauteurBut / 2f);

        if (poissonGlobe.y >= hautBut && poissonGlobe.y <= basBut) {
            if (poissonGlobe.x - poissonGlobe.rayon <= 0) {
                scoreJoueurDro++;
                remiseEnJeu(false);
            } else if (poissonGlobe.x + poissonGlobe.rayon >= largeurTerrain) {
                scoreJoueurGau++;
                remiseEnJeu(true);
            }
        } else {
            if (poissonGlobe.x - poissonGlobe.rayon < 0) {
                poissonGlobe.x = poissonGlobe.rayon;
                poissonGlobe.vitesseX = -poissonGlobe.vitesseX;
            } else if (poissonGlobe.x + poissonGlobe.rayon >= largeurTerrain) {
                poissonGlobe.x = largeurTerrain - poissonGlobe.rayon;
                poissonGlobe.vitesseX = -poissonGlobe.vitesseX;
            }
        }
    }
}