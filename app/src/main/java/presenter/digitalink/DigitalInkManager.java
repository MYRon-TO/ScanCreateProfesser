package presenter.digitalink;

import view.DrawingView;

public class DigitalInkManager {
   private static final String TAG = "DigitalInkManager";
   private final StrokeManager strokeManager = new StrokeManager();
   private final DrawingView drawingView;

    public DigitalInkManager(DrawingView drawingView) {
        this.drawingView = drawingView;
    }

    public StrokeManager getStrokeManager() {
      return strokeManager;
   }

   public void deleteModel(){
      strokeManager.deleteActiveModel();
   }

   public void clear(){
      strokeManager.reset();
      drawingView.clear();
   }

   public void recognize(){
      strokeManager.recognize();
   }

   public void download() {
      strokeManager.download();
   }

}
