package com.escpos.printer.ui;

import com.escpos.printer.alert.AlertManager;
import com.escpos.printer.service.PollingService;
import com.escpos.printer.service.ServiceStateListener;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame implements ServiceStateListener {
    private final PollingService pollingService;
    private final AlertManager alertManager;

    private JLabel statusLabel;
    private JTextArea logArea;
    private JButton toggleLogsBtn;
    private JButton restartBtn;
    private JButton stopAlertBtn;
    private JButton exitBtn;
    
    private boolean logsVisible = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public MainFrame(PollingService pollingService, AlertManager alertManager) {
        this.pollingService = pollingService;
        this.alertManager = alertManager;
        initUI();
    }

    private void initUI() {
        setTitle("Serviço de Impressão ESC/POS");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Top panel for status
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statusLabel = new JLabel("Estado: A iniciar...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.LIGHT_GRAY);
        statusLabel.setPreferredSize(new Dimension(360, 40));
        topPanel.add(statusLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        toggleLogsBtn = new JButton("Mostrar Logs");
        restartBtn = new JButton("Reiniciar Serviço");
        stopAlertBtn = new JButton("Parar Alarme");
        stopAlertBtn.setEnabled(false);
        exitBtn = new JButton("Sair");

        buttonPanel.add(toggleLogsBtn);
        buttonPanel.add(restartBtn);
        buttonPanel.add(stopAlertBtn);
        buttonPanel.add(exitBtn);
        add(buttonPanel, BorderLayout.CENTER);

        // Logs area (hidden initially)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(380, 200));

        // Event listeners
        toggleLogsBtn.addActionListener(e -> {
            logsVisible = !logsVisible;
            if (logsVisible) {
                add(scrollPane, BorderLayout.SOUTH);
                setSize(400, 400);
                toggleLogsBtn.setText("Ocultar Logs");
            } else {
                remove(scrollPane);
                setSize(400, 150);
                toggleLogsBtn.setText("Mostrar Logs");
            }
            revalidate();
            repaint();
        });

        restartBtn.addActionListener(e -> {
            logMessage("A reiniciar o serviço...");
            pollingService.stop();
            alertManager.stopAlert();
            stopAlertBtn.setEnabled(false);
            pollingService.start();
        });

        stopAlertBtn.addActionListener(e -> {
            alertManager.stopAlert();
            stopAlertBtn.setEnabled(false);
            logMessage("Alarme parado pelo utilizador.");
        });

        exitBtn.addActionListener(e -> {
            pollingService.stop();
            alertManager.stopAlert();
            System.exit(0);
        });
        
        setLocationRelativeTo(null);
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = dateFormat.format(new Date());
            logArea.append("[" + time + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void onStateChanged(String state, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Estado: " + state);
            if (isError) {
                statusLabel.setBackground(new Color(255, 100, 100)); // Red
            } else {
                statusLabel.setBackground(new Color(100, 255, 100)); // Green
            }
        });
    }

    @Override
    public void onLogMessage(String message) {
        logMessage(message);
    }

    @Override
    public void onCriticalFailure() {
        SwingUtilities.invokeLater(() -> {
            stopAlertBtn.setEnabled(true);
            alertManager.startAlert();
        });
    }
}
