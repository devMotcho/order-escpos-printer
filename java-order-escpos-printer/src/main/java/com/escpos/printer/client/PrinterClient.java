package com.escpos.printer.client;

import com.escpos.printer.model.Order;

public interface PrinterClient {
    void printOrder(Order order) throws Exception;
    void checkConnection() throws Exception;
    void connect() throws Exception;
    void disconnect() throws Exception;
}
