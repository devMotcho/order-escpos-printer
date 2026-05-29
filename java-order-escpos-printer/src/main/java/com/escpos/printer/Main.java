package com.escpos.printer;

import com.escpos.printer.alert.AlertManager;
import com.escpos.printer.authentication.AuthService;
import com.escpos.printer.authentication.RestAuthService;
import com.escpos.printer.authentication.SessionManager;
import com.escpos.printer.client.ApiClient;
import com.escpos.printer.client.EscPosPrinterClient;
import com.escpos.printer.client.PrinterClient;
import com.escpos.printer.client.RestApiClient;
import com.escpos.printer.service.PollingService;
import com.escpos.printer.settings.AppSettings;
import com.escpos.printer.settings.Config;
import com.escpos.printer.ui.MainFrame;
import tools.jackson.databind.ObjectMapper;

import javax.swing.SwingUtilities;
import java.net.http.HttpClient;

public class Main {
    public static void main(String[] args) {
        // macOS specific properties for app name
        System.setProperty("apple.awt.application.name", "Sistema de Impressão Rodizio");
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // Load Configuration
        AppSettings settings = AppSettings.getInstance();
        Config config = settings.config();

        // Setup common dependencies
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        SessionManager sessionManager = SessionManager.getSession();

        // Setup Services and Clients
        AuthService authService = new RestAuthService(httpClient, objectMapper);
        ApiClient apiClient = new RestApiClient(httpClient, objectMapper, config, sessionManager);
        PrinterClient printerClient = new EscPosPrinterClient(config);

        // Setup Alert Manager
        AlertManager alertManager = new AlertManager();

        // Setup Polling Service
        PollingService pollingService = new PollingService(
                apiClient,
                printerClient,
                authService,
                config,
                sessionManager
        );

        // Start GUI
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(pollingService, alertManager);
            pollingService.addListener(mainFrame);
            mainFrame.setVisible(true);
            
            // Start the service
            pollingService.start();
        });
    }
}