package presenter;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

import model.preference.PreferenceManager;

public class TextAnalysis {

    private static final String TEXT_RECOGNITION_LATIN = "Latin";
    private static final String TEXT_RECOGNITION_CHINESE = "Chinese";
    private static final String TEXT_RECOGNITION_DEVANAGARI = "Devanagari";
    private static final String TEXT_RECOGNITION_JAPANESE = "Japanese";
    private static final String TEXT_RECOGNITION_KOREAN = "Korean";
    public TextRecognizer recognizer;
    public String text;

    //创建TextRecognizer的实例
    private void createTextRecognizer(String selectedMode) {

        try {
            switch (selectedMode) {

                case TEXT_RECOGNITION_CHINESE:
                    recognizer =
                            TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
                    break;
                case TEXT_RECOGNITION_LATIN:
                    recognizer =
                            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                    break;

                case TEXT_RECOGNITION_JAPANESE:
                    recognizer =
                            TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
                    break;
                case TEXT_RECOGNITION_KOREAN:
                    recognizer =
                            TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
                    break;
                case TEXT_RECOGNITION_DEVANAGARI:
                    recognizer =
                            TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
                    break;
                default:
                    Log.e("识别器语言选择出错", "Unknown selectedMode: " + selectedMode);
            }
        } catch (Exception e) {
            Log.e("创建识别器出错", "Can not create TextRecognizer " + selectedMode, e);
            Toast.makeText(
                            getApplicationContext(),
                            "Can not create TextRecognizer: " + e.getMessage(),
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    //用imageUri识别文字
    public Task<Text> imageUriTextAnalyzer(Uri imageUri, Context context) throws IOException {
        //获取图片
        InputImage image;
        try {
            image = InputImage.fromFilePath(context, imageUri);
        } catch (IOException e) {
            Log.e("图片路径错误", "Can not create InputImage from URI: " + imageUri, e);
            throw new IOException(e);
        }
        return imageTextAnalyzer(image);

    }

//    @OptIn(markerClass = ExperimentalGetImage.class)
//    public Task<Text> imageProxyTextAnalyzer(ImageProxy imageProxy) {
//        InputImage image = null;
//
//        Image mediaImage = imageProxy.getImage();
//        if (mediaImage != null) {
//            image =
//                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
//        }
//
//        return imageTextAnalyzer(image);
//    }

    //用image识别文本
    private Task<Text> imageTextAnalyzer(InputImage image) {

        createTextRecognizer(PreferenceManager.getInstance().getPreferenceTextRecognizerModel());
        //获取图片中的文字
        return recognizer.process(image)
                .addOnSuccessListener(
                        visionText -> {
                            // Task completed successfully
                            text = visionText.getText();
                        }
                )
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                        }
                );
    }


}
