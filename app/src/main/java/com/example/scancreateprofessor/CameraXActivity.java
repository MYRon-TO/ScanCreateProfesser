package com.example.scancreateprofessor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import model.UriStringConverters;

public class CameraXActivity extends AppCompatActivity {
    private final static String TAG = "CameraXActivity";

    public ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    public PreviewView previewView;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_x);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.camerax_return_toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button takePhoto;
        takePhoto = findViewById(R.id.take_photo);
        previewView = findViewById(R.id.main_preview);
        takePhoto.setOnClickListener(
                v -> {
                    // 拍摄照片
                    imageCapture.takePicture(
                            ContextCompat.getMainExecutor(CameraXActivity.this),
                            new ImageCapture.OnImageCapturedCallback() {
                                @OptIn(markerClass = ExperimentalGetImage.class)
                                @Override
                                public void onCaptureSuccess(@NonNull ImageProxy image) {
                                    Image tempImage = image.getImage();
                                    if (tempImage == null) {
                                        throw new RuntimeException("image is null");
                                    }
                                    Bitmap bitmap = imageReaderToBitmap(tempImage);

                                    image.close();

                                    try {
                                        Uri imageUri = saveBitmapToGallery(bitmap);

                                        String fileUri = getIntent().getStringExtra("FileUri");
                                        String fileTitle = getIntent().getStringExtra("Title");

                                        Intent intent = new Intent(CameraXActivity.this, NoteActivity.class);
                                        intent.putExtra("ScanResult", UriStringConverters.stringFromUri(imageUri));
                                        intent.putExtra("FileUri", fileUri);
                                        intent.putExtra("Title", fileTitle);

                                        Log.i(TAG + "/takePicture", "image uri: " + UriStringConverters.stringFromUri(imageUri));
                                        Log.i(TAG + "/takePicture", "file uri: " + fileUri);
                                        Log.i(TAG + "/takePicture", "file title: " + fileTitle);

                                        startActivity(intent);
                                    } catch (IOException e) {
                                        Log.e(TAG + "/takePicture", e.toString());
                                        throw new RuntimeException(e);
                                    }

                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException exception) {
                                    // 处理错误
                                    Toast.makeText(CameraXActivity.this, "拍摄失败", Toast.LENGTH_SHORT).show();
                                }
                            });

                });

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }, getExecutor());

    }


    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

    }


    // 将 Bitmap 保存到相册
    // 将 Bitmap 保存到相册
    private Uri saveBitmapToGallery(Bitmap bitmap) throws IOException {


        String displayName = "Image_" + System.currentTimeMillis() + ".jpg";
        String mimeType = "image/jpeg";

        // 创建一个新的 ContentValues 实例
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);

        // 插入图片到MediaStore的Images库
        ContentResolver contentResolver = this.getContentResolver();
        Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (imageUri == null) {
            throw new RuntimeException("imageUri is null");
        }
        OutputStream outputStream = contentResolver.openOutputStream(imageUri);

        assert outputStream != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        Objects.requireNonNull(outputStream).close();

        // 通知系统相册有新图片
        MediaScannerConnection.scanFile(CameraXActivity.this, new String[]{imageUri.toString()}, null, null);

        return imageUri;
    }

    private Bitmap imageReaderToBitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    //返回上一个页面
    @Override
    public void onBackPressed() {
        super.onBackPressed(); // 调用父类的onBackPressed方法，执行默认的返回操作
    }
}