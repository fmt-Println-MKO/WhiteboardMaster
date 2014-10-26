package co.whiteboardmaster.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by matthiaskoch on 26.10.14.
 */
public class StreamUtils {

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        String body = "";

        StringBuilder sb = new StringBuilder();
        while ((body = rd.readLine()) != null) {
            sb.append(body);
            sb.append("\n");

        }
        return sb.toString();
    }
}
