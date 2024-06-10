package presenter.digitalink;

import android.graphics.Rect;
import android.text.Editable;
import android.text.Layout;
import android.util.Log;

import com.google.mlkit.vision.digitalink.Ink;

import java.util.ArrayList;

import view.DrawingView;

public class GestureHandler {
    private final static String TAG = "GestureHandler";

    private static final int MIN_BB_WIDTH = 10;
    private static final int MIN_BB_HEIGHT = 10;
    private static final int MAX_BB_WIDTH = 256;
    private static final int MAX_BB_HEIGHT = 256;
    private final static String cursor = "█";
    private final DrawingView view;
    private boolean hasCursor;

    public GestureHandler(DrawingView view) {
        this.view = view;

        Editable editableText = view.getText();
        String editableTextString = editableText.toString();
        int startPos = editableTextString.indexOf(cursor);
        hasCursor = startPos != -1;
    }


    /**
     * Writes the word that is in the bounding box of the ink
     *
     * @param ri the recognized ink
     */
    public void writeWord(RecognitionTask.RecognizedInk ri) {
        Editable editableText = view.getText();
        if (hasCursor) {
            String textToProcessed = editableText.toString();
            String processedText = textToProcessed.replaceAll(cursor, ri.text);
            editableText.replace(0, editableText.length(), processedText);
            Log.d(TAG, "write with cursor");
        } else {
            editableText.append(ri.text);
            Log.d(TAG, "write");
        }
        hasCursor = false;
    }

    public void addCursor(RecognitionTask.RecognizedInk ri) {
        final Rect bb = computeBoundingBox(ri.ink);
        Layout layout = view.getLayout();

        int offset = getCharacterOffsetFromCart(layout, bb, false);

        Editable editableText = view.getText();
        editableText.insert(offset, cursor);
        hasCursor = true;
    }

    public void addSpace(RecognitionTask.RecognizedInk ri, boolean isAbove) {
        final Rect bb = computeBoundingBox(ri.ink);
        Layout layout = view.getLayout();

        int offset = getCharacterOffsetFromCart(layout, bb, isAbove);

        Editable editableText = view.getText();
        editableText.insert(offset, " ");
    }

    /**
     * Adds a new line to the text
     *
     * @param ri the recognized ink
     */
    public void addCR(RecognitionTask.RecognizedInk ri) {

        final Rect bb = computeBoundingBox(ri.ink);
        Layout layout = view.getLayout();

        ArrayList<Integer> arr = getCharacterOffsetFromRect(layout, bb);

        Integer startOffset = arr.get(0);
        Integer endOffset = arr.get(1);

        if (startOffset >= 0 && endOffset >= 0 && startOffset < endOffset) {
            Editable editableText = view.getText();

            Log.d(TAG, "addCR");
            editableText.insert(endOffset, "\n");
        }

    }

    /**
     * Wraps the word that is in the bounding box of the ink with *
     *
     * @param ri the recognized ink
     */
    public void strong(RecognitionTask.RecognizedInk ri) {
        final Rect bb = computeBoundingBox(ri.ink);
        Layout layout = view.getLayout();

        ArrayList<Integer> arr = getCharacterOffsetFromRect(layout, bb);

        Integer startOffset = arr.get(0);
        Integer endOffset = arr.get(1);

        if (startOffset >= 0 && endOffset >= 0 && startOffset < endOffset) {
            Editable editableText = view.getText();

            // Log
            char[] text = new char[endOffset - startOffset];
            editableText.getChars(startOffset, endOffset, text, 0);
            Log.d(TAG, "warp: " + new String(text) + " from " + startOffset + " to " + endOffset);

            String textToWrap = editableText.subSequence(startOffset, endOffset).toString();
            String wrappedText = " *" + textToWrap + "* ";
            editableText.replace(startOffset, endOffset, wrappedText);

        }
    }

    /**
     * Deletes the word that is in the bounding box of the ink
     *
     * @param ri the recognized ink
     */
    public void deleteWord(RecognitionTask.RecognizedInk ri) {

        final Rect bb = computeBoundingBox(ri.ink);
        Layout layout = view.getLayout();

        ArrayList<Integer> arr = getCharacterOffsetFromRect(layout, bb);

        Integer startOffset = arr.get(0);
        Integer endOffset = arr.get(1);

        if (startOffset >= 0 && endOffset >= 0 && startOffset < endOffset) {
            Editable editableText = view.getText();

            char[] text = new char[endOffset - startOffset];
            editableText.getChars(startOffset, endOffset, text, 0);
            Log.d(TAG, "Deleting: " + new String(text) + " from " + startOffset + " to " + endOffset);

            editableText.delete(startOffset, endOffset);
        }
    }

    public void deleteSpace(RecognitionTask.RecognizedInk ri, boolean isAbove) {

        final Rect bb = computeBoundingBox(ri.ink);
        Layout layout = view.getLayout();

        ArrayList<Integer> arr = getCharacterOffsetFromArch(layout, bb, isAbove);

        Integer startOffset = arr.get(0);
        Integer endOffset = arr.get(1);

        if (startOffset >= 0 && endOffset >= 0 && startOffset < endOffset) {
            Editable editableText = view.getText();

            char[] text = new char[endOffset - startOffset];
            editableText.getChars(startOffset, endOffset, text, 0);
            Log.d(TAG, "DeletingSpace: " + new String(text) + " from " + startOffset + " to " + endOffset);

            String textToProcess = editableText.subSequence(startOffset, endOffset).toString();
            String processedText = textToProcess.replaceAll("\\s", "");
            editableText.replace(startOffset, endOffset, processedText);
        }

    }

    private ArrayList<Integer> getCharacterOffsetFromArch(Layout layout, Rect rect, boolean isAbove) {
        ArrayList<Integer> offsets = new ArrayList<>();
        int start;
        int end;
        if (isAbove) {
            start = getCharacterOffsetFromPoint(layout, rect.left, rect.bottom);
            end = getCharacterOffsetFromPoint(layout, rect.right, rect.bottom);
        } else {
            start = getCharacterOffsetFromPoint(layout, rect.left, rect.top);
            end = getCharacterOffsetFromPoint(layout, rect.right, rect.top);
        }
        offsets.add(start);
        offsets.add(end);
        return offsets;
    }

    private ArrayList<Integer> getCharacterOffsetFromRect(Layout layout, Rect rect) {
        ArrayList<Integer> offsets = new ArrayList<>();
        int start = getCharacterOffsetFromPoint(layout, rect.left, rect.top);
        int end = getCharacterOffsetFromPoint(layout, rect.right, rect.bottom);
        offsets.add(start);
        offsets.add(end);
        return offsets;
    }

    private int getCharacterOffsetFromCart(Layout layout, Rect rect, boolean isAbove) {
        int offset;
        if (isAbove) {
            offset = getCharacterOffsetFromPoint(layout, rect.centerX(), rect.bottom);
        } else {
            offset = getCharacterOffsetFromPoint(layout, rect.centerX(), rect.top);
        }
        return offset;
    }

    private int getCharacterOffsetFromPoint(Layout layout, float x, float y) {
        if (layout != null) {
            int line = layout.getLineForVertical((int) y);
            return layout.getOffsetForHorizontal(line, (int) x);
        }
        return -1;
    }


    private Rect computeBoundingBox(Ink ink) {
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
        // Enforce a maximum size of the bounding box, to ensure Emoji characters get displayed
        // correctly
        if (bb.width() > MAX_BB_WIDTH) {
            bb.set(bb.centerX() - MAX_BB_WIDTH / 2, bb.top, bb.centerX() + MAX_BB_WIDTH / 2, bb.bottom);
        }
        if (bb.height() > MAX_BB_HEIGHT) {
            bb.set(bb.left, bb.centerY() - MAX_BB_HEIGHT / 2, bb.right, bb.centerY() + MAX_BB_HEIGHT / 2);
        }
        return bb;
    }
}
