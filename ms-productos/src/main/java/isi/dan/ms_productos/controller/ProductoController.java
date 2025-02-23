package isi.dan.ms_productos.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;

import isi.dan.ms_productos.aop.LogExecutionTime;
import isi.dan.ms_productos.dto.DescuentoDto;
import isi.dan.ms_productos.dto.StockUpdateDTO;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Producto;
import isi.dan.ms_productos.servicio.EchoClientFeign;
import isi.dan.ms_productos.servicio.ProductoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
	@Autowired
	private ProductoService productoService;

	Logger log = LoggerFactory.getLogger(ProductoController.class);

	@Autowired
	EchoClientFeign echoSvc;

	@PostMapping
	@LogExecutionTime
	public ResponseEntity<Producto> createProducto(@RequestBody @Valid Producto producto)
			throws CategoriaNotFoundException {
		Producto savedProducto = productoService.saveProducto(producto);
		return ResponseEntity.ok(savedProducto);
	}

	@GetMapping("/test")
	@LogExecutionTime
	public String getEcho() {
		String resultado = echoSvc.echo();
		log.info("Log en test 1!!!! {}", resultado);
		return resultado;
	}

	@GetMapping("/test2")
	@LogExecutionTime
	public String getEcho2() {
		RestTemplate restTemplate = new RestTemplate();
		String gatewayURL = "http://ms-gateway-svc:8080";
		String resultado = restTemplate.getForObject(gatewayURL + "/clientes/api/clientes/echo", String.class);
		log.info("Log en test 2 {}", resultado);
		return resultado;
	}

	@GetMapping
	@LogExecutionTime
	public List<Producto> getAllProductos() {
		return productoService.getAllProductos();
	}

	@GetMapping("/{id}")
	@LogExecutionTime
	public ResponseEntity<?> getProductoById(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(productoService.getProductoById(id));
		} catch (ProductoNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Producto no encontrado", "message", e.getMessage()));
		}
	}

	@GetMapping("/buscar")
	public List<Producto> buscarProductos(
	        @RequestParam(required = false) Long id,
	        @RequestParam(required = false) String nombre,
	        @RequestParam(required = false) BigDecimal precioMin,
	        @RequestParam(required = false) BigDecimal precioMax,
	        @RequestParam(required = false) Integer stockMin,
	        @RequestParam(required = false) Integer stockMax) {
	    return productoService.buscarProductos(id, nombre, precioMin, precioMax, stockMin, stockMax);
	}

	@DeleteMapping("/{id}")
	@LogExecutionTime
	public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
		productoService.deleteProducto(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/enter-orden")
	@LogExecutionTime
	public ResponseEntity<?> enterOrdenProvision(@RequestBody @Valid StockUpdateDTO ordenProvision)
			throws JsonProcessingException {
		Producto prod;
		try {
			prod = productoService.putOrdenProvision(ordenProvision);
			return ResponseEntity.ok(prod);
		} catch (ProductoNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Producto no encontrado", "message", e.getMessage()));
		}
	}

	@PutMapping("/descuento")
	@LogExecutionTime
	public ResponseEntity<?> updateDescuento(@RequestBody @Valid DescuentoDto descuentoDto) {
		Producto prod;
		try {
			prod = productoService.updateDescuento(descuentoDto);
			return ResponseEntity.ok(prod);
		} catch (ProductoNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Producto no encontrado", "message", e.getMessage()));
		}
	}

	@PostMapping("/stock-suficiente")
	@LogExecutionTime
	public ResponseEntity<Boolean> verificarStockSuficiente(@RequestBody Map<Long, Integer> productsToCheck) {
		try {
			boolean stockSuficiente = productoService.verificarStockSuficiente(productsToCheck);
			return ResponseEntity.ok(stockSuficiente);
		} catch (ProductoNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
		}
	}

}
