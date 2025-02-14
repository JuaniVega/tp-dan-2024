package isi.dan.ms.pedidos.modelo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Obra {

    @NotNull(message = "Debe indicarse el id de la obra")
    private Integer id;
    private String direccion;

}
