package isi.dan.ms.pedidos.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pedido_counter")
public class PedidoCounter {
     @Id
    private String id = "pedido"; // Siempre será "pedido"
    private int seq; // Número secuencial

    public PedidoCounter() {}

    public PedidoCounter(int seq) {
        this.seq = seq;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
