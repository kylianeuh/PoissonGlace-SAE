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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

public class GameView extends View {

    // =======================================================================
    // --- CONFIGURATION GLOBAL ET CALIBRATION DE LA PHYSIQUE (STATIC) ---
    // =======================================================================

    // --- LE POISSON GLOBE ---
    public static float FACTEUR_TAILLE_POISSON = 0.08f;    // Taille de l'image (proportionnelle à la hauteur de l'écran)
    public static float CONFIG_HITBOX_POISSON_RAYON = 0.70f; // Multiplicateur de la hitbox (1.0 = taille de l'image)
    public static float CONFIG_HITBOX_POISSON_DECALAGE_X = 10f; // Décale la hitbox vers la droite (+) ou gauche (-)
    public static float CONFIG_HITBOX_POISSON_DECALAGE_Y = 0f; // Décale la hitbox vers le bas (+) ou le haut (-)

    // --- LES JOUEURS (BULLES) ---
    public static float FACTEUR_TAILLE_BULLE = 0.05f; // Règle le diamètre visuel global (ex: 0.10f pour plus petit)
    public static float CONFIG_HITBOX_BULLE_RAYON = 1.0f; // 1.0f signifie que la ligne verte se calera PILE sur le contour turquoise
    public static float CONFIG_HITBOX_BULLE_DECALAGE_X = 0f;
    public static float CONFIG_HITBOX_BULLE_DECALAGE_Y = 0f;

    // --- CONSTANTES DE JEU ---
    public static float INTENSITE_IMPACT_TIR = 15f;        // Puissance de propulsion du poisson lors d'un choc
    public static float FRICTION_TERRAIN = 0.98f;          // Glisse du poisson (1.0 = pas de fin, 0.90 = s'arrête très vite)


    private Paint pinceauLignes;
    private Paint pinceauButs;
    private Paint pinceauHitboxDebug;
    private Paint pinceauBullesJoueurs;


    // Pinceaux pour les textes (style PoissonGlaceTextView)
    private Paint pinceauTexteJaune;
    private Paint pinceauTexteContour;

    // Les données de match (Le "Back" de l'arbitrage)
    public String nomJoueurGau = "Joueur 1";
    public String nomJoueurDro = "Joueur 2";
    public int scoreJoueurGau = 0;
    public int scoreJoueurDro = 0;

    public static float CONFIG_RESTITUTION = 0.5f;          // Amorti (0.0 = pas de rebond autonome, 1.0 = rebond parfait style billard)
    public static float CONFIG_MULT_FORCE_DOIGT = 1.2f;     // Multiplicateur de la force de ton geste (Remplace CONFIG_MULT_VITESSE_TRANSFERT)
    public static float CONFIG_VITESSE_MIN_DOIGT = 0.5f;    // Seuil en-dessous duquel le jeu ignore la vitesse du doigt
    public static float CONFIG_VITESSE_MAX_POISSON = 45f;    // Vitesse max autorisée pour le poisson (anti-transpercement)
    public static int PHYSIQUE_SUB_STEPS = 3;

    public float ligneCentraleX, centreX, centreY;
    public float rayonCercleCentral, limiteSableGauche, limiteSableDroite;
    private float pauseBtnX, pauseBtnY, pauseBtnRadius;

    // --- OBJETS DE JEU (BACK) ---
    public PoissonGlobe poissonGlobe;
    public Bulle bulleJoueur1; // Joueur Gauche
    public Bulle bulleJoueur2; // Joueur Droit

    // --- ASSETS GRAPHIQUES ---
    private Bitmap imgPoissonGlobe;

    private float positionYTextes, margeExtremite, ecartScore, correctionYScore;

    private android.os.Handler jeuHandler = new android.os.Handler();
    private Runnable boucleJeu;
    private final int FPS = 60;

    // Suivre l'ID du pointeur (doigt) qui contrôle chaque bulle (Multitouch)
    private int idDoigtJ1 = -1;
    private int idDoigtJ2 = -1;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialiserTerrain(context);
    }

    private void initialiserTerrain(Context context) {
        pinceauLignes = createPaint(Color.WHITE, Paint.Style.STROKE, 12f);
        pinceauButs = createPaint(Color.parseColor("#5C4033"), Paint.Style.FILL, 0f);

        pinceauButs = new Paint();
        pinceauButs.setColor(Color.parseColor("#5C4033"));
        pinceauButs.setStyle(Paint.Style.FILL);

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

        imgPoissonGlobe = BitmapFactory.decodeResource(context.getResources(), R.drawable.fish_brown);

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(rectangleButGauche, pinceauButs);
        canvas.drawRect(rectangleButDroite, pinceauButs);
        canvas.drawLine(ligneCentraleX, 0, ligneCentraleX, getHeight(), pinceauLignes);
        canvas.drawCircle(centreX, centreY, rayonCercleCentral, pinceauLignes);
        canvas.drawArc(zoneButGauche, 270, 180, false, pinceauLignes);
        canvas.drawArc(zoneButDroit, 90, 180, false, pinceauLignes);

        // =======================================================================
        // 4. DESSIN DES ASSETS (Calculs basés sur les variables Static)
        // =======================================================================
        if (!initialisationFaite) {
            initialisationFaite = true;
        }

        // 1. CALCUL DE TOUS LES RAYONS DE BASE VISUELS
        float rayonVisuelPoisson = hauteur * FACTEUR_TAILLE_POISSON;
        float rayonVisuelBulle = hauteur * FACTEUR_TAILLE_BULLE;

        // 2. CORRECTION CRITIQUE : MISE À JOUR DES RAYONS DES OBJETS AVANT TOUT DESSIN
        poissonGlobe.rayon = rayonVisuelPoisson * CONFIG_HITBOX_POISSON_RAYON;
        bulleJoueur1.rayon = rayonVisuelBulle * CONFIG_HITBOX_BULLE_RAYON;
        bulleJoueur2.rayon = rayonVisuelBulle * CONFIG_HITBOX_BULLE_RAYON;

        // --- A. DESSIN DU POISSON (IMAGE + HITBOX RED) ---
        if (imgPoissonGlobe != null) {
            RectF positionPoisson = new RectF(
                    poissonGlobe.x - rayonVisuelPoisson,
                    poissonGlobe.y - rayonVisuelPoisson,
                    poissonGlobe.x + rayonVisuelPoisson,
                    poissonGlobe.y + rayonVisuelPoisson
            );
            canvas.drawBitmap(imgPoissonGlobe, null, positionPoisson, null);
        }


        // --- B. DESSIN DES JOUEURS ---

        // Pinceau pour le corps de la bulle (Bleu eau translucide)
        Paint pinceauCorpsBulle = new Paint();
        pinceauCorpsBulle.setColor(Color.parseColor("#A0E0FF"));
        pinceauCorpsBulle.setStyle(Paint.Style.FILL);
        pinceauCorpsBulle.setAlpha(80);
        pinceauCorpsBulle.setAntiAlias(true);

        // Pinceau pour le reflet de lumière (Donne l'effet sphérique 3D)
        Paint pinceauRefletBulle = new Paint();
        pinceauRefletBulle.setColor(Color.WHITE);
        pinceauRefletBulle.setStyle(Paint.Style.FILL);
        pinceauRefletBulle.setAlpha(180);
        pinceauRefletBulle.setAntiAlias(true);

        Paint pinceauDebugBulle = new Paint(pinceauHitboxDebug);
        pinceauDebugBulle.setColor(Color.WHITE);

        // ---- JOUEUR 1 (GAUCHE) ----
        canvas.drawCircle(bulleJoueur1.x, bulleJoueur1.y, rayonVisuelBulle, pinceauCorpsBulle);
        canvas.drawCircle(bulleJoueur1.x, bulleJoueur1.y, rayonVisuelBulle, pinceauBullesJoueurs);

        // Dessin du reflet réaliste (un petit rond blanc décalé en haut à gauche de la bulle)
        float decalageReflet1 = rayonVisuelBulle * 0.35f;
        float rayonReflet1 = rayonVisuelBulle * 0.15f;
        canvas.drawCircle(bulleJoueur1.x - decalageReflet1, bulleJoueur1.y - decalageReflet1, rayonReflet1, pinceauRefletBulle);

        // Dessin de la Hitbox Verte de Debug
        canvas.drawCircle(
                bulleJoueur1.x + CONFIG_HITBOX_BULLE_DECALAGE_X,
                bulleJoueur1.y + CONFIG_HITBOX_BULLE_DECALAGE_Y,
                bulleJoueur1.rayon,
                pinceauDebugBulle
        );

        // ---- JOUEUR 2 (DROIT) ----
        // Dessin du corps transparent
        canvas.drawCircle(bulleJoueur2.x, bulleJoueur2.y, rayonVisuelBulle, pinceauCorpsBulle);
        // Dessin du contour turquoise
        canvas.drawCircle(bulleJoueur2.x, bulleJoueur2.y, rayonVisuelBulle, pinceauBullesJoueurs);

        // Dessin du reflet réaliste
        canvas.drawCircle(bulleJoueur2.x - decalageReflet1, bulleJoueur2.y - decalageReflet1, rayonReflet1, pinceauRefletBulle);

        // Dessin de la Hitbox Verte de Debug
        canvas.drawCircle(
                bulleJoueur2.x + CONFIG_HITBOX_BULLE_DECALAGE_X,
                bulleJoueur2.y + CONFIG_HITBOX_BULLE_DECALAGE_Y,
                bulleJoueur2.rayon,
                pinceauDebugBulle
        );

            positionAsset.set(bulleJoueur2.x - bulleJoueur2.rayon, bulleJoueur2.y - bulleJoueur2.rayon, bulleJoueur2.x + bulleJoueur2.rayon, bulleJoueur2.y + bulleJoueur2.rayon);
            canvas.drawBitmap(imgBulle, null, positionAsset, null);
        }

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
    }

    private void drawTextWithContour(Canvas canvas, String text, float x, float y) {
        canvas.drawText(text, x, y, pinceauTexteContour);
        canvas.drawText(text, x, y, pinceauTexteJaune);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float dx = event.getX() - pauseBtnX;
            float dy = event.getY() - pauseBtnY;
            if ((dx * dx + dy * dy) <= (pauseBtnRadius * pauseBtnRadius)) {
                declencherPause();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private void declencherPause() {
        // Code de pause
    }


    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        int action = event.getActionMasked();
        int indexPointeur = event.getActionIndex();
        int idPointeur = event.getPointerId(indexPointeur);

        float touchX = event.getX(indexPointeur);
        float touchY = event.getY(indexPointeur);

        switch (action) {
            case android.view.MotionEvent.ACTION_DOWN:
            case android.view.MotionEvent.ACTION_POINTER_DOWN:
                // Vérification J1 (Camp Gauche)
                if (bulleJoueur1.estTouche(touchX, touchY) && idDoigtJ1 == -1) {
                    idDoigtJ1 = idPointeur;
                }
                // Vérification J2 (Camp Droit)
                else if (bulleJoueur2.estTouche(touchX, touchY) && idDoigtJ2 == -1) {
                    idDoigtJ2 = idPointeur;
                }
                break;

            case android.view.MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pId = event.getPointerId(i);
                    float currentX = event.getX(i);
                    float currentY = event.getY(i);

                    if (pId == idDoigtJ1) {
                        bulleJoueur1.x = currentX;
                        bulleJoueur1.y = currentY;
                        // Force la bulle à rester dans l'écran et à gauche de la ligne centrale
                        bulleJoueur1.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, true);
                    } else if (pId == idDoigtJ2) {
                        bulleJoueur2.x = currentX;
                        bulleJoueur2.y = currentY;
                        // Force la bulle à rester dans l'écran et à droite de la ligne centrale
                        bulleJoueur2.contraindreDansLimites(getWidth(), getHeight(), ligneCentraleX, false);
                    }
                }
                break;

            case android.view.MotionEvent.ACTION_UP:
            case android.view.MotionEvent.ACTION_POINTER_UP:
            case android.view.MotionEvent.ACTION_CANCEL:
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

    private void gererPhysiqueEtArbitrage() {
        int largeur = getWidth();
        int hauteur = getHeight();

        // 1. Calculer d'abord la trajectoire et la vitesse des joueurs
        bulleJoueur1.calculerVitesse();
        bulleJoueur2.calculerVitesse();

        // 2. Sub-stepping : On découpe le mouvement du poisson pour éviter qu'il saute par dessus un obstacle
        for (int step = 0; step < PHYSIQUE_SUB_STEPS; step++) {

            // On fait bouger le poisson d'une fraction de sa vitesse
            poissonGlobe.x += (poissonGlobe.vitesseX / PHYSIQUE_SUB_STEPS);
            poissonGlobe.y += (poissonGlobe.vitesseY / PHYSIQUE_SUB_STEPS);

            // On vérifie les collisions avec les murs et les buts à cette micro-étape
            gererMursEtButsEtape(largeur, hauteur);

            // Gestion des collisions dynamiques avec les joueurs
            calculerCollisionBullePoisson(bulleJoueur1);
            calculerCollisionBullePoisson(bulleJoueur2);
        }

        // 3. Application globale de la friction à la fin de la frame
        poissonGlobe.vitesseX *= FRICTION_TERRAIN;
        poissonGlobe.vitesseY *= FRICTION_TERRAIN;

        // 4. Limitation de sécurité (Speed Cap)
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

            // 1. Éjection immédiate pour ne pas que les cercles s'encastrent
            float chevauchement = distanceMin - distance;
            poissonGlobe.x += normalX * chevauchement;
            poissonGlobe.y += normalY * chevauchement;

            // 2. PHYSIQUE DU REBOND (Vitesse relative entre le poisson et la bulle)
            float vitesseRelativeX = poissonGlobe.vitesseX - bulle.vX;
            float vitesseRelativeY = poissonGlobe.vitesseY - bulle.vY;

            // Calcul de la vitesse projetée sur l'axe d'impact
            float vitesseSurNormale = vitesseRelativeX * normalX + vitesseRelativeY * normalY;

            // On applique le rebond uniquement si les objets se rentrent dedans
            if (vitesseSurNormale < 0) {
                // Utilisation de la variable de restitution (amorti) au lieu d'une valeur brute
                float impulsion = -(1.0f + CONFIG_RESTITUTION) * vitesseSurNormale;
                poissonGlobe.vitesseX += normalX * impulsion;
                poissonGlobe.vitesseY += normalY * impulsion;
            }

            // 3. TRANSFERT DYNAMIQUE DE LA FORCE DU GESTE
            // On calcule la vitesse globale (la magnitude) du mouvement du joueur
            float vitesseDoigtMgn = (float) Math.sqrt(bulle.vX * bulle.vX + bulle.vY * bulle.vY);

            if (vitesseDoigtMgn > CONFIG_VITESSE_MIN_DOIGT) {
                // Le poisson est propulsé uniquement par la vitesse de ton doigt multipliée par ton curseur
                float forceAjustee = vitesseDoigtMgn * CONFIG_MULT_FORCE_DOIGT;
                poissonGlobe.vitesseX = normalX * forceAjustee;
                poissonGlobe.vitesseY = normalY * forceAjustee;
            }
        }
    }

    private void remiseEnJeu(boolean auJoueur2) {
        int largeur = getWidth();

        // Repositionne le poisson au centre vertical
        poissonGlobe.y = getHeight() / 2f;
        poissonGlobe.vitesseX = 0;
        poissonGlobe.vitesseY = 0;

        if (auJoueur2) {
            // Devant le joueur 2 (Camp Droit)
            poissonGlobe.x = largeur * 0.70f;
        } else {
            // Devant le joueur 1 (Camp Gauche)
            poissonGlobe.x = largeur * 0.30f;
        }

        // On remet également les bulles à leurs positions initiales pour éviter les tirs réflexes immédiats
        bulleJoueur1.x = largeur * 0.20f;
        bulleJoueur1.y = getHeight() / 2f;
        bulleJoueur2.x = largeur * 0.80f;
        bulleJoueur2.y = getHeight() / 2f;

        // Reset des tracking de doigts
        idDoigtJ1 = -1;
        idDoigtJ2 = -1;

        bulleJoueur1.reinitialiserVitesse();
        bulleJoueur2.reinitialiserVitesse();
    }

    private void gererMursEtButsEtape(int largeurTerrain, int hauteurTerrain) {
        // Murs haut et bas
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

        // Si dans la tranche des buts
        if (poissonGlobe.y >= hautBut && poissonGlobe.y <= basBut) {
            if (poissonGlobe.x - poissonGlobe.rayon <= 0) {
                scoreJoueurDro++;
                remiseEnJeu(false);
            } else if (poissonGlobe.x + poissonGlobe.rayon >= largeurTerrain) {
                scoreJoueurGau++;
                remiseEnJeu(true);
            }
        } else {
            // Murs extérieurs gauche et droit
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