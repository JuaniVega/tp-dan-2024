package isi.dan.ms_productos.servicio;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    Logger log = LoggerFactory.getLogger(ProductoService.class);

    @RabbitListener(queues = RabbitMQConfig.STOCK_UPDATE_QUEUE)
    public void handleStockUpdate(String jsonMessage) throws ProductoNotFoundException, CategoriaNotFoundException, JsonProcessingException {
        log.info("Recibido mensaje de actualización de stock: {}", jsonMessage);
        StockUpdateDTO stockUpdate = objectMapper.readValue(jsonMessage, StockUpdateDTO.class);
        Producto product = productoRepository.findById(stockUpdate.getIdProducto())
                .orElseThrow(() -> new ProductoNotFoundException(stockUpdate.getIdProducto()));

        Integer stockAnterior = product.getStockActual();

        if(stockUpdate.isReponerStock()){
            product.setStockActual(stockAnterior+stockUpdate.getCantidad());
        } else{
            product.setStockActual(stockAnterior-stockUpdate.getCantidad());
        }
        
        
        log.info("Stock actualizado: Producto {} | Stock anterior: {} | Stock nuevo: {}",
        stockUpdate.getIdProducto(), stockAnterior, product.getStockActual());
        this.saveProducto(product);
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

    public Producto putOrdenProvision(StockUpdateDTO ordenProvision) throws ProductoNotFoundException, JsonProcessingException {
        log.info("Actualizando producto {}", ordenProvision.getIdProducto());
        Producto productoToUpdate = productoRepository.findById(ordenProvision.getIdProducto())
                .orElseThrow(() -> new ProductoNotFoundException(ordenProvision.getIdProducto()));

        productoToUpdate.setStockActual(productoToUpdate.getStockActual() + ordenProvision.getCantidad());
        productoToUpdate.setPrecio(ordenProvision.getPrecio());

        productoRepository.save(productoToUpdate);

        // Se usa RabbitMQ para verificar si se puede pasar un Pedido de ACEPTADO a EN_PREPARACION
        ordenProvision.setCantidad(productoToUpdate.getStockActual());
        String jsonMessage = objectMapper.writeValueAsString(ordenProvision);
        log.info("Enviando actualización de stock: {}", jsonMessage);
        rabbitTemplate.convertAndSend(RabbitMQConfig.STOCK_CHECK_QUEUE, jsonMessage);

        return productoToUpdate;
    }

    public Producto updateDescuento(DescuentoDto descuentoDto) throws ProductoNotFoundException {
        Producto productToUpdate = productoRepository.findById(descuentoDto.getIdProducto())
                .orElseThrow(() -> new ProductoNotFoundException(descuentoDto.getIdProducto()));

        productToUpdate.setDescuento(descuentoDto.getDescuento());

        return productoRepository.save(productToUpdate);
    }

    public boolean verificarStockSuficiente(Map<Long, Integer> productsToCheck) throws ProductoNotFoundException{
        log.info("Revisando si hay stock suficiente para los productos indicados...");
        for (Map.Entry<Long, Integer> entry : productsToCheck.entrySet()) {
            Long idProducto = entry.getKey();
            Integer cantidadRequerida = entry.getValue();
            Producto producto = productoRepository.findById(idProducto).orElseThrow(() -> new ProductoNotFoundException(idProducto));
            if(producto.getStockActual() < cantidadRequerida){
                log.info("No hay stock suficiente para el producto {}: Stock actual = {}, Stock requerido = {}.", idProducto, producto.getStockActual(), cantidadRequerida);
                return false;
            }
        }
        log.info("Hay stock para todos los productos indicados.");
        return true;
    }

}
