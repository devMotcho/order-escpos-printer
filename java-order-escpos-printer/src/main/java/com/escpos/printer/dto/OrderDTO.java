package com.escpos.printer.dto;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDTO(
    @JsonProperty("id") String id,
    @JsonProperty("customer") String customer,
    @JsonProperty("email") String email,
    @JsonProperty("nif") String nif,
    @JsonProperty("full_address") String fullAddress,
    @JsonProperty("phone_number") String phoneNumber,
    @JsonProperty("delivery_time") String deliveryTime,
    @JsonProperty("created") String created,
    @JsonProperty("order_products") List<OrderProductDTO> orderProducts,
    @JsonProperty("total_price") BigDecimal totalPrice,
    @JsonProperty("printed") boolean printed,
    @JsonProperty("locality_name") String localityName,
    @JsonProperty("indication") String indication
) {}
