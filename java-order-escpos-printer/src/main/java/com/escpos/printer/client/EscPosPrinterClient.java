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

            // Header
            alignCenter();
            setTextSize(1, 1);
            boldOn();
            printText("NOVA ENCOMENDA\n");
            boldOff();
            setTextSize(0, 0);
            printText("ID Encomenda: " + order.id() + "\n");
            printText("Criada: " + order.created() + "\n");
            printText("Hora Entrega: " + (order.deliveryTime() != null ? order.deliveryTime() : "N/D") + "\n");
            printText("--------------------------------\n");
            
            // Customer
            alignLeft();
            boldOn();
            printText("Detalhes do Cliente:\n");
            boldOff();
            printText("Nome: " + (order.customerName() != null ? order.customerName() : "N/D") + "\n");
            printText("Telemovel: " + (order.phoneNumber() != null ? order.phoneNumber() : "N/D") + "\n");
            if (order.nif() != null && !order.nif().isEmpty()) {
                printText("NIF: " + order.nif() + "\n");
            }
            if (order.fullAddress() != null && !order.fullAddress().isEmpty()) {
                printText("Morada: " + order.fullAddress() + "\n");
                if (order.localityName() != null && !order.localityName().isEmpty()) {
                    printText("Localidade: " + order.localityName() + "\n");
                }
            }
            printText("--------------------------------\n");

            // Products
            boldOn();
            printText("Artigos:\n");
            boldOff();
            if (order.orderProducts() != null) {
                for (OrderProduct op : order.orderProducts()) {
                    printText(op.quantity() + "x " + (op.productName() != null ? op.productName() : "Desconhecido") + "\n");
                    if (op.note() != null && !op.note().isEmpty()) {
                        printText("  Nota: " + op.note() + "\n");
                    }
                    if (op.menuProducts() != null && !op.menuProducts().isEmpty()) {
                        for (MenuProduct mp : op.menuProducts()) {
                            printText("  - " + mp.quantity() + "x " + mp.name() + "\n");
                        }
                    }
                    printText("  Preco: " + op.price() + " EUR\n");
                }
            }
            printText("--------------------------------\n");

            if (order.indication() != null && !order.indication().isEmpty()) {
                boldOn();
                printText("Indicacoes:\n");
                boldOff();
                printText(order.indication() + "\n");
                printText("--------------------------------\n");
            }

            // Total
            alignRight();
            setTextSize(1, 1);
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
