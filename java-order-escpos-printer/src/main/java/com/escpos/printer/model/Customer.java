package com.escpos.printer.model;

public record Customer(
    String name,
    String email,
    String taxId,
    String fullAddress,
    String phone
) {}
