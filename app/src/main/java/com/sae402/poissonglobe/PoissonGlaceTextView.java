package com.sae402.poissonglobe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class PoissonGlaceTextView  extends AppCompatTextView {
    public PoissonGlaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //LA BORDURE
        int textColor = getCurrentTextColor();
        this.getPaint().setStyle(Paint.Style.STROKE);
        this.getPaint().setStrokeWidth(8); //EPAISSEUR
        this.setTextColor(Color.parseColor("#C68B59"));
        super.onDraw(canvas);

        //TEXTE
        this.getPaint().setStyle(Paint.Style.FILL);
        this.setTextColor(textColor);
        super.onDraw(canvas);

    }
}
