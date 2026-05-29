package com.escpos.printer.model;

import java.math.BigDecimal;

public record OrderProduct(
    Product product,
    int quantity,
    BigDecimal price,
    int pointsUsed,
    String specialNotes
) {}
