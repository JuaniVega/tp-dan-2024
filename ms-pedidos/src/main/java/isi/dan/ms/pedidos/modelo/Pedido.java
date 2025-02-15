package isi.dan.ms.pedidos.modelo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pedidos")
@Data
public class Pedido {
    @Id
    private String id;
    private Instant fecha;
    private Integer numeroPedido;
    private String usuario;
    private String observaciones;
    @NotNull(message = "Debe indicarse el cliente para el cual se realiza el pedido")
    @Valid
    private Cliente cliente;

    @NotNull(message = "Debe indicarse la obra para la cual se realiza el pedido")
    @Valid
    private Obra obra;

    @Field("estados")
    private List<HistorialEstado> estados = new ArrayList<>();
    private EstadoPedido estado;
    
    @Field("detalle")
    @NotNull(message = "Debe indicarse el detalle del pedido")
    @Size(min=1, message = "El pedido debe contar con al menos un detalle")
    @Valid
    private List<DetallePedido> detalle;
    
    private BigDecimal total;
}
