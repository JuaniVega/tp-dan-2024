package isi.dan.ms.pedidos.modelo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Cliente {
    
    @NotNull(message = "Debe indicarse el id del cliente")
    private Integer id;
    private String nombre;
    private String correoElectronico;
    private String cuit;

}
