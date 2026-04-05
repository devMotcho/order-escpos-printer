package com.escpos.printer.authentication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import tools.jackson.databind.ObjectMapper;

public class RestAuthService implements AuthService {
    private final HttpClient _httpClient;
    private final ObjectMapper _objectMapper;

    public RestAuthService(HttpClient httpClient, ObjectMapper objectMapper) {
        _httpClient = httpClient;
        _objectMapper = objectMapper;
    }

    public AuthResponse login(String loginUrl, String username, String password) throws Exception {
        String jsonBody = _objectMapper.writeValueAsString(Map.of(
            "username", username,
            "password", password
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(loginUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Login failed: " + response.body());
        }
        
        // Deserialize JSON to our Record
        return _objectMapper.readValue(response.body(), AuthResponse.class);
    }
}
