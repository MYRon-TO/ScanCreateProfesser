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

    // TODO: 5/25/24 add function rename
    // TODO: 5/25/24 add function delete

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
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(content.getBytes());
                outputStream.close();
                return true;
            } else {
                return false;
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
    public static ListenableFuture<String> read(Uri uri, Context context) throws IOException {

        Callable<String> task = () -> {
            StringBuilder stringBuilder = new StringBuilder();
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                return stringBuilder.toString();
            } else {
                return null;
            }
        };

        return Futures.submit(task, MoreExecutors.directExecutor());
    }
}