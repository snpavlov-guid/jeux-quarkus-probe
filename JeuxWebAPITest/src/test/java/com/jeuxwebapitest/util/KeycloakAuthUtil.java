package com.jeuxwebapitest.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.opentest4j.TestAbortedException;

public final class KeycloakAuthUtil {
    private static final String TOKEN_URL_KEY = "keycloak.auth.token-url";
    private static final String CLIENT_ID_KEY = "keycloak.auth.client-id";
    private static final String GRANT_TYPE_KEY = "keycloak.auth.grant-type";
    private static final String USERNAME_KEY = "keycloak.auth.username";
    private static final String PASSWORD_KEY = "keycloak.auth.password";

    private KeycloakAuthUtil() {
    }

    public static String getKeycloakAuthToken() {
        Config config = ConfigProvider.getConfig();
        String tokenUrl = config.getValue(TOKEN_URL_KEY, String.class);
        String clientId = config.getValue(CLIENT_ID_KEY, String.class);
        String grantType = config.getValue(GRANT_TYPE_KEY, String.class);
        String username = config.getValue(USERNAME_KEY, String.class);
        String password = config.getValue(PASSWORD_KEY, String.class);

        String form = buildFormBody(clientId, grantType, username, password);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUrl))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new TestAbortedException(
                    "Keycloak is unavailable for tests: HTTP "
                        + response.statusCode()
                        + " from "
                        + tokenUrl
                );
            }
            return extractAccessToken(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Keycloak token request was interrupted", ex);
        } catch (IOException ex) {
            throw new TestAbortedException(
                "Keycloak is unavailable for tests at " + tokenUrl + ", skipping secured endpoint tests",
                ex
            );
        }
    }

    private static String buildFormBody(String clientId, String grantType, String username, String password) {
        return "client_id=" + encode(clientId)
            + "&grant_type=" + encode(grantType)
            + "&username=" + encode(username)
            + "&password=" + encode(password);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String extractAccessToken(String jsonBody) {
        String marker = "\"access_token\":\"";
        int start = jsonBody.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Keycloak token response missing access_token");
        }
        int valueStart = start + marker.length();
        int valueEnd = jsonBody.indexOf('"', valueStart);
        if (valueEnd < 0) {
            throw new IllegalStateException("Keycloak token response has invalid access_token");
        }
        return jsonBody.substring(valueStart, valueEnd);
    }

}
