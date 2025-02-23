package isi.dan.ms.pedidos.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import isi.dan.ms.pedidos.modelo.EstadoPedido;
import isi.dan.ms.pedidos.modelo.Pedido;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
     List<Pedido> findByCliente_Id(Integer clienteId);

     @Query("{ 'estado' : 'ACEPTADO', 'detalle.producto.id' : ?0 }")
    List<Pedido> findPedidosAceptadosByProductoId(Long idProducto);

     // Buscar por cliente
     List<Pedido> findByClienteId(String clienteId);

     // Buscar por estado
     List<Pedido> findByEstado(EstadoPedido estado);

     // Buscar por cliente y estado
     List<Pedido> findByClienteIdAndEstado(String clienteId, EstadoPedido estado);
}
