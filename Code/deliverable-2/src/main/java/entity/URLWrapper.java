package entity;

import java.net.HttpURLConnection;
import java.net.URL;
public class URLWrapper {
    private final URL url;

    public URLWrapper(URL url) {
        this.url = url;
    }

    public HttpURLConnection openConnection() throws Exception {
        return (HttpURLConnection) url.openConnection();
    }
}
