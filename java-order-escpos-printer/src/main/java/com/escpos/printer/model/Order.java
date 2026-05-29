package com.escpos.printer.model;

import java.math.BigDecimal;
import java.util.List;

public record Order(
    String id,
    String customerName,
    String email,
    String nif,
    String fullAddress,
    String phoneNumber,
    String deliveryTime,
    String created,
    List<OrderProduct> orderProducts,
    BigDecimal totalPrice,
    boolean printed,
    String localityName,
    String indication
) {}
