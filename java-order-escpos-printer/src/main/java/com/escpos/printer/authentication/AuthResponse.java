package com.escpos.printer.authentication;

public record AuthResponse(
    String access,
    String refresh
) {

}
