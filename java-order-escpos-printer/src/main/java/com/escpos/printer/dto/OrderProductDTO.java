package com.escpos.printer.dto;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderProductDTO(
    @JsonProperty("category") String category,
    @JsonProperty("product_name") String productName,
    @JsonProperty("product_accompaniment") String productAccompaniment,
    @JsonProperty("quantity") int quantity,
    @JsonProperty("note") String note,
    @JsonProperty("price") BigDecimal price,
    @JsonProperty("menu_products") List<MenuProductDTO> menuProducts
) {}
