package isi.dan.ms.pedidos.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Float descuento;

}
