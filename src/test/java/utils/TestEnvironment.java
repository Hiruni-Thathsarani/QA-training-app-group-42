package utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class TestEnvironment {

    public static boolean isReachable(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int code = conn.getResponseCode();
            return code > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private TestEnvironment() {}
}
