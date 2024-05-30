package view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.scancreateprofesser.R;
import com.google.mlkit.vision.digitalink.Ink;

public class DrawView extends View {
    private final String TAG = "DrawView";
    private static final int STROKE_WIDTH_DP = 3;
    private static final int MIN_BB_WIDTH = 10;
    private static final int MIN_BB_HEIGHT = 10;
    private static final int MAX_BB_WIDTH = 256;
    private static final int MAX_BB_HEIGHT = 256;

    private final Paint recognizedStrokePaint;
    private final TextPaint textPaint;
    private final Paint currentStrokePaint;
    private final Paint canvasPaint;

    private final Path currentStroke;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    public DrawView(Context context) {
        this(context, null);
    }

    /**
     * @param context The Context the view is running in, through which it can
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentStrokePaint = new Paint();
        currentStrokePaint.setColor(R.integer.current_stroke_paint_color);

        // anti alias
        currentStrokePaint.setAntiAlias(true);

        // Set stroke width based on display density.
        currentStrokePaint.setStrokeWidth(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH_DP,
                        getResources().getDisplayMetrics()
                )
        );

        // Set stroke cap and join styles.
        currentStrokePaint.setStyle(Paint.Style.STROKE);
        currentStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        
        recognizedStrokePaint = new Paint(currentStrokePaint);
        recognizedStrokePaint.setColor(R.integer.recognized_stroke_paint); // pale pink.

        textPaint = new TextPaint();
        textPaint.setColor(0xFF33CC33); // green.

        currentStroke = new Path();
        canvasPaint = new Paint(Paint.DITHER_FLAG);

    }

    private static Rect computeBoundingBox(Ink ink) {
        float top = Float.MAX_VALUE;
        float left = Float.MAX_VALUE;
        float bottom = Float.MIN_VALUE;
        float right = Float.MIN_VALUE;

        for (Ink.Stroke s : ink.getStrokes()) {
            for (Ink.Point p : s.getPoints()) {
                top = Math.min(top, p.getY());
                left = Math.min(left, p.getX());
                bottom = Math.max(bottom, p.getY());
                right = Math.max(right, p.getX());
            }
        }

        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;

        Rect bb = new Rect((int) left, (int) top, (int) right, (int) bottom);
        // Enforce a minimum size of the bounding box such that recognitions for small inks are readable
        bb.union(
                (int) (centerX - MIN_BB_WIDTH / 2),
                (int) (centerY - MIN_BB_HEIGHT / 2),
                (int) (centerX + MIN_BB_WIDTH / 2),
                (int) (centerY + MIN_BB_HEIGHT / 2));

        // Enforce a maximum size of the bounding box, to ensure Emoji characters get displayed correctly
        if (bb.width() > MAX_BB_WIDTH) {
            bb.set(bb.centerX() - MAX_BB_WIDTH / 2, bb.top, bb.centerX() + MAX_BB_WIDTH / 2, bb.bottom);
        }
        if (bb.height() > MAX_BB_HEIGHT) {
            bb.set(bb.left, bb.centerY() - MAX_BB_HEIGHT / 2, bb.right, bb.centerY() + MAX_BB_HEIGHT / 2);
        }

        return bb;
    }
}
