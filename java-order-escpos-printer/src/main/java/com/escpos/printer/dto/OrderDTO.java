package com.escpos.printer.dto;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDTO(
    String orderId,
    BigDecimal totalPrice,
    String time,
    String type,
    CustomerDTO customer,
    List<OrderProductDTO> products
) {}
