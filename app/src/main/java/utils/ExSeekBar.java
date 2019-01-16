package utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class ExSeekBar extends SeekBar {
    public ExSeekBar(Context context) {
        super(context);
    }
    public ExSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        Paint pen = new Paint();
        pen.setColor(Color.parseColor("#99AABB"));
        pen.setStrokeWidth(1);

        int offset = 34;
        float width = this.getWidth();
        float interval = (float)((width-offset*1.0)/this.getMax());
        float size = this.getHeight() / 2;
        for(int i=0; i<=this.getMax(); i++){
            float x = (i*interval)+ (offset/2);
            canvas.drawLine(x, 0, x, size, pen);
            //canvas.drawText(Integer.toString(i), x, this.getHeight() + 6,pen);
        }
        super.onDraw(canvas);
    }
}
