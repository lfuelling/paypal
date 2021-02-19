package de.micromata.paypal.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Handles all the https requests including the pooling and re-usage of connections.
 */
public class HttpsClient {
    public enum Mode {POST, GET, PATCH}

    private static Logger log = LoggerFactory.getLogger(HttpsClient.class);

    private String url;
    private Mode mode;
    private String authorization;
    private String acceptLanguage;
    private MimeType contentType;
    private MimeType accept;
    private boolean keepAlive;

    /**
     * It doesn't matter if you re-use this class for same urls or if you create new objects of this class.
     * The underlying {@link URL#openConnection()} does the connection pooling and re-usage for us.
     * @param url Url for the request, e. g. "https://api.sandbox.paypal.com/v1/payments/payment".
     * @param mode {@link Mode#POST} {@link Mode#GET}
     */
    public HttpsClient(String url, Mode mode) {
        this.url = url;
        this.mode = mode;
    }

    /**
     * Executes the https call. Use this method only for GET calls (GET calls don't have a body).
     * @return The server's response.
     * @throws IOException If any IOException occurs while connecting the server.
     */
    public String send() throws IOException {
        return send(null);
    }

    /**
     * Executes the https call. Use this method only for POST calls (POST calls need a body).
     * @param body The body as post input.
     * @return The server's response.
     * @throws IOException If any IOException occurs while connecting the server.
     */
    public String send(String body) throws IOException {
        if (body == null && mode == Mode.POST) {
            throw new IllegalArgumentException("body can't be null for POST calls.");
        } else if (body == null && mode == Mode.PATCH) {
            throw new IllegalArgumentException("body can't be null for PATCH calls.");
        } else if (body != null && mode == Mode.GET) {
            throw new IllegalArgumentException("body must be null for GET calls.");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = getRequest(body);

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if(res.statusCode() != 200 && res.statusCode() != 201) {
                throw new RuntimeException("Failed! Response code: " + res.statusCode());
            }
            return res.body();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error! Interrupted!", e);
        }
    }

    /**
     * @param accessToken Sets authorization to "Bearer &lt;accessToken&gt;"
     * @return this for chaining.
     */
    public HttpsClient setBearerAuthorization(String accessToken) {
        if (log.isDebugEnabled()) {
            log.debug("Authorization: Bearer " + accessToken);
        }
        this.authorization = "Bearer " + accessToken;
        return this;
    }

    /**
     * @param usernamePassword &lt;username&gt;:&lt;password&gt;
     * @return this for chaining.
     */
    public HttpsClient setUserPasswordAuthorization(String usernamePassword) {
        if (log.isDebugEnabled()) {
            if (usernamePassword.length() < 10) {
                log.debug("Authorization: TO-SHORT?: " + usernamePassword.length());
            }
            log.debug("Authorization: Basic " + usernamePassword.substring(0, 3) + "...:..." + usernamePassword.substring(usernamePassword.length() - 3));
        }
        this.authorization = "Basic " + new String(Base64.getEncoder().encode(usernamePassword.getBytes()));
        return this;
    }

    public HttpsClient setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    public HttpsClient setContentType(MimeType contentType) {
        this.contentType = contentType;
        return this;
    }

    public HttpsClient setAccept(MimeType accept) {
        this.accept = accept;
        return this;
    }

    public HttpsClient setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    private HttpRequest getRequest(String body) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url));
        if(mode == Mode.GET) {
            requestBuilder.method(mode.name(), HttpRequest.BodyPublishers.noBody());
        } else {
            requestBuilder.method(mode.name(), HttpRequest.BodyPublishers.ofString(body));
        }
        if (this.keepAlive) {
            requestBuilder.header("keep-alive", "true");
        }
        if (authorization != null) {
            requestBuilder.header("Authorization", authorization);
        }
        if (acceptLanguage != null) {
            if (log.isDebugEnabled()) log.debug("Accept-Language: " + acceptLanguage);
            requestBuilder.header("Accept-Language", acceptLanguage);
        }
        if (contentType != null) {
            if (log.isDebugEnabled()) log.debug("Content-Type: " + contentType.asString());
            requestBuilder.header("Content-Type", contentType.asString());
        }
        if (accept != null) {
            if (log.isDebugEnabled()) log.debug("Accept: " + accept.asString());
            requestBuilder.header("Accept", accept.asString());
        }

        return requestBuilder.build();
    }

    private static final int BUFFER_SIZE = 4 * 1024;

    private void copy(final Reader input, final Writer output) throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
