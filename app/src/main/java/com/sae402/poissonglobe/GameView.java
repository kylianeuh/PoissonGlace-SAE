package com.sae402.poissonglobe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

public class GameView extends View {

    public static float FACTEUR_TAILLE_POISSON = 0.08f;
    public static float CONFIG_HITBOX_POISSON_RAYON = 0.70f;
    public static float CONFIG_HITBOX_POISSON_DECALAGE_X = 10f;
    public static float CONFIG_HITBOX_POISSON_DECALAGE_Y = 0f;

    public static float FACTEUR_TAILLE_BULLE = 0.05f;
    public static float CONFIG_HITBOX_BULLE_RAYON = 1.0f;
    public static float CONFIG_HITBOX_BULLE_DECALAGE_X = 0f;
    public static float CONFIG_HITBOX_BULLE_DECALAGE_Y = 0f;

    public static float FRICTION_TERRAIN = 0.98f;
    public static float FRICTION_BULLES = 0.96f;
    public static float CONFIG_RESTITUTION = 0.96f;
    public static float CONFIG_MULT_FORCE_DOIGT = 1.8f;
    public static float CONFIG_VITESSE_MIN_DOIGT = 0.5f;
    public static float CONFIG_VITESSE_MAX_POISSON = 45f;
    public static int PHYSIQUE_SUB_STEPS = 3;

    public int nombreDeJoueursConfig = 2;

    private Paint pinceauLignes;
    private Paint pinceauButs;
    private Paint pinceauHitboxDebug;
    private Paint pinceauBullesJoueurs;
    private Paint pinceauBoutonPause;
    private Paint pinceauBoutonPauseBordure;
    private Paint pinceauSymbolePause;
    private Paint pinceauSymbolePauseBordure;

    private Paint pinceauTexteJaune;
    private Paint pinceauTexteContour;

    public String nomJoueurGau = "Joueur 1";
    public String nomJoueurDro = "Joueur 2";
    public String nomJoueurGau2 = "Joueur 3";
    public String nomJoueurDro2 = "Joueur 4";

    public int scoreJoueurGau = 0;
    public int scoreJoueurDro = 0;

    private android.media.SoundPool soundPool;
    private final int[] sonBulles = new int[5];
    private final int[] sonBords = new int[5];
    private int sonBut;
    private boolean sonsChargés = false;

    public float ligneCentraleX, centreX, centreY;
    public float rayonCercleCentral, limiteSableGauche, limiteSableDroite;

    private RectF rectangleButGauche;
    private RectF rectangleButDroite;
    private RectF zoneButGauche;
    private RectF zoneButDroit;

    private float pauseBtnX, pauseBtnY, pauseBtnRadius;
    private RectF pauseBarLeft;
    private RectF pauseBarRight;

    public PoissonGlobe poissonGlobe;
    public Bulle bulleJoueur1;
    public Bulle bulleJoueur2;
    public Bulle bulleJoueur3;
    public Bulle bulleJoueur4;

    private Drawable svgPoissonGlobe;

    private float positionYTextes, margeExtremite, ecartScore, correctionYScore;
    private boolean initialisationFaite = false;

    private final android.os.Handler jeuHandler = new android.os.Handler();
    private Runnable boucleJeu;
    private final int FPS = 60;

    private int idDoigtJ1 = -1;
    private int idDoigtJ2 = -1;
    private int idDoigtJ3 = -1;
    private int idDoigtJ4 = -1;

    private boolean partieTerminee = false;
    private boolean jeuEnPause = false;

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

    public interface OnPauseRequestedListener {
        void onPauseRequested();
    }
    private OnPauseRequestedListener pauseRequestedListener;

    public void setOnPauseRequestedListener(OnPauseRequestedListener listener) {
        this.pauseRequestedListener = listener;
    }

// LOCALISATION : Remplace TOUTE ta méthode initialiserTerrain par celle-ci dans GameView.java

    private void initialiserTerrain(Context context) {
        pinceauLignes = createPaint(Color.WHITE, Paint.Style.STROKE, 12f);
        pinceauButs = createPaint(Color.parseColor("#5C4033"), Paint.Style.FILL, 0f);

        Typeface typoCherry = ResourcesCompat.getFont(context, R.font.cherry_bomb);

        // 1. INITIALISATION DU SOUNDPOOL (Ce qui manquait et faisait crasher)
        android.media.AudioAttributes audioAttrs = new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_GAME)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        this.soundPool = new android.media.SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttrs)
                .build();

        // 2. CONFIGURATION DE L'ÉCOUTEUR DE DIAGNOSTIC UNIQUE
        this.soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                sonsChargés = true;
                Log.d("DIAGNOSTIC_SONS", "✓ Succès du chargement pour l'ID : " + sampleId);
            } else {
                Log.e("DIAGNOSTIC_SONS", "✕ ÉCHEC CRITIQUE de décodage pour l'ID : " + sampleId + " (Status : " + status + ")");
            }
        });

        // 3. CHARGEMENT SÉCURISÉ DES SAMPLES AUDIO
        sonBulles[0] = soundPool.load(context, R.raw.son_bulle_1, 1);
        sonBulles[1] = soundPool.load(context, R.raw.son_bulle_2, 1);
        sonBulles[2] = soundPool.load(context, R.raw.son_bulle_3, 1);
        sonBulles[3] = soundPool.load(context, R.raw.son_bulle_4, 1);
        sonBulles[4] = soundPool.load(context, R.raw.son_bulle_5, 1);

        sonBords[0] = soundPool.load(context, R.raw.son_bord_1, 1);
        sonBords[1] = soundPool.load(context, R.raw.son_bord_2, 1);
        sonBords[2] = soundPool.load(context, R.raw.son_bord_3, 1);
        sonBords[3] = soundPool.load(context, R.raw.son_bord_4, 1);
        sonBords[4] = soundPool.load(context, R.raw.son_bord_5, 1);

        sonBut = soundPool.load(context, R.raw.but_sound_effect, 1);

        // --- ENSEMBLE DES PINCEAUX GRAPHIK ---
        pinceauTexteContour = new Paint();
        pinceauTexteContour.setTypeface(typoCherry);
        pinceauTexteContour.setColor(Color.WHITE);
        pinceauTexteContour.setTextSize(65f);
        pinceauTexteContour.setStyle(Paint.Style.STROKE);
        pinceauTexteContour.setStrokeWidth(12f);
        pinceauTexteContour.setStrokeJoin(Paint.Join.ROUND);
        pinceauTexteContour.setAntiAlias(true);

        pinceauTexteJaune = new Paint();
        pinceauTexteJaune.setTypeface(typoCherry);
        pinceauTexteJaune.setColor(Color.parseColor("#FFCC00"));
        pinceauTexteJaune.setTextSize(65f);
        pinceauTexteJaune.setStyle(Paint.Style.FILL);
        pinceauTexteJaune.setAntiAlias(true);

        pinceauBoutonPause = createPaint(Color.parseColor("#6622A7F0"), Paint.Style.FILL, 0f);
        pinceauBoutonPauseBordure = createPaint(Color.WHITE, Paint.Style.STROKE, 8f);
        pinceauSymbolePause = createPaint(Color.parseColor("#FFCC00"), Paint.Style.FILL, 0f);

        pinceauSymbolePauseBordure = createPaint(Color.WHITE, Paint.Style.STROKE, 12f);
        pinceauSymbolePauseBordure.setStrokeJoin(Paint.Join.ROUND);

        svgPoissonGlobe = ResourcesCompat.getDrawable(
                context.getResources(),
                R.drawable.fish_brown,
                null
        );

        rectangleButGauche = new RectF();
        rectangleButDroite = new RectF();
        zoneButGauche = new RectF();
        zoneButDroit = new RectF();
        pauseBarLeft = new RectF();
        pauseBarRight = new RectF();

        // MOTEUR DU JEU
        boucleJeu = new Runnable() {
            @Override
            public void run() {
                if (initialisationFaite && !partieTerminee && !jeuEnPause) {
                    gererPhysiqueEtArbitrage();
                }
                invalidate();
                jeuHandler.postDelayed(this, 1000 / FPS);
            }
        };
        jeuHandler.post(boucleJeu);

        pinceauBullesJoueurs = new Paint();
        pinceauBullesJoueurs.setColor(Color.parseColor("#FFFFFF"));
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

        if (nombreDeJoueursConfig == 4) {
            bulleJoueur1 = new Bulle(w * 0.25f, h * 0.33f, h * 0.13f);
            bulleJoueur3 = new Bulle(w * 0.25f, h * 0.66f, h * 0.13f);
            bulleJoueur2 = new Bulle(w * 0.75f, h * 0.33f, h * 0.13f);
            bulleJoueur4 = new Bulle(w * 0.75f, h * 0.66f, h * 0.13f);
        } else {
            bulleJoueur1 = new Bulle(w * 0.25f, centreY, h * 0.13f);
            bulleJoueur2 = new Bulle(w * 0.75f, centreY, h * 0.13f);
            bulleJoueur3 = new Bulle(w * 0.10f, centreY, h * 0.13f);
            bulleJoueur4 = new Bulle(w * 0.90f, centreY, h * 0.13f);
        }

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
        margeExtremite = w * 0.04f;
        ecartScore = 220f;
        correctionYScore = positionYTextes + 15f;

        initialisationFaite = true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int hauteur = getHeight();

        canvas.drawRect(rectangleButGauche, pinceauButs);
        canvas.drawRect(rectangleButDroite, pinceauButs);
        canvas.drawLine(ligneCentraleX, 0, ligneCentraleX, hauteur, pinceauLignes);
        canvas.drawCircle(centreX, centreY, rayonCercleCentral, pinceauLignes);
        canvas.drawArc(zoneButGauche, 270, 180, false, pinceauLignes);
        canvas.drawArc(zoneButDroit, 90, 180, false, pinceauLignes);

        float rayonVisuelPoisson = hauteur * FACTEUR_TAILLE_POISSON;
        float rayonVisuelBulle = hauteur * FACTEUR_TAILLE_BULLE;

        poissonGlobe.rayon = rayonVisuelPoisson * CONFIG_HITBOX_POISSON_RAYON;
        bulleJoueur1.rayon = rayonVisuelBulle * CONFIG_HITBOX_BULLE_RAYON;
        bulleJoueur2.rayon = rayonVisuelBulle * CONFIG_HITBOX_BULLE_RAYON;
        bulleJoueur3.rayon = rayonVisuelBulle * CONFIG_HITBOX_BULLE_RAYON;
        bulleJoueur4.rayon = rayonVisuelBulle * CONFIG_HITBOX_BULLE_RAYON;

        if (svgPoissonGlobe != null) {
            int gauche = (int) (poissonGlobe.x - rayonVisuelPoisson);
            int haut   = (int) (poissonGlobe.y - rayonVisuelPoisson);
            int droite = (int) (poissonGlobe.x + rayonVisuelPoisson);
            int bas    = (int) (poissonGlobe.y + rayonVisuelPoisson);

            svgPoissonGlobe.setBounds(gauche, haut, droite, bas);
            svgPoissonGlobe.draw(canvas);
        }

        Paint pinceauCorpsBulle = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinceauCorpsBulle.setColor(Color.parseColor("#A0E0FF"));
        pinceauCorpsBulle.setStyle(Paint.Style.FILL);
        pinceauCorpsBulle.setAlpha(80);

        Paint pinceauRefletBulle = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinceauRefletBulle.setColor(Color.WHITE);
        pinceauRefletBulle.setStyle(Paint.Style.FILL);
        pinceauRefletBulle.setAlpha(180);

        float decalageReflet = rayonVisuelBulle * 0.35f;
        float rayonReflet = rayonVisuelBulle * 0.15f;

        dessinerBulleVectorielle(canvas, bulleJoueur1, rayonVisuelBulle, decalageReflet, rayonReflet, pinceauCorpsBulle, pinceauRefletBulle);
        if (nombreDeJoueursConfig == 4) {
            dessinerBulleVectorielle(canvas, bulleJoueur3, rayonVisuelBulle, decalageReflet, rayonReflet, pinceauCorpsBulle, pinceauRefletBulle);
        }

        dessinerBulleVectorielle(canvas, bulleJoueur2, rayonVisuelBulle, decalageReflet, rayonReflet, pinceauCorpsBulle, pinceauRefletBulle);
        if (nombreDeJoueursConfig == 4) {
            dessinerBulleVectorielle(canvas, bulleJoueur4, rayonVisuelBulle, decalageReflet, rayonReflet, pinceauCorpsBulle, pinceauRefletBulle);
        }

        canvas.drawCircle(pauseBtnX, pauseBtnY, pauseBtnRadius, pinceauBoutonPause);
        canvas.drawCircle(pauseBtnX, pauseBtnY, pauseBtnRadius, pinceauBoutonPauseBordure);
        canvas.drawRect(pauseBarLeft, pinceauSymbolePauseBordure);
        canvas.drawRect(pauseBarRight, pinceauSymbolePauseBordure);
        canvas.drawRect(pauseBarLeft, pinceauSymbolePause);
        canvas.drawRect(pauseBarRight, pinceauSymbolePause);

        String safeJ1 = (nomJoueurGau != null) ? nomJoueurGau : "Joueur 1";
        String safeJ2 = (nomJoueurDro != null) ? nomJoueurDro : "Joueur 2";
        String safeJ3 = (nomJoueurGau2 != null) ? nomJoueurGau2 : "Joueur 3";
        String safeJ4 = (nomJoueurDro2 != null) ? nomJoueurDro2 : "Joueur 4";

        String texteGauche = (nombreDeJoueursConfig == 4) ? safeJ1 + " + " + safeJ3 : safeJ1;
        String texteDroit = (nombreDeJoueursConfig == 4) ? safeJ2 + " + " + safeJ4 : safeJ2;

        pinceauTexteContour.setTextAlign(Paint.Align.LEFT);
        pinceauTexteJaune.setTextAlign(Paint.Align.LEFT);
        drawTextWithContour(canvas, texteGauche, margeExtremite, positionYTextes);

        pinceauTexteContour.setTextAlign(Paint.Align.RIGHT);
        pinceauTexteJaune.setTextAlign(Paint.Align.RIGHT);
        drawTextWithContour(canvas, texteDroit, getWidth() - margeExtremite, positionYTextes);

        pinceauTexteContour.setTextAlign(Paint.Align.CENTER);
        pinceauTexteJaune.setTextAlign(Paint.Align.CENTER);
        drawTextWithContour(canvas, String.valueOf(scoreJoueurGau), centreX - ecartScore, correctionYScore);
        drawTextWithContour(canvas, String.valueOf(scoreJoueurDro), centreX + ecartScore, correctionYScore);

        if (!partieTerminee) {
            if (scoreJoueurGau >= 6 || scoreJoueurDro >= 6) {
                partieTerminee = true;
                String vainqueur;
                if (scoreJoueurGau >= 6) {
                    vainqueur = (nombreDeJoueursConfig == 4) ? nomJoueurGau + " & " + nomJoueurGau2 : nomJoueurGau;
                } else {
                    vainqueur = (nombreDeJoueursConfig == 4) ? nomJoueurDro + " & " + nomJoueurDro2 : nomJoueurDro;
                }

                if (gameOverListener != null) {
                    gameOverListener.onGameOver(vainqueur);
                }
            }
        }
    }

    private void dessinerBulleVectorielle(Canvas canvas, Bulle b, float rVisuel, float decReflet, float rReflet, Paint corps, Paint reflet) {
        canvas.drawCircle(b.x, b.y, rVisuel, corps);
        canvas.drawCircle(b.x, b.y, rVisuel, pinceauBullesJoueurs);
        canvas.drawCircle(b.x - decReflet, b.y - decReflet, rReflet, reflet);
    }

    private void drawTextWithContour(Canvas canvas, String text, float x, float y) {
        canvas.drawText(text, x, y, pinceauTexteContour);
        canvas.drawText(text, x, y, pinceauTexteJaune);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (jeuEnPause) return false;

        int action = event.getActionMasked();
        int indexPointeur = event.getActionIndex();
        int idPointeur = event.getPointerId(indexPointeur);

        float touchX = event.getX(indexPointeur);
        float touchY = event.getY(indexPointeur);

        if (action == MotionEvent.ACTION_DOWN) {
            float dx = touchX - pauseBtnX;
            float dy = touchY - pauseBtnY;
            if ((dx * dx + dy * dy) <= (pauseBtnRadius * pauseBtnRadius)) {
                declencherPauseInterne();
                return true;
            }
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (bulleJoueur1.estTouche(touchX, touchY) && idDoigtJ1 == -1) idDoigtJ1 = idPointeur;
                else if (bulleJoueur2.estTouche(touchX, touchY) && idDoigtJ2 == -1) idDoigtJ2 = idPointeur;
                else if (nombreDeJoueursConfig == 4 && bulleJoueur3.estTouche(touchX, touchY) && idDoigtJ3 == -1) idDoigtJ3 = idPointeur;
                else if (nombreDeJoueursConfig == 4 && bulleJoueur4.estTouche(touchX, touchY) && idDoigtJ4 == -1) idDoigtJ4 = idPointeur;
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pId = event.getPointerId(i);
                    float currentX = event.getX(i);
                    float currentY = event.getY(i);

                    if (pId == idDoigtJ1) {
                        bulleJoueur1.x = currentX; bulleJoueur1.y = currentY;
                        bulleJoueur1.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, true);
                    } else if (pId == idDoigtJ2) {
                        bulleJoueur2.x = currentX; bulleJoueur2.y = currentY;
                        bulleJoueur2.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, false);
                    } else if (pId == idDoigtJ3 && nombreDeJoueursConfig == 4) {
                        bulleJoueur3.x = currentX; bulleJoueur3.y = currentY;
                        bulleJoueur3.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, true);
                    } else if (pId == idDoigtJ4 && nombreDeJoueursConfig == 4) {
                        bulleJoueur4.x = currentX; bulleJoueur4.y = currentY;
                        bulleJoueur4.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, false);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (idPointeur == idDoigtJ1) idDoigtJ1 = -1;
                else if (idPointeur == idDoigtJ2) idDoigtJ2 = -1;
                else if (idPointeur == idDoigtJ3) idDoigtJ3 = -1;
                else if (idPointeur == idDoigtJ4) idDoigtJ4 = -1;
                break;
        }
        return true;
    }

    private void declencherPauseInterne() {
        this.jeuEnPause = true;

        idDoigtJ1 = -1; idDoigtJ2 = -1; idDoigtJ3 = -1; idDoigtJ4 = -1;
        bulleJoueur1.reinitialiserVitesse();
        bulleJoueur2.reinitialiserVitesse();
        if (bulleJoueur3 != null) bulleJoueur3.reinitialiserVitesse();
        if (bulleJoueur4 != null) bulleJoueur4.reinitialiserVitesse();

        if (pauseRequestedListener != null) {
            pauseRequestedListener.onPauseRequested();
        }
    }

    public void reprendreJeu() {
        this.jeuEnPause = false;
    }

    private void gererPhysiqueEtArbitrage() {
        int largeur = getWidth();
        int hauteur = getHeight();

        if (idDoigtJ1 != -1) bulleJoueur1.calculerVitesse();
        if (idDoigtJ2 != -1) bulleJoueur2.calculerVitesse();
        if (nombreDeJoueursConfig == 4) {
            if (idDoigtJ3 != -1) bulleJoueur3.calculerVitesse();
            if (idDoigtJ4 != -1) bulleJoueur4.calculerVitesse();
        }

        for (int step = 0; step < PHYSIQUE_SUB_STEPS; step++) {
            poissonGlobe.x += (poissonGlobe.vitesseX / PHYSIQUE_SUB_STEPS);
            poissonGlobe.y += (poissonGlobe.vitesseY / PHYSIQUE_SUB_STEPS);

            if (idDoigtJ1 == -1) { bulleJoueur1.x += (bulleJoueur1.vX / PHYSIQUE_SUB_STEPS); bulleJoueur1.y += (bulleJoueur1.vY / PHYSIQUE_SUB_STEPS); }
            if (idDoigtJ2 == -1) { bulleJoueur2.x += (bulleJoueur2.vX / PHYSIQUE_SUB_STEPS); bulleJoueur2.y += (bulleJoueur2.vY / PHYSIQUE_SUB_STEPS); }
            if (nombreDeJoueursConfig == 4) {
                if (idDoigtJ3 == -1) { bulleJoueur3.x += (bulleJoueur3.vX / PHYSIQUE_SUB_STEPS); bulleJoueur3.y += (bulleJoueur3.vY / PHYSIQUE_SUB_STEPS); }
                if (idDoigtJ4 == -1) { bulleJoueur4.x += (bulleJoueur4.vX / PHYSIQUE_SUB_STEPS); bulleJoueur4.y += (bulleJoueur4.vY / PHYSIQUE_SUB_STEPS); }
            }

            gererMursEtButsEtape(largeur, hauteur);
            gererRebondsMursPourBulles(largeur, hauteur);

            calculerCollisionBullePoisson(bulleJoueur1);
            calculerCollisionBullePoisson(bulleJoueur2);
            if (nombreDeJoueursConfig == 4) {
                calculerCollisionBullePoisson(bulleJoueur3);
                calculerCollisionBullePoisson(bulleJoueur4);
                gererCollisionEntreJoueurs(bulleJoueur1, bulleJoueur3);
                gererCollisionEntreJoueurs(bulleJoueur2, bulleJoueur4);
            }
        }

        poissonGlobe.vitesseX *= FRICTION_TERRAIN;
        poissonGlobe.vitesseY *= FRICTION_TERRAIN;

        if (idDoigtJ1 == -1) { bulleJoueur1.vX *= FRICTION_BULLES; bulleJoueur1.vY *= FRICTION_BULLES; }
        if (idDoigtJ2 == -1) { bulleJoueur2.vX *= FRICTION_BULLES; bulleJoueur2.vY *= FRICTION_BULLES; }
        if (nombreDeJoueursConfig == 4) {
            if (idDoigtJ3 == -1) { bulleJoueur3.vX *= FRICTION_BULLES; bulleJoueur3.vY *= FRICTION_BULLES; }
            if (idDoigtJ4 == -1) { bulleJoueur4.vX *= FRICTION_BULLES; bulleJoueur4.vY *= FRICTION_BULLES; }
        }

        limiterVitessePoisson();
    }

    private void gererRebondsMursPourBulles(int largeur, int hauteur) {
        Bulle[] campGauche = (nombreDeJoueursConfig == 4) ? new Bulle[]{bulleJoueur1, bulleJoueur3} : new Bulle[]{bulleJoueur1};
        Bulle[] campDroit = (nombreDeJoueursConfig == 4) ? new Bulle[]{bulleJoueur2, bulleJoueur4} : new Bulle[]{bulleJoueur2};

        for (Bulle b : campGauche) {
            if (b.y - b.rayon < 0) { b.y = b.rayon; b.vY = -b.vY * CONFIG_RESTITUTION; }
            else if (b.y + b.rayon > hauteur) { b.y = hauteur - b.rayon; b.vY = -b.vY * CONFIG_RESTITUTION; }
            if (b.x - b.rayon < 0) { b.x = b.rayon; b.vX = -b.vX * CONFIG_RESTITUTION; }
            else if (b.x + b.rayon > ligneCentraleX) { b.x = ligneCentraleX - b.rayon; b.vX = -b.vX * CONFIG_RESTITUTION; }
        }

        for (Bulle b : campDroit) {
            if (b.y - b.rayon < 0) { b.y = b.rayon; b.vY = -b.vY * CONFIG_RESTITUTION; }
            else if (b.y + b.rayon > hauteur) { b.y = hauteur - b.rayon; b.vY = -b.vY * CONFIG_RESTITUTION; }
            if (b.x + b.rayon > largeur) { b.x = largeur - b.rayon; b.vX = -b.vX * CONFIG_RESTITUTION; }
            else if (b.x - b.rayon < ligneCentraleX) { b.x = ligneCentraleX + b.rayon; b.vX = -b.vX * CONFIG_RESTITUTION; }
        }
    }

    private void calculerCollisionBullePoisson(Bulle bulle) {
        float dx = (poissonGlobe.x + CONFIG_HITBOX_POISSON_DECALAGE_X) - (bulle.x + CONFIG_HITBOX_BULLE_DECALAGE_X);
        float dy = (poissonGlobe.y + CONFIG_HITBOX_POISSON_DECALAGE_Y) - (bulle.y + CONFIG_HITBOX_BULLE_DECALAGE_Y);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float distanceMin = poissonGlobe.rayon + bulle.rayon;

        if (distance < distanceMin && distance > 0) {
            float normalX = dx / distance;
            float normalY = dy / distance;

            poissonGlobe.x += normalX * (distanceMin - distance);
            poissonGlobe.y += normalY * (distanceMin - distance);

            float vitesseRelativeX = poissonGlobe.vitesseX - bulle.vX;
            float vitesseRelativeY = poissonGlobe.vitesseY - bulle.vY;
            float vitesseSurNormale = vitesseRelativeX * normalX + vitesseRelativeY * normalY;

            if (vitesseSurNormale < 0) {
                float impulsion = -(1.0f + CONFIG_RESTITUTION) * vitesseSurNormale;
                poissonGlobe.vitesseX += normalX * impulsion;
                poissonGlobe.vitesseY += normalY * impulsion;
            }

            float vitesseMgn = (float) Math.sqrt(bulle.vX * bulle.vX + bulle.vY * bulle.vY);
            if (vitesseMgn > CONFIG_VITESSE_MIN_DOIGT) {
                poissonGlobe.vitesseX = normalX * (vitesseMgn * CONFIG_MULT_FORCE_DOIGT);
                poissonGlobe.vitesseY = normalY * (vitesseMgn * CONFIG_MULT_FORCE_DOIGT);
            }
            jouerSonBulle();
        }
    }

    private void gererCollisionEntreJoueurs(Bulle b1, Bulle b2) {
        float dx = b1.x - b2.x;
        float dy = b1.y - b2.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float minDist = b1.rayon + b2.rayon;

        if (dist < minDist && dist > 0) {
            float normalX = dx / dist;
            float normalY = dy / dist;
            float chevauchement = (minDist - dist) / 2f;

            b1.x += normalX * chevauchement; b1.y += normalY * chevauchement;
            b2.x -= normalX * chevauchement; b2.y -= normalY * chevauchement;

            b1.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, true);
            b2.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, b2 == bulleJoueur3);
        }
    }

    private void limiterVitessePoisson() {
        float v = (float) Math.sqrt(poissonGlobe.vitesseX * poissonGlobe.vitesseX + poissonGlobe.vitesseY * poissonGlobe.vitesseY);
        if (v > CONFIG_VITESSE_MAX_POISSON) {
            poissonGlobe.vitesseX = (poissonGlobe.vitesseX / v) * CONFIG_VITESSE_MAX_POISSON;
            poissonGlobe.vitesseY = (poissonGlobe.vitesseY / v) * CONFIG_VITESSE_MAX_POISSON;
        }
    }

    private void remiseEnJeu(boolean auJoueur2) {
        int largeur = getWidth();
        int hauteur = getHeight();

        poissonGlobe.x = largeur / 2f;
        poissonGlobe.y = hauteur / 2f;
        poissonGlobe.vitesseX = 0;
        poissonGlobe.vitesseY = 0;

        if (nombreDeJoueursConfig == 4) {
            bulleJoueur1.x = largeur * 0.25f;
            bulleJoueur1.y = hauteur * 0.33f;
            bulleJoueur3.x = largeur * 0.25f;
            bulleJoueur3.y = hauteur * 0.66f;

            bulleJoueur2.x = largeur * 0.75f;
            bulleJoueur2.y = hauteur * 0.33f;
            bulleJoueur4.x = largeur * 0.75f;
            bulleJoueur4.y = hauteur * 0.66f;

        } else {

            bulleJoueur1.x = largeur * 0.25f;
            bulleJoueur1.y = hauteur / 2f;
            bulleJoueur2.x = largeur * 0.75f;
            bulleJoueur2.y = hauteur / 2f;

            bulleJoueur3.x = largeur * 0.10f; bulleJoueur3.y = hauteur * 0.50f;
            bulleJoueur4.x = largeur * 0.90f; bulleJoueur4.y = hauteur * 0.50f;
        }

        idDoigtJ1 = -1; idDoigtJ2 = -1; idDoigtJ3 = -1; idDoigtJ4 = -1;
        bulleJoueur1.reinitialiserVitesse();
        bulleJoueur2.reinitialiserVitesse();
        bulleJoueur3.reinitialiserVitesse();
        bulleJoueur4.reinitialiserVitesse();
    }

    private void gererMursEtButsEtape(int largeurTerrain, int hauteurTerrain) {
        if (poissonGlobe.y - poissonGlobe.rayon < 0) {
            poissonGlobe.y = poissonGlobe.rayon; poissonGlobe.vitesseY = -poissonGlobe.vitesseY; jouerSonBord();
        } else if (poissonGlobe.y + poissonGlobe.rayon > hauteurTerrain) {
            poissonGlobe.y = hauteurTerrain - poissonGlobe.rayon; poissonGlobe.vitesseY = -poissonGlobe.vitesseY; jouerSonBord();
        }

        float hauteurBut = hauteurTerrain / 3f;
        float hautBut = (hauteurTerrain / 2f) - (hauteurBut / 2f);
        float basBut = (hauteurTerrain / 2f) + (hauteurBut / 2f);

        if (poissonGlobe.y >= hautBut && poissonGlobe.y <= basBut) {
            if (poissonGlobe.x - poissonGlobe.rayon <= 0) {
                scoreJoueurDro++; jouerSonBut(); remiseEnJeu(false);
            } else if (poissonGlobe.x + poissonGlobe.rayon >= largeurTerrain) {
                scoreJoueurGau++; jouerSonBut(); remiseEnJeu(true);
            }
        } else {
            if (poissonGlobe.x - poissonGlobe.rayon < 0) {
                poissonGlobe.x = poissonGlobe.rayon; poissonGlobe.vitesseX = -poissonGlobe.vitesseX; jouerSonBord();
            } else if (poissonGlobe.x + poissonGlobe.rayon >= largeurTerrain) {
                poissonGlobe.x = largeurTerrain - poissonGlobe.rayon; poissonGlobe.vitesseX = -poissonGlobe.vitesseX; jouerSonBord();
            }
        }
    }

    private void jouerSonBulle() {
        if (sonsChargés && soundPool != null) {
            int indexAleatoire = (int) (Math.random() * 5);
            soundPool.play(sonBulles[indexAleatoire], 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    private void jouerSonBord() {
        if (sonsChargés && soundPool != null) {
            int indexAleatoire = (int) (Math.random() * 5);
            soundPool.play(sonBords[indexAleatoire], 0.8f, 0.8f, 1, 0, 1.0f);
        }
    }

    private void jouerSonBut() {
        if (sonsChargés && soundPool != null) {
            soundPool.play(sonBut, 1.0f, 1.0f, 2, 0, 1.0f);
        }
    }

    public void couperLesSons() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}