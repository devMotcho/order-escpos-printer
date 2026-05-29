package com.escpos.printer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerDTO(
    String name,
    String email,
    String taxId,
    String fullAddress,
    String phone
) {}
