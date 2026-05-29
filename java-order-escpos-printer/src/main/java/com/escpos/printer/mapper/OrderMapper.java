package com.escpos.printer.mapper;

import com.escpos.printer.dto.MenuProductDTO;
import com.escpos.printer.dto.OrderDTO;
import com.escpos.printer.dto.OrderProductDTO;
import com.escpos.printer.model.MenuProduct;
import com.escpos.printer.model.Order;
import com.escpos.printer.model.OrderProduct;

import java.util.stream.Collectors;

public class OrderMapper {
    public static Order toModel(OrderDTO dto) {
        if (dto == null) return null;
        return new Order(
            dto.id(),
            dto.customer(),
            dto.email(),
            dto.nif(),
            dto.fullAddress(),
            dto.phoneNumber(),
            dto.deliveryTime(),
            dto.created(),
            dto.orderProducts() != null ? dto.orderProducts().stream().map(OrderMapper::toModel).collect(Collectors.toList()) : null,
            dto.totalPrice(),
            dto.printed(),
            dto.localityName(),
            dto.indication()
        );
    }

    public static OrderProduct toModel(OrderProductDTO dto) {
        if (dto == null) return null;
        return new OrderProduct(
            dto.category(),
            dto.productName(),
            dto.productAccompaniment(),
            dto.quantity(),
            dto.note(),
            dto.price(),
            dto.menuProducts() != null ? dto.menuProducts().stream().map(OrderMapper::toModel).collect(Collectors.toList()) : null
        );
    }

    public static MenuProduct toModel(MenuProductDTO dto) {
        if (dto == null) return null;
        return new MenuProduct(
            dto.name(),
            dto.quantity()
        );
    }
}
