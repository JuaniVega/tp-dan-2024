package isi.dan.ms.pedidos.dto;

import isi.dan.ms.pedidos.modelo.Cliente;
import lombok.Data;

@Data
public class ObraDTO {

    private Integer id;
    private String direccion;
    private Cliente cliente;

}