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

    private Paint pinceauLignes, pinceauButs;
    private Paint pinceauTexteJaune, pinceauTexteContour;
    private Paint pinceauBoutonPause, pinceauBoutonPauseBordure, pinceauSymbolePause, pinceauSymbolePauseBordure;

    public float ligneCentraleX, centreX, centreY;
    public float rayonCercleCentral, limiteSableGauche, limiteSableDroite;
    private float pauseBtnX, pauseBtnY, pauseBtnRadius;

    private final RectF rectangleButGauche = new RectF();
    private final RectF rectangleButDroite = new RectF();
    private final RectF zoneButGauche = new RectF();
    private final RectF zoneButDroit = new RectF();
    private final RectF pauseBarLeft = new RectF();
    private final RectF pauseBarRight = new RectF();
    private final RectF positionAsset = new RectF();

    public String nomJoueurGau = "Joueur 1", nomJoueurDro = "Joueur 2";
    public int scoreJoueurGau = 0, scoreJoueurDro = 0;

    public PoissonGlobe poissonGlobe;
    public Bulle bulleJoueur1, bulleJoueur2;
    private Bitmap imgPoissonGlobe, imgBulle;

    private float positionYTextes, margeExtremite, ecartScore, correctionYScore;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialiserTerrain(context);
    }

    private void initialiserTerrain(Context context) {
        pinceauLignes = createPaint(Color.WHITE, Paint.Style.STROKE, 12f);
        pinceauButs = createPaint(Color.parseColor("#5C4033"), Paint.Style.FILL, 0f);

        Typeface typoCherry = ResourcesCompat.getFont(context, R.font.cherry_bomb);

        pinceauTexteContour = createPaint(Color.WHITE, Paint.Style.STROKE, 12f);
        pinceauTexteContour.setTypeface(typoCherry);
        pinceauTexteContour.setTextSize(65f);
        pinceauTexteContour.setStrokeJoin(Paint.Join.ROUND);
        pinceauTexteContour.setTextAlign(Paint.Align.CENTER);

        pinceauTexteJaune = createPaint(Color.parseColor("#FFCC00"), Paint.Style.FILL, 0f);
        pinceauTexteJaune.setTypeface(typoCherry);
        pinceauTexteJaune.setTextSize(65f);
        pinceauTexteJaune.setTextAlign(Paint.Align.CENTER);

        pinceauBoutonPause = createPaint(Color.parseColor("#6622A7F0"), Paint.Style.FILL, 0f);
        pinceauBoutonPauseBordure = createPaint(Color.WHITE, Paint.Style.STROKE, 8f);
        pinceauSymbolePause = createPaint(Color.parseColor("#FFCC00"), Paint.Style.FILL, 0f);

        pinceauSymbolePauseBordure = createPaint(Color.WHITE, Paint.Style.STROKE, 12f);
        pinceauSymbolePauseBordure.setStrokeJoin(Paint.Join.ROUND);

        imgPoissonGlobe = BitmapFactory.decodeResource(context.getResources(), R.drawable.fish_brown);
        imgBulle = BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_c);
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

        if (imgPoissonGlobe != null) {
            positionAsset.set(poissonGlobe.x - poissonGlobe.rayon, poissonGlobe.y - poissonGlobe.rayon, poissonGlobe.x + poissonGlobe.rayon, poissonGlobe.y + poissonGlobe.rayon);
            canvas.drawBitmap(imgPoissonGlobe, null, positionAsset, null);
        }
        if (imgBulle != null) {
            positionAsset.set(bulleJoueur1.x - bulleJoueur1.rayon, bulleJoueur1.y - bulleJoueur1.rayon, bulleJoueur1.x + bulleJoueur1.rayon, bulleJoueur1.y + bulleJoueur1.rayon);
            canvas.drawBitmap(imgBulle, null, positionAsset, null);

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
}