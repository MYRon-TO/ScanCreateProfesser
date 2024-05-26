package model.filemanager;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
     *
     * @param uri     The uri of the file.
     * @param content The content to write.
     * @return True if the content was written successfully, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public static ListenableFuture<Boolean> write(Uri uri, String content) throws IOException {
        Callable<Boolean> task = () -> {
            File file = new File(uri.getPath());
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(content);
                return true;
            } catch (IOException e) {
                Log.e("FileWrite", "Error writing file: " + e.getMessage());
                throw e;
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
    public static ListenableFuture<String> read(Uri uri) throws IOException {

        Callable<String> task = () -> {
            File file = new File(uri.getPath());
            StringBuilder contentBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
                return contentBuilder.toString();
            } catch (IOException e) {
                Log.e("FileRead", "Error reading file: " + e.getMessage());
                throw e;
            }
        };

        return Futures.submit(task, MoreExecutors.directExecutor());
    }
}