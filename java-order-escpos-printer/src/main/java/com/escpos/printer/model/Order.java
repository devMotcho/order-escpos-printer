package com.escpos.printer.model;

import java.math.BigDecimal;
import java.util.List;

public record Order(
    String orderId,
    BigDecimal totalPrice,
    String time,
    String type,
    Customer customer,
    List<OrderProduct> products
) {}
