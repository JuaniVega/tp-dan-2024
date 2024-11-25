package isi.dan.ms.pedidos.modelo;

import java.time.Instant;
import lombok.Data;

@Data
public class HistorialEstado {

    private EstadoPedido estado;
    private Instant fechaEstado;
    private String userEstado;
    private String detalle; 

}
