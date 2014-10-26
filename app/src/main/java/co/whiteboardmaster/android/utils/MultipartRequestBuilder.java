package co.whiteboardmaster.android.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by matthiaskoch on 26.10.14.
 */
public class MultipartRequestBuilder {

    final String twoHyphens = "--";
    final String boundary;
    final String lineEnd = "\r\n";

    public MultipartRequestBuilder(String boundary) {
        this.boundary = boundary;
    }

    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2 * 1024 * 1024);


    public void addTextPart(String name, String value) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(twoHyphens);
        sb.append(boundary);
        sb.append(lineEnd);
        sb.append("Content-Disposition: form-data; name=\"");
        sb.append(name);
        sb.append("\"");
        sb.append(lineEnd);
        sb.append("Content-Type: text/plain");
        sb.append(lineEnd);
        sb.append(lineEnd);
        sb.append(value);
        sb.append(lineEnd);

        outputStream.write(stringToByteArray(sb.toString()));
    }

    public void addFilePart(String formName, String fileName, File file) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(twoHyphens);
        sb.append(boundary);
        sb.append(lineEnd);
        sb.append("Content-Disposition: form-data; name=\"");
        sb.append(formName);
        sb.append("\"; filename=\"");
        sb.append(fileName);
        sb.append("\"");
        sb.append(lineEnd);
        sb.append("Content-Type: image/jpeg");
        sb.append(lineEnd);
        sb.append("Content-Transfer-Encoding: binary");
        sb.append(lineEnd);
        sb.append(lineEnd);

        outputStream.write(stringToByteArray(sb.toString()));

        FileInputStream fileInputStream = new FileInputStream(file);

        int bytesAvailable = fileInputStream.available();
        int maxBufferSize = 1024 * 100;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
            outputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }
        outputStream.write(stringToByteArray(lineEnd));
    }

    public byte[] getRequest() throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(twoHyphens);
        sb.append(boundary);
        sb.append(twoHyphens);
        sb.append(lineEnd);

        outputStream.write(stringToByteArray(sb.toString()));
        return outputStream.toByteArray();
    }

    public void reset() {
        outputStream.reset();
    }

    private static byte[] stringToByteArray(String str) {
        byte[] bytes = new byte[str.length()];
        for (int index = 0; index < str.length(); index++) {
            bytes[index] = (byte) str.charAt(index);
        }
        return bytes;
    }
}
