package isi.dan.ms.pedidos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException.FeignClientException;
import isi.dan.ms.pedidos.exception.PedidoNotFoundException;
import isi.dan.ms.pedidos.exception.PedidosClienteNotFoundException;
import isi.dan.ms.pedidos.exception.StateChangeException;
import isi.dan.ms.pedidos.modelo.EstadoPedido;
import isi.dan.ms.pedidos.modelo.Pedido;
import isi.dan.ms.pedidos.servicio.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	@Autowired
	private PedidoService pedidoService;

	@Autowired
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping
	public ResponseEntity<Pedido> createPedido(@RequestBody Pedido pedido)
			throws FeignClientException, JsonProcessingException {
		Pedido savedPedido = pedidoService.savePedido(pedido);
		return ResponseEntity.ok(savedPedido);
	}

	@GetMapping
	public List<Pedido> getAllPedidos() {
		return pedidoService.getAllPedidos();
	}

	@GetMapping("/{numeroPedido}")
	public ResponseEntity<Pedido> getPedidoByNumeroPedido(@PathVariable Integer numeroPedido) {
		Pedido pedido = pedidoService.getPedidoByNumeroPedido(numeroPedido);
		return pedido != null ? ResponseEntity.ok(pedido) : ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePedido(@PathVariable String id) {
		pedidoService.deletePedido(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/actualizar-estado")
	public ResponseEntity<Pedido> updateEstado(@PathVariable Integer id, @RequestBody String estado)
			throws PedidoNotFoundException, StateChangeException, JsonProcessingException {

		String estadoString = objectMapper.readTree(estado).get("estado").asText();
		EstadoPedido estadoPedido = EstadoPedido.fromString(estadoString);
		Pedido pedido = pedidoService.updateEstado(id, estadoPedido);
		return ResponseEntity.ok(pedido);
	}

	@GetMapping("/{id}/estado")
	public ResponseEntity<String> getEstadoPedido(@PathVariable String id) throws PedidoNotFoundException {
		String estado = pedidoService.obtenerEstadoPedido(id);
		return ResponseEntity.ok(estado);
	}

	@GetMapping("/cliente/{idCliente}")
	public ResponseEntity<List<Pedido>> consultarPedidosPorCliente(@PathVariable Integer idCliente)
			throws PedidosClienteNotFoundException {
		List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(idCliente);
		return ResponseEntity.ok(pedidos);
	}

	@GetMapping("/buscar")
	public ResponseEntity<List<Pedido>> buscarPedidos(@RequestParam(required = false) Integer clienteId,
			@RequestParam(required = false) String estado) {

		EstadoPedido estadoPedido = EstadoPedido.fromString(estado);
		List<Pedido> pedidos = pedidoService.buscarPedidos(clienteId, estadoPedido);
		return ResponseEntity.ok(pedidos);
	}

}