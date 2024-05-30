package presenter.digitalink;

import android.util.Log;

import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;

import model.digitalinkmanager.DigitalInkModelManager;
import model.preference.PreferenceManager;

public class DigitalInkManger {
    private static volatile DigitalInkManger instance = null;
    private  final String TAG = "DigitalInkManger";

    private final DigitalInkModelManager digitalInkModelManager = DigitalInkModelManager.getInstance();
    private DigitalInkRecognitionModel model;
    private DigitalInkRecognizer recognizer;
    private DigitalInkRecognitionModelIdentifier modelIdentifier;

    public static DigitalInkManger getInstance() throws Exception {
        if (instance == null) {
            synchronized (DigitalInkManger.class) {
                if (instance == null) {
                    instance = new DigitalInkManger();
                }
            }
        }
        return instance;
    }

    private DigitalInkManger() throws Exception {

        String languageTag = PreferenceManager.getInstance().getPreferenceDigitalInkRecognitionModel();

        try {
            setModel(languageTag);
        } catch (Exception e) {
            Log.e(TAG, "Error setting model: " + e.getMessage());
            throw e;
        }
    }

    public void setModel(String languageTag) throws Exception{
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag);
        }catch (MlKitException e){
            Log.e(TAG, "No model found for the language tag: " + languageTag + " using default model en-US");
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
        }

        if (modelIdentifier == null){
            Log.e(TAG, "Model Identifier is null");
            throw new Exception("Model Identifier is null");
        }

        model = DigitalInkRecognitionModel.builder(modelIdentifier).build();
        recognizer = DigitalInkRecognition.getClient( DigitalInkRecognizerOptions.builder(model).build());
    };

    public DigitalInkRecognitionModel getModel() {
        return model;
    }

    public DigitalInkRecognizer getRecognizer() {
        return recognizer;
    }

    public DigitalInkRecognitionModelIdentifier getModelIdentifier() {
        return modelIdentifier;
    }
}