package com.escpos.printer.client;

import com.escpos.printer.model.MenuProduct;
import com.escpos.printer.model.Order;
import com.escpos.printer.model.OrderProduct;
import com.escpos.printer.settings.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

public class EscPosPrinterClient implements PrinterClient {
    private final Config config;
    private Socket socket;
    private OutputStream out;

    public EscPosPrinterClient(Config config) {
        this.config = config;
    }

    @Override
    public void connect() throws Exception {
        if (socket == null || socket.isClosed()) {
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(config.printerIP(), config.printerPort()), 5000);
            socket.setSoTimeout(5000);
            out = socket.getOutputStream();
        }
    }

    @Override
    public void disconnect() throws Exception {
        if (out != null) {
            out.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        socket = null;
        out = null;
    }

    @Override
    public void checkConnection() throws Exception {
        try {
            connect();
        } finally {
            disconnect();
        }
    }

    @Override
    public void printOrder(Order order) throws Exception {
        try {
            connect();
            initPrinter();

            // Fast Info Header
            feedLines(1);
            alignCenter();
            setTextSize(2, 2); // Represents width=3, height=3 in python escpos (3x scale)
            boldOn();
            String fastInfoHeaderType = (order.fullAddress() != null && !order.fullAddress().isEmpty()) ? "ED" : "VB";
            String timeStr = extractTime(order.deliveryTime());
            printText(fastInfoHeaderType + " - " + timeStr + "\n");
            boldOff();
            setTextSize(0, 0);

            feedLines(1);

            // Order Header
            alignCenter();
            boldOn();
            printText("Pedido n." + order.id() + " Rodizio Ementa Digital\n");
            boldOff();

            feedLines(1);

            // Order Details
            alignLeft();
            boldOn();
            printText("Data e Hora da Entrega: " + formatPtPtDateTime(order.deliveryTime()) + "\n");
            boldOff();
            String nif = order.nif() != null ? order.nif().trim() : "";
            if (!nif.isEmpty() && !nif.equalsIgnoreCase("0") && !nif.matches("^0+$") && !nif.equalsIgnoreCase("N/D") && !nif.equalsIgnoreCase("null")) {
                printText("NIF: " + nif + "\n");
            }
            if (order.localityName() != null && !order.localityName().isEmpty()) {
                printText("Localidade de Entrega: " + order.localityName() + "\n");
            }
            if (order.fullAddress() != null && !order.fullAddress().isEmpty()) {
                printText("Morada: " + order.fullAddress() + "\n");
            }
            if (order.indication() != null && !order.indication().isEmpty()) {
                printText("Ponto de Referencia: " + order.indication() + "\n");
            }

            feedLines(1);

            // Customer Information
            alignCenter();
            boldOn();
            printText("Informacoes do Cliente\n");
            boldOff();
            alignLeft();
            printText("Cliente: " + (order.customerName() != null ? order.customerName() : "N/D") + "\n");
            if (order.email() != null && !order.email().isEmpty()) {
                printText("Email: " + order.email() + "\n");
            }
            printText("Tel.: " + (order.phoneNumber() != null ? order.phoneNumber() : "N/D") + "\n");

            feedLines(1);

            // Products List
            alignCenter();
            boldOn();
            printText("Produtos do Pedido:\n");
            boldOff();
            alignLeft();
            if (order.orderProducts() != null) {
                for (OrderProduct op : order.orderProducts()) {
                    String qtyName = op.quantity() + "x " + (op.productName() != null ? op.productName() : "Desconhecido");
                    String priceStr = op.price() + " EUR";
                    printProductLine(qtyName, priceStr);
                    if (op.note() != null && !op.note().trim().isEmpty()) {
                        printText("Nota do Pedido: " + op.note() + "\n");
                    }
                    if (op.menuProducts() != null && !op.menuProducts().isEmpty()) {
                        for (MenuProduct mp : op.menuProducts()) {
                            printText("  - " + mp.quantity() + "x " + mp.name() + "\n");
                        }
                    }
                    printText("\n");
                }
            }

            // Total
            alignCenter();
            setTextSize(1, 1); // Represents width=2, height=2 in python escpos (2x scale)
            boldOn();
            printText("TOTAL: " + order.totalPrice() + " EUR\n");
            boldOff();
            setTextSize(0, 0);
            alignLeft();
            
            // Footer
            feedLines(4);
            cutPaper();

            out.flush();
        } finally {
            disconnect();
        }
    }

    private java.time.ZonedDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return java.time.OffsetDateTime.parse(dateStr).atZoneSameInstant(java.time.ZoneId.of("Europe/Lisbon"));
        } catch (Exception e) {}
        try {
            return java.time.LocalDateTime.parse(dateStr).atZone(java.time.ZoneId.of("Europe/Lisbon"));
        } catch (Exception e) {}
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return java.time.LocalDateTime.parse(dateStr, fmt).atZone(java.time.ZoneId.of("Europe/Lisbon"));
        } catch (Exception e) {}
        return null;
    }

    private String formatPtPtDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("N/D")) return "N/D";
        java.time.ZonedDateTime zdt = parseDate(dateStr);
        if (zdt != null) {
            return zdt.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        }
        return dateStr;
    }

    private String extractTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("N/D")) return "N/D";
        java.time.ZonedDateTime zdt = parseDate(dateStr);
        if (zdt != null) {
            return zdt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        if (dateStr.contains("T")) {
            return dateStr.split("T")[1].split("\\.")[0].split("\\+")[0].split("Z")[0];
        }
        String[] parts = dateStr.split(" ");
        if (parts.length > 1) {
            return parts[1];
        }
        return dateStr;
    }

    private void printProductLine(String left, String right) throws IOException {
        int lineWidth = 32;
        if (left.length() + right.length() > lineWidth) {
            printText(left + "\n");
            int spaces = lineWidth - right.length();
            printText(" ".repeat(Math.max(0, spaces)) + right + "\n");
        } else {
            int spaces = lineWidth - left.length() - right.length();
            printText(left + " ".repeat(Math.max(0, spaces)) + right + "\n");
        }
    }

    private void printText(String text) throws IOException {
        out.write(text.getBytes(Charset.forName("IBM858")));
    }

    private void initPrinter() throws IOException {
        out.write(new byte[]{0x1B, 0x40}); // Initialize printer
        out.write(new byte[]{0x1B, 0x74, 0x13}); // Set character code table to CP858 (19)
    }

    private void alignLeft() throws IOException {
        out.write(new byte[]{0x1B, 0x61, 0x00});
    }

    private void alignCenter() throws IOException {
        out.write(new byte[]{0x1B, 0x61, 0x01});
    }

    private void alignRight() throws IOException {
        out.write(new byte[]{0x1B, 0x61, 0x02});
    }

    private void boldOn() throws IOException {
        out.write(new byte[]{0x1B, 0x45, 0x01});
    }

    private void boldOff() throws IOException {
        out.write(new byte[]{0x1B, 0x45, 0x00});
    }

    private void setTextSize(int widthMultiplier, int heightMultiplier) throws IOException {
        int size = (widthMultiplier << 4) | heightMultiplier;
        out.write(new byte[]{0x1D, 0x21, (byte) size});
    }

    private void feedLines(int n) throws IOException {
        out.write(new byte[]{0x1B, 0x64, (byte) n});
    }

    private void cutPaper() throws IOException {
        out.write(new byte[]{0x1D, 0x56, 0x41, 0x03});
    }
}
