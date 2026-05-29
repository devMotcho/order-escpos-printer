package com.escpos.printer.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderProductDTO(
    ProductDTO product,
    int quantity,
    BigDecimal price,
    int pointsUsed,
    String specialNotes
) {}
