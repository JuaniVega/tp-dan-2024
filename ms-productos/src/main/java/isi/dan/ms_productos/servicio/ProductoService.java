package isi.dan.ms_productos.servicio;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
import jakarta.validation.Valid;

@Service
public class ProductoService {
	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private CategoriaService categoriaService;

	Logger log = LoggerFactory.getLogger(ProductoService.class);

	@RabbitListener(queues = RabbitMQConfig.STOCK_UPDATE_QUEUE)
	public void handleStockUpdate(Message msg) throws ProductoNotFoundException, CategoriaNotFoundException {
		log.info("Recibido {}", msg);
		String body = msg.getBody().toString();
		Long productId = Long.parseLong(body.split(";")[0]);
		Integer quantity = Integer.parseInt(body.split(";")[1]);

		Producto product = productoRepository.findById(productId)
				.orElseThrow(() -> new ProductoNotFoundException(productId));

		product.setStockActual(product.getStockActual() - quantity);

		if (product.getStockActual() < product.getStockMinimo()) {
			// TODO Generar pedido pendiente.
		}

		this.saveProducto(product);
	}

	public Producto saveProducto(Producto producto) throws CategoriaNotFoundException {
		Categoria categoria = categoriaService.findCategoriaById(producto.getCategoria().getId());
		producto.setCategoria(categoria);

		if (producto.getDescuento() == null) {
			producto.setDescuento(0f);
		}
		producto.setStockActual(0);
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

	public Producto putOrdenProvision(@Valid StockUpdateDTO ordenProvision) throws ProductoNotFoundException {
		Producto productoToUpdate = productoRepository.findById(ordenProvision.getIdProducto())
				.orElseThrow(() -> new ProductoNotFoundException(ordenProvision.getIdProducto()));

		productoToUpdate.setStockActual(productoToUpdate.getStockActual() + ordenProvision.getCantidad());
		productoToUpdate.setPrecio(ordenProvision.getPrecio());

		return productoRepository.save(productoToUpdate);
	}

	public Producto updateDescuento(@Valid DescuentoDto descuentoDto) throws ProductoNotFoundException {
		Producto productToUpdate = productoRepository.findById(descuentoDto.getIdProducto())
				.orElseThrow(() -> new ProductoNotFoundException(descuentoDto.getIdProducto()));

		productToUpdate.setDescuento(descuentoDto.getDescuento());

		return productoRepository.save(productToUpdate);
	}
}
