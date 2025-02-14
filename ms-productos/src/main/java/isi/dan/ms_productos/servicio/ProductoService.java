package isi.dan.ms_productos.servicio;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.ms_productos.conf.RabbitMQConfig;
import isi.dan.ms_productos.dao.ProductoRepository;
import isi.dan.ms_productos.dto.DescuentoDto;
import isi.dan.ms_productos.dto.StockUpdateDTO;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.modelo.Producto;

@Service
public class ProductoService {
	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private CategoriaService categoriaService;

	@Autowired
    private RabbitTemplate rabbitTemplate;

	Logger log = LoggerFactory.getLogger(ProductoService.class);

	@RabbitListener(queues = RabbitMQConfig.STOCK_UPDATE_QUEUE)
	public void handleStockUpdate(Message msg) throws ProductoNotFoundException, CategoriaNotFoundException {
		log.info("Recibido {}", msg);
		String body = new String(msg.getBody(), StandardCharsets.UTF_8);
		String[] parts = body.split(";");
		
		if (parts.length != 3) {
			log.error("Mensaje para RabbitMQ incorrecto: {}", body);
			return; 
		}

		Long productId = Long.parseLong(parts[0].trim());
        Integer quantity = Integer.parseInt(parts[1].trim());
        String pedidoId = parts[2].trim();

		// Se obtiene el producto en cuestión
		Producto product = productoRepository.findById(productId)
				.orElseThrow(() -> new ProductoNotFoundException(productId));

		// Verificar si hay suficiente stock
		boolean stockSuficiente = product.getStockActual() >= quantity;
		if (stockSuficiente) {
			// Al stock actual se le resta la cantidad pedida 
			product.setStockActual(product.getStockActual() - quantity);
			log.info("Stock actualizado para el producto {}: Nuevo stock = {}", productId, product.getStockActual());
			this.saveProducto(product);
		} else{
			log.warn("Stock insuficiente para el producto {}: Solicitado = {}, Disponible = {}",
				productId, quantity, product.getStockActual());
			return;
		}

		String responseMessage = pedidoId + ";" + productId + ";" + stockSuficiente;
        rabbitTemplate.convertAndSend(RabbitMQConfig.STOCK_RESPONSE_QUEUE, responseMessage);

	}

	public Producto saveProducto(Producto producto) throws CategoriaNotFoundException {
		Categoria categoria = categoriaService.findCategoriaById(producto.getCategoria().getId());
		producto.setCategoria(categoria);

		if (producto.getDescuento() == null) {
			producto.setDescuento(0f);
		}
		
		return productoRepository.save(producto);
	}

	public List<Producto> getAllProductos() {
		return productoRepository.findAll();
	}

	public Producto getProductoById(Long id) throws ProductoNotFoundException {
		return productoRepository.findById(id).orElseThrow(() -> new ProductoNotFoundException(id));
	}

	public void deleteProducto(Long id) {
		productoRepository.deleteById(id);
	}

	public Producto putOrdenProvision(StockUpdateDTO ordenProvision) throws ProductoNotFoundException {
		Producto productoToUpdate = productoRepository.findById(ordenProvision.getIdProducto())
				.orElseThrow(() -> new ProductoNotFoundException(ordenProvision.getIdProducto()));

		productoToUpdate.setStockActual(productoToUpdate.getStockActual() + ordenProvision.getCantidad());
		productoToUpdate.setPrecio(ordenProvision.getPrecio());

		return productoRepository.save(productoToUpdate);
	}

	public Producto updateDescuento(DescuentoDto descuentoDto) throws ProductoNotFoundException {
		Producto productToUpdate = productoRepository.findById(descuentoDto.getIdProducto())
				.orElseThrow(() -> new ProductoNotFoundException(descuentoDto.getIdProducto()));

		productToUpdate.setDescuento(descuentoDto.getDescuento());

		return productoRepository.save(productToUpdate);
	}
}
