package com.sae402.poissonglobe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class PoissonGlaceTextView extends AppCompatTextView {

    public PoissonGlaceTextView(Context context) {
        super(context);
        init();
    }

    public PoissonGlaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PoissonGlaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setShadowLayer(25, 0, 0, Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getParent() instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) getParent()).setClipChildren(false);
        }

        int originalColor = getCurrentTextColor();

        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(20); // Épaisseur de la bordure
        getPaint().setStrokeJoin(Paint.Join.ROUND);
        setTextColor(Color.WHITE);
        super.onDraw(canvas);

        getPaint().setStyle(Paint.Style.FILL);
        setTextColor(Color.parseColor("#FFCC00"));
        super.onDraw(canvas);

        setTextColor(originalColor);
    }
}