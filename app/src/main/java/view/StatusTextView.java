package view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.scancreateprofessor.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
        Snackbar.make(view, this.strokeManager.getStatus(), Snackbar.LENGTH_SHORT).setAnchorView(R.id.note_layout_bottom_view).show();
    }

    public void setStrokeManager(StrokeManager strokeManager) {
        this.strokeManager = strokeManager;
    }
}
