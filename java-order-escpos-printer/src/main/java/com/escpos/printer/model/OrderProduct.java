package com.escpos.printer.model;

import java.math.BigDecimal;
import java.util.List;

public record OrderProduct(
    String category,
    String productName,
    String productAccompaniment,
    int quantity,
    String note,
    BigDecimal price,
    List<MenuProduct> menuProducts
) {}
