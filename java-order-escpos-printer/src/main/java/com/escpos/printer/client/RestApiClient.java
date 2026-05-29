package com.escpos.printer.client;

import com.escpos.printer.authentication.SessionManager;
import com.escpos.printer.dto.OrderDTO;
import com.escpos.printer.mapper.OrderMapper;
import com.escpos.printer.model.Order;
import com.escpos.printer.settings.Config;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestApiClient implements ApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Config config;
    private final SessionManager sessionManager;

    public RestApiClient(HttpClient httpClient, ObjectMapper objectMapper, Config config, SessionManager sessionManager) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.config = config;
        this.sessionManager = sessionManager;
    }

    private HttpRequest.Builder createAuthorizedRequestBuilder(String url) {
        String token = sessionManager.getAccessToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    @Override
    public List<Order> fetchUnprintedOrders() throws Exception {
        String url = config.baseUrl() + config.ordersUrl();
        HttpRequest request = createAuthorizedRequestBuilder(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new Exception("Unauthorized. Please re-authenticate.");
        } else if (response.statusCode() != 200) {
            throw new Exception("Failed to fetch orders: " + response.body());
        }

        OrderDTO[] orderDTOs = objectMapper.readValue(response.body(), OrderDTO[].class);
        return Arrays.stream(orderDTOs)
                .map(OrderMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatusToPrinted(String orderId) throws Exception {
        String url = config.baseUrl() + config.updateOrderUrl().replace("{id}", orderId);
        HttpRequest request = createAuthorizedRequestBuilder(url)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new Exception("Failed to update order status: " + response.body());
        }
    }

    @Override
    public boolean checkHealth() throws Exception {
        String url = config.baseUrl() + config.checkServerHealth();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }
}
