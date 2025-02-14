package isi.dan.ms.pedidos.modelo;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DetallePedido {
    
    @NotNull(message = "Debe indicarse el producto en el detalle")
    @Valid
    private Producto producto;

    @NotNull(message = "Debe indicarse la cantidad de producto a adquirir")
    @Positive
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private Float descuento;
    private BigDecimal precioFinal = BigDecimal.ZERO;
    
}
