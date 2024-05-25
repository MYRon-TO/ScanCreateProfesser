package model.filemanager;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncCallable;
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
    private FileManager(){
        throw new IllegalStateException("Utility class");
    }

    // TODO: 5/25/24 add function rename
    // TODO: 5/25/24 add function delete

    /**
     * Write content to a file.
     * @param context The context.
     * @param uri The uri of the file.
     * @param content The content to write.
     * @return True if the content was written successfully, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public static ListenableFuture<Boolean> write(Context context, Uri uri, String content) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        Callable<Boolean> task = () -> {
            try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {
                if(outputStream != null){
                    outputStream.write(content.getBytes());
                    return true;
                }
            }
            return false;
        };
        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    public static ListenableFuture<String> read(Context context, Uri uri) throws IOException {
         ContentResolver contentResolver = context.getContentResolver();

        Callable<String> callable = () -> {
            StringBuilder contentBuilder = new StringBuilder();
            try (InputStream inputStream = contentResolver.openInputStream(uri)) {
                if (inputStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        contentBuilder.append(line).append("\n");
                    }
                    return contentBuilder.toString();
                }
            }
            return null;
        };

        return Futures.submit(callable, MoreExecutors.directExecutor());
    }
}