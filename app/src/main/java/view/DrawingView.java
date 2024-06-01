package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.google.mlkit.vision.digitalink.Ink;

import java.util.ArrayList;
import java.util.List;

import presenter.digitalink.GestureHandler;
import presenter.digitalink.RecognitionTask;
import presenter.digitalink.StrokeManager;

/**
 * Main view for rendering content.
 *
 * <p>The view accepts touch inputs, renders them on screen, and passes the content to the
 * StrokeManager. The view is also able to draw content from the StrokeManager.
 */
//public class DrawingView extends androidx.appcompat.widget.AppCompatEditText implements StrokeManager.ContentChangedListener {
public class DrawingView extends androidx.appcompat.widget.AppCompatTextView implements StrokeManager.ContentChangedListener {
    private static final String TAG = "DrawingView";
    private static final int STROKE_WIDTH_DP = 3;
    private final Paint recognizedStrokePaint;
    private final TextPaint textPaint;
    private final Paint currentStrokePaint;
    private final Paint canvasPaint;

    private final Path currentStroke;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private StrokeManager strokeManager;

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        currentStrokePaint = new Paint();
        currentStrokePaint.setColor(0xFFFF00FF); // pink.
        currentStrokePaint.setAntiAlias(true);
        // Set stroke width based on display density.
        currentStrokePaint.setStrokeWidth(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH_DP, getResources().getDisplayMetrics()));
        currentStrokePaint.setStyle(Paint.Style.STROKE);
        currentStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        currentStrokePaint.setStrokeCap(Paint.Cap.ROUND);

        recognizedStrokePaint = new Paint(currentStrokePaint);
        recognizedStrokePaint.setColor(0xFFFFCCFF); // pale pink.

        textPaint = new TextPaint();
        textPaint.setColor(0xFF33CC33); // green.

        currentStroke = new Path();
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }


    public void setStrokeManager(StrokeManager strokeManager) {
        this.strokeManager = strokeManager;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        Log.i(TAG, "onSizeChanged");
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    public void redrawContent() {
        clear();
        Ink currentInk = strokeManager.getCurrentInk();
        drawInk(currentInk, currentStrokePaint);

        List<RecognitionTask.RecognizedInk> content = strokeManager.getContent();

        for (RecognitionTask.RecognizedInk ri : content) {
            drawInk(ri.ink, recognizedStrokePaint);
            GestureHandler.handleGesture(ri, this);
        }

        invalidate();

    }

    private void drawInk(Ink ink, Paint paint) {
        for (Ink.Stroke s : ink.getStrokes()) {
            drawStroke(s, paint);
        }
    }

    private void drawStroke(Ink.Stroke s, Paint paint) {
        Log.i(TAG, "draw stroke");
        Path path = null;
        for (Ink.Point p : s.getPoints()) {
            if (path == null) {
                path = new Path();
                path.moveTo(p.getX(), p.getY());
            } else {
                path.lineTo(p.getX(), p.getY());
            }
        }
        drawCanvas.drawPath(path, paint);
    }

    public void clear() {
        currentStroke.reset();
        onSizeChanged(
                canvasBitmap.getWidth(),
                canvasBitmap.getHeight(),
                canvasBitmap.getWidth(),
                canvasBitmap.getHeight());
    }

//    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas); // 保留 EditText 的默认绘制逻辑

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(currentStroke, currentStrokePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                currentStroke.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                currentStroke.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                currentStroke.lineTo(x, y);
                drawCanvas.drawPath(currentStroke, currentStrokePaint);
                currentStroke.reset();
                break;
            default:
                break;
        }
        strokeManager.addNewTouchEvent(event);
        invalidate();
        return true;
    }

    @Override
    public void onContentChanged() {
        redrawContent();
    }

    @Override
    public Editable getText() {
        CharSequence text = super.getText();
        // This can only happen during construction.
        if (text == null) {
            return null;
        }
        if (text instanceof Editable) {
            return (Editable) text;
        }
        super.setText(text, BufferType.EDITABLE);
        return (Editable) super.getText();
    }

}
