package com.escpos.printer.client;

import com.escpos.printer.model.Order;
import com.escpos.printer.model.OrderProduct;
import com.escpos.printer.settings.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

            // Header
            alignCenter();
            setTextSize(1, 1);
            boldOn();
            printText("NEW ORDER - " + order.type() + "\n");
            boldOff();
            setTextSize(0, 0);
            printText("Order ID: " + order.orderId() + "\n");
            printText("Time: " + order.time() + "\n");
            printText("--------------------------------\n");
            
            // Customer
            alignLeft();
            boldOn();
            printText("Customer Details:\n");
            boldOff();
            if (order.customer() != null) {
                printText("Name: " + order.customer().name() + "\n");
                printText("Phone: " + order.customer().phone() + "\n");
                if (order.customer().fullAddress() != null && !order.customer().fullAddress().isEmpty()) {
                    printText("Address: " + order.customer().fullAddress() + "\n");
                }
            }
            printText("--------------------------------\n");

            // Products
            boldOn();
            printText("Items:\n");
            boldOff();
            if (order.products() != null) {
                for (OrderProduct op : order.products()) {
                    printText(op.quantity() + "x " + (op.product() != null ? op.product().name() : "Unknown") + "\n");
                    if (op.specialNotes() != null && !op.specialNotes().isEmpty()) {
                        printText("  Note: " + op.specialNotes() + "\n");
                    }
                    printText("  Price: $" + op.price() + "\n");
                }
            }
            printText("--------------------------------\n");

            // Total
            alignRight();
            setTextSize(1, 1);
            boldOn();
            printText("TOTAL: $" + order.totalPrice() + "\n");
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

    private void printText(String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }

    private void initPrinter() throws IOException {
        out.write(new byte[]{0x1B, 0x40});
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
