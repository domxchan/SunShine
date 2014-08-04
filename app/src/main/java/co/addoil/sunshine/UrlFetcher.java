package co.addoil.sunshine;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chandominic on 16/7/14.
 */
public class UrlFetcher {


    public byte[] getUrlBytes(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            int bytesRead = 0;

            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            if (connection != null)
                connection.disconnect();
            if (out != null)
                out.close();
        }
    }

    public String getUrlString(String urlString) throws IOException {
        StringBuffer sb = new StringBuffer();
        return new String(getUrlBytes(urlString));
    }
}
