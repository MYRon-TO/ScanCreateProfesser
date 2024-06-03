package model;

import android.net.Uri;

public class UriStringConverters {
    public UriStringConverters() {
    }

    public static Uri uriFromString(String value) {
        return value == null ? null : Uri.parse(value);
    }

    public static String stringFromUri(Uri uri) {
        return uri == null ? null : uri.toString();
    }

}
