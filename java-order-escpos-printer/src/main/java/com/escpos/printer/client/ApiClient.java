package com.escpos.printer.client;

import com.escpos.printer.model.Order;
import java.util.List;

public interface ApiClient {
    List<Order> fetchUnprintedOrders() throws Exception;
    void updateOrderStatusToPrinted(String orderId) throws Exception;
    boolean checkHealth() throws Exception;
}
