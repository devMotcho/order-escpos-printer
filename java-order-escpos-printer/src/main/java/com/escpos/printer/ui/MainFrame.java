package com.escpos.printer.ui;

import com.escpos.printer.alert.AlertManager;
import com.escpos.printer.service.PollingService;
import com.escpos.printer.service.ServiceStateListener;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame implements ServiceStateListener {
    private final PollingService pollingService;
    private final AlertManager alertManager;

    private JLabel statusLabel;
    private JTextArea logArea;
    private JButton restartBtn;
    private JButton stopAlertBtn;
    private JButton exitBtn;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public MainFrame(PollingService pollingService, AlertManager alertManager) {
        this.pollingService = pollingService;
        this.alertManager = alertManager;
        initUI();
    }

    private void initUI() {
        setTitle("Sistema de Impressão Rodizio");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        try {
            URL iconUrl = getClass().getResource("/icon_nobg.png");
            if (iconUrl != null) {
                Image icon = Toolkit.getDefaultToolkit().getImage(iconUrl);
                setIconImage(icon);
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar taskbar = Taskbar.getTaskbar();
                    if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        taskbar.setIconImage(icon);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao carregar icone.");
        }

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
        restartBtn = new JButton("Reiniciar Serviço");
        stopAlertBtn = new JButton("Parar Alarme");
        stopAlertBtn.setEnabled(false);
        exitBtn = new JButton("Sair");

        buttonPanel.add(restartBtn);
        buttonPanel.add(stopAlertBtn);
        buttonPanel.add(exitBtn);
        add(buttonPanel, BorderLayout.CENTER);

        // Logs area (always visible)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(380, 200));
        add(scrollPane, BorderLayout.SOUTH);

        // Event listeners

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
