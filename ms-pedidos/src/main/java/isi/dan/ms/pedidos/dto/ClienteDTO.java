package isi.dan.ms.pedidos.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClienteDTO {

    private Integer id;
    private String nombre;
    private String correoElectronico;
    private String cuit;
    private BigDecimal maximoDescubierto;
    
}