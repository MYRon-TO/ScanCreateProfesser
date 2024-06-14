package model.filemanager;

import android.content.Context;
import android.net.Uri;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

/**
 * A utility class for managing files.
 */
public class FileManager {

    /**
     * Please do <strong>not</strong> instantiate this class.
     */
    private FileManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Write content to a file.
     * @param uri The uri of the file.
     * @param content The content to write.
     * @param context The context of the application.
     * @return A future that resolves to true if the write was successful, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public static ListenableFuture<Boolean> write(Uri uri, String content, Context context) throws IOException {
        Callable<Boolean> task = () -> {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri, "wt")) {
                if (outputStream != null) {
                    outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                    return true;
                } else {
                    return false;
                }
            }
        };
        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    /**
     * Read content from a file.
     * @param uri The uri of the file.
     * @param context The context of the application.
     * @return The content of the file.
     * @throws IOException If an I/O error occurs.
     */
    public static ListenableFuture<String> read(Uri uri, Context context) throws IOException {

        Callable<String> task = () -> {
            StringBuilder stringBuilder = new StringBuilder();
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                    }
                    // Remove the last added newline character, if desired
//                    if (stringBuilder.length() > 0) {
//                        stringBuilder.setLength(stringBuilder.length() - 1);
//                    }
                    return stringBuilder.toString();
                } else {
                    return null;
                }
            }
        };

        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    /**
     * Read content from a file.
     * @param uri The uri of the file.
     * @return The content of the file.
     * @throws IOException If an I/O error occurs.
     */
    public static ListenableFuture<String> preview(Uri uri, Context context, int NUM_OF_LINES) throws IOException {

        Callable<String> task = () -> {
            StringBuilder stringBuilder = new StringBuilder();
            try(InputStream inputStream = context.getContentResolver().openInputStream(uri)){
                if (inputStream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        int count = NUM_OF_LINES;
                        while ((line = reader.readLine()) != null && count > 0) {
                            stringBuilder.append(line).append("\n");
                            count -= 1;
                        }
                    }
//                    // Remove the last added newline character, if desired
//                    if (stringBuilder.length() > 0) {
//                        stringBuilder.setLength(stringBuilder.length() - 1);
//                    }
                    return stringBuilder.toString();
                } else {
                    return null;
                }
            }
        };

        return Futures.submit(task, MoreExecutors.directExecutor());
    }
}