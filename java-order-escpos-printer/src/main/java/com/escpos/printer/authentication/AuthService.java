package com.escpos.printer.authentication;

public interface AuthService {
    AuthResponse login(String loginUrl, String username, String password) throws Exception;
}
