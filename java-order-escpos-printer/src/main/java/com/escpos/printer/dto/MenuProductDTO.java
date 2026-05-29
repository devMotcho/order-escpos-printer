package com.escpos.printer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuProductDTO(
    @JsonProperty("name") String name,
    @JsonProperty("quantity") int quantity
) {}
