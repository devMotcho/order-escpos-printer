package com.escpos.printer;

import java.net.http.HttpClient;

import com.escpos.printer.authentication.AuthResponse;
import com.escpos.printer.authentication.AuthService;
import com.escpos.printer.authentication.RestAuthService;
import com.escpos.printer.settings.AppSettings;
import com.escpos.printer.settings.Config;

import tools.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        AppSettings settings = AppSettings.getInstance();
        Config config = settings.config();

        System.out.println(config);

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        String loginUrl = config.baseUrl() + config.authUrl();

        AuthService authService = new RestAuthService(client, mapper);

        try {
            AuthResponse auth = authService.login(loginUrl, config.username(), config.password());
            System.out.println("Authenticated! Token: " + auth.access());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}