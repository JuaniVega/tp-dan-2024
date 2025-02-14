package isi.dan.ms.pedidos.modelo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Producto {

    @NotNull(message = "Debe indicarse el id del producto")
    private Long id;
    private String nombre;
    private String descripcion;

}
