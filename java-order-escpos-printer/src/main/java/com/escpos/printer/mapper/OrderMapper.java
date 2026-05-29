package com.escpos.printer.mapper;

import com.escpos.printer.dto.CustomerDTO;
import com.escpos.printer.dto.OrderDTO;
import com.escpos.printer.dto.OrderProductDTO;
import com.escpos.printer.dto.ProductDTO;
import com.escpos.printer.model.Customer;
import com.escpos.printer.model.Order;
import com.escpos.printer.model.OrderProduct;
import com.escpos.printer.model.Product;

import java.util.stream.Collectors;

public class OrderMapper {
    public static Order toModel(OrderDTO dto) {
        if (dto == null) return null;
        return new Order(
            dto.orderId(),
            dto.totalPrice(),
            dto.time(),
            dto.type(),
            toModel(dto.customer()),
            dto.products() != null ? dto.products().stream().map(OrderMapper::toModel).collect(Collectors.toList()) : null
        );
    }

    public static Customer toModel(CustomerDTO dto) {
        if (dto == null) return null;
        return new Customer(
            dto.name(),
            dto.email(),
            dto.taxId(),
            dto.fullAddress(),
            dto.phone()
        );
    }

    public static OrderProduct toModel(OrderProductDTO dto) {
        if (dto == null) return null;
        return new OrderProduct(
            toModel(dto.product()),
            dto.quantity(),
            dto.price(),
            dto.pointsUsed(),
            dto.specialNotes()
        );
    }

    public static Product toModel(ProductDTO dto) {
        if (dto == null) return null;
        return new Product(
            dto.category(),
            dto.name()
        );
    }
}
