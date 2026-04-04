package com.escpos.printer.settings;

public record Config(
    String url, 
    int port, 
    String username,
    String password
) {
}
