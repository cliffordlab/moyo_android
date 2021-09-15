package com.cliffordlab.amoss.gui.ball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by ChristopherWainwrightAaron on 1/25/16.
 */
public class HoleView extends View {

    private final Paint mPaint = new Paint();
    public float x;
    public float y;
    private final int[] colors = {Color.BLACK,Color.BLUE,Color.GRAY,Color.GREEN};

    public HoleView(Context context, float x, float y, int colorIndex){
        super(context);
        mPaint.setColor(colors[colorIndex]);
        this.x = x;
        this.y = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, 50, mPaint);
    }
}
