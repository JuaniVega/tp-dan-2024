package isi.dan.ms.pedidos.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import feign.FeignException.FeignClientException;
import isi.dan.ms.pedidos.exception.PedidoNotFoundException;
import isi.dan.ms.pedidos.exception.PedidosClienteNotFoundException;
import isi.dan.ms.pedidos.exception.StateChangeException;
import isi.dan.ms.pedidos.modelo.EstadoPedido;
import isi.dan.ms.pedidos.modelo.Pedido;
import isi.dan.ms.pedidos.servicio.PedidoService;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    
    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<Pedido> createPedido(@RequestBody Pedido pedido) throws FeignClientException, JsonProcessingException {
        Pedido savedPedido = pedidoService.savePedido(pedido);
        return ResponseEntity.ok(savedPedido);
    }

    @GetMapping
    public List<Pedido> getAllPedidos() {
        return pedidoService.getAllPedidos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable String id) {
        Pedido pedido = pedidoService.getPedidoById(id);
        return pedido != null ? ResponseEntity.ok(pedido) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable String id) {
        pedidoService.deletePedido(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/actualizar-estado")
    public ResponseEntity<Pedido> updateEstado(@PathVariable String id, @RequestBody EstadoPedido estado) throws PedidoNotFoundException, StateChangeException, JsonProcessingException {
        Pedido pedido = pedidoService.updateEstado(id, estado);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/{id}/estado")
    public ResponseEntity<String> getEstadoPedido(@PathVariable String id) throws PedidoNotFoundException{
        String estado = pedidoService.obtenerEstadoPedido(id);
        return ResponseEntity.ok(estado);
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Pedido>> consultarPedidosPorCliente(@PathVariable Integer idCliente) throws PedidosClienteNotFoundException {
        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(idCliente);
        return ResponseEntity.ok(pedidos);
    }

	@GetMapping("/buscar")
	public ResponseEntity<List<Pedido>> buscarPedidos(@RequestParam(required = false) String clienteId,
			@RequestParam(required = false) String estado) {

		EstadoPedido estadoPedido = EstadoPedido.fromString(estado);
		List<Pedido> pedidos = pedidoService.buscarPedidos(clienteId, estadoPedido);
		return ResponseEntity.ok(pedidos);
	}

}