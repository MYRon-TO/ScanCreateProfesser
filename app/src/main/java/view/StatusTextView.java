package view;

import android.view.View;

import com.example.scancreateprofessor.R;
import com.google.android.material.snackbar.Snackbar;

import presenter.digitalink.StrokeManager;

/**
 * Status bar for the test app.
 *
 * <p>It is updated upon status changes announced by the StrokeManager.
 */
public class StatusTextView implements StrokeManager.StatusChangedListener {

    private StrokeManager strokeManager;
    private final View view;

    public StatusTextView(View view) {
        this.view = view;
    }

    @Override
    public void onStatusChanged() {
        Snackbar.make(view, this.strokeManager.getStatus(), Snackbar.LENGTH_SHORT).setAnchorView(R.id.bottom_view_activity_note).show();
    }

    public void setStrokeManager(StrokeManager strokeManager) {
        this.strokeManager = strokeManager;
    }
}
