package com.escpos.printer.settings;

public record Config(
    int maxAttempts, // n if max attempts until trigger stop
    int retryDelay, // seconds between attempts
    int lineWidth, // 80mm line width

    String baseUrl,
    String authUrl,
    String ordersUrl,
    String updateOrderUrl,
    String checkServerHealth,

    String printerIP,
    int printerPort,

    String username,
    String password
) {
}
