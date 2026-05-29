package com.escpos.printer.service;

import com.escpos.printer.authentication.AuthResponse;
import com.escpos.printer.authentication.AuthService;
import com.escpos.printer.authentication.SessionManager;
import com.escpos.printer.client.ApiClient;
import com.escpos.printer.client.PrinterClient;
import com.escpos.printer.model.Order;
import com.escpos.printer.settings.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PollingService {
    private final ApiClient apiClient;
    private final PrinterClient printerClient;
    private final AuthService authService;
    private final Config config;
    private final SessionManager sessionManager;
    private ScheduledExecutorService scheduler;
    private final List<ServiceStateListener> listeners = new ArrayList<>();
    
    private int consecutiveFailures = 0;

    public PollingService(ApiClient apiClient, PrinterClient printerClient, AuthService authService, Config config, SessionManager sessionManager) {
        this.apiClient = apiClient;
        this.printerClient = printerClient;
        this.authService = authService;
        this.config = config;
        this.sessionManager = sessionManager;
    }

    public void addListener(ServiceStateListener listener) {
        listeners.add(listener);
    }

    private void notifyState(String state, boolean isError) {
        for (ServiceStateListener l : listeners) {
            l.onStateChanged(state, isError);
        }
    }

    private void notifyLog(String message) {
        for (ServiceStateListener l : listeners) {
            l.onLogMessage(message);
        }
    }

    private void notifyCriticalFailure() {
        for (ServiceStateListener l : listeners) {
            l.onCriticalFailure();
        }
    }

    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        consecutiveFailures = 0;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(this::poll, 0, config.retryDelay(), TimeUnit.SECONDS);
        notifyState("Serviço Iniciado", false);
        notifyLog("Serviço de verificação iniciado.");
    }

    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        notifyState("Serviço Parado", true);
        notifyLog("Serviço de verificação parado.");
    }

    private void authenticate() throws Exception {
        String loginUrl = config.baseUrl() + config.authUrl();
        AuthResponse auth = authService.login(loginUrl, config.username(), config.password());
        sessionManager.updateSession(auth.access(), auth.refresh());
        notifyLog("Autenticação realizada com sucesso.");
    }

    private void poll() {
        try {
            if (sessionManager.getAccessToken() == null) {
                notifyLog("Sem token de acesso. A tentar autenticar...");
                authenticate();
            }

            if (!apiClient.checkHealth()) {
                throw new Exception("Falha na verificação de integridade da API.");
            }
            
            // Check printer connectivity even if there are no orders
            printerClient.checkConnection();

            List<Order> orders = apiClient.fetchUnprintedOrders();
            if (!orders.isEmpty()) {
                notifyLog("Foram encontradas " + orders.size() + " novas encomendas.");
            }

            for (Order order : orders) {
                notifyLog("A imprimir encomenda " + order.id() + "...");
                printerClient.printOrder(order);
                notifyLog("A atualizar o estado da encomenda " + order.id() + "...");
                apiClient.updateOrderStatusToPrinted(order.id());
                notifyLog("Encomenda " + order.id() + " processada com sucesso.");
            }

            // Reset failures on success
            consecutiveFailures = 0;
            notifyState("A funcionar sem problemas", false);

        } catch (Exception e) {
            consecutiveFailures++;
            String ptErrorMsg = getHumanReadableError(e);
            notifyLog("Erro: " + ptErrorMsg + " (Detalhe técnico: " + e.toString() + ")");
            notifyState("Erro: " + ptErrorMsg, true);
            
            // If it's a 401/403 (unauthorized), we might need to re-authenticate next time
            if (e.getMessage() != null && e.getMessage().contains("Unauthorized")) {
                sessionManager.updateSession(null, null); // Clear token to force re-auth
            }

            if (consecutiveFailures >= config.maxAttempts()) {
                notifyLog("Número máximo de tentativas (" + config.maxAttempts() + ") atingido. Falha crítica. O serviço foi interrompido.");
                notifyCriticalFailure();
                stop();
            }
        }
    }

    private String getHumanReadableError(Exception e) {
        String msg = e.toString().toLowerCase();
        
        if (msg.contains("unknownhost") || msg.contains("unresolvedaddress") || msg.contains("network is unreachable") || msg.contains("no route to host")) {
            return "Sem acesso à internet ou rede falhou. Verifique a sua ligação Wi-Fi/Cabo e se o computador tem internet.";
        } else if (msg.contains("connectexception") || msg.contains("sockettimeout")) {
            return "Não foi possível estabelecer ligação. Verifique se a impressora está ligada e conectada à rede.";
        } else if (msg.contains("unauthorized") || msg.contains("401") || msg.contains("403")) {
            return "Erro de autenticação. Verifique se as credenciais (username/password) estão corretas.";
        }
        
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }
        return "Erro inesperado ao comunicar com o servidor ou impressora.";
    }
}
