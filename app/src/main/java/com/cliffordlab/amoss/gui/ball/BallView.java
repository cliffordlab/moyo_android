package com.cliffordlab.amoss.gui.ball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by ChristopherWainwrightAaron on 1/25/16.
 */
public class BallView extends View {

    public float x;
    public float y;
    private final int r;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BallView(Context context, float x, float y, int r) {
        super(context);

        mPaint.setColor(Color.parseColor("#D5922A"));
        this.x = x;
        this.y = y;
        this.r = r;
    }

    //called by invalidate()
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, r, mPaint);
    }
}
