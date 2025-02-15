package isi.dan.ms.pedidos.servicio;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import isi.dan.ms.pedidos.conf.RabbitMQConfig;
import isi.dan.ms.pedidos.dao.PedidoRepository;
import isi.dan.ms.pedidos.dto.StockUpdateDTO;
import isi.dan.ms.pedidos.modelo.DetallePedido;
import isi.dan.ms.pedidos.modelo.EstadoPedido;
import isi.dan.ms.pedidos.modelo.HistorialEstado;
import isi.dan.ms.pedidos.modelo.Pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PedidoService {
    
    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PedidoCounterService pedidoCounterService;

    @Autowired
    private SaldoClientFeign saldoClientFeign;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockClientFeign stockClientFeign;

    Logger log = LoggerFactory.getLogger(PedidoService.class);

    @Transactional
    public Pedido savePedido(Pedido pedido) throws FeignException, JsonProcessingException {
        // 1) Asignar número de pedido y fecha de pedido
        pedido.setNumeroPedido(pedidoCounterService.getNextPedidoNumber());
        log.info("Creando pedido {}.", pedido.getNumeroPedido());
        pedido.setFecha(Instant.now());

        // 2) Calcular el monto total del pedido y el de cada línea de detalle del pedido
        log.info("Calculando el monto del pedido...");
        BigDecimal precio, totalProducto;
        BigDecimal totalPedido = BigDecimal.ZERO;
        Integer cantidad;
        Float descuento;
        for( DetallePedido dp : pedido.getDetalle()){
            // Se obtienen precio unitario y descuento del Producto, y la cantidad del DetallePedido
            precio = dp.getProducto().getPrecio();
            cantidad = dp.getCantidad();
            descuento = dp.getProducto().getDescuento();
            dp.setPrecioUnitario(precio);
            dp.setDescuento(descuento);
            // Se obtiene el total
            totalProducto = precio.multiply(BigDecimal.valueOf(cantidad));
            // Si hay un descuento, se aplica al total
            if(descuento != null && descuento>0){
                BigDecimal descuentoDecimal = BigDecimal.valueOf(descuento);
                totalProducto = totalProducto.multiply(BigDecimal.ONE.subtract(descuentoDecimal));
                totalProducto = totalProducto.setScale(2, RoundingMode.HALF_UP);
            }
            // Se setea el precio final de DetallePedido, y se agrega al total del Pedido
            dp.setPrecioFinal(totalProducto);
            totalPedido = totalPedido.add(totalProducto);
        }
        // Se setea el total del pedido
        totalPedido = totalPedido.setScale(2, RoundingMode.HALF_UP);
        pedido.setTotal(totalPedido);
        log.info("...Monto total = {}", totalPedido);

        // 3) Verificar si el cliente tiene el saldo suficiente para hacer el pedido
        EstadoPedido estado = EstadoPedido.RECIBIDO;
        pedido.getEstados().add(asignarHistorialEstado(estado, "Pedido Recibido."));
        log.info("Estado del Pedido: RECIBIDO");
        /* Si hay pedidos, para aquellos que esten ACEPTADOS o EN_PREPARACION se toma su total y se suma.
        Al finalizar se agrega el total del pedido que se está creando, y se llama a un endpoint rest que indica
        si el cliente tiene el saldo suficiente como para aceptar el nuevo pedido */
        Integer idCliente = pedido.getCliente().getId();
        List<Pedido> listaPedidosCliente = pedidoRepository.findByCliente_Id(idCliente);
        BigDecimal saldoRequerido = BigDecimal.ZERO;
        if(listaPedidosCliente != null){
            for(Pedido p: listaPedidosCliente){
                estado = p.getEstado();
                if(estado == EstadoPedido.ACEPTADO || estado == EstadoPedido.EN_PREPARACION){
                    saldoRequerido =  saldoRequerido.add(p.getTotal());
                }
            }
        }
        // Si se tiene saldo, el pedido es ACEPTADO, sino es RECHAZADO
        saldoRequerido = saldoRequerido.add(totalPedido);
        if(saldoClientFeign.verificarSaldoSuficiente(idCliente, saldoRequerido)){
            estado = EstadoPedido.ACEPTADO;
            pedido.getEstados().add(asignarHistorialEstado(estado, "El cliente tiene el saldo suficiente para realizar el pedido."));
            log.info("Estado del Pedido: ACEPTADO.");
        } else{
            estado = EstadoPedido.RECHAZADO;
            pedido.getEstados().add(asignarHistorialEstado(estado, "El cliente NO tiene el saldo suficiente para realizar el pedido."));
            log.info("Estado del Pedido: RECHAZADO.");
        }
        // RECIBIDO, ACEPTADO o RECHAZADO
        pedido.setEstado(estado);

        // 4) Verificar si hay stock para los productos (en caso de que el pedido se haya ACEPTADO)
        if(estado == EstadoPedido.ACEPTADO){
            if(hayStockSuficiente(pedido.getDetalle())){
                for( DetallePedido dp : pedido.getDetalle()){
                    StockUpdateDTO stockUpdate = new StockUpdateDTO();
                    stockUpdate.setIdProducto(dp.getProducto().getId());
                    stockUpdate.setCantidad(dp.getCantidad());
                    String jsonMessage = objectMapper.writeValueAsString(stockUpdate);
                    log.info("Enviando actualización de stock: {}", jsonMessage);
                    rabbitTemplate.convertAndSend(RabbitMQConfig.STOCK_UPDATE_QUEUE, jsonMessage);
                    /*
                    log.info("Enviando {}", dp.getProducto().getId()+";"+dp.getCantidad());
                    rabbitTemplate.convertAndSend(RabbitMQConfig.STOCK_UPDATE_QUEUE, dp.getProducto().getId()+";"+dp.getCantidad());
                    */
                }
                estado = EstadoPedido.EN_PREPARACION;
                pedido.setEstado(estado);
                pedido.getEstados().add(asignarHistorialEstado(estado, "Hay stock suficiente, por lo que se comienza a preparar el pedido."));
                log.info("Estado del Pedido: EN_PREPARACION.");
            }
        }

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido getPedidoById(String id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public void deletePedido(String id) {
        pedidoRepository.deleteById(id);
    }

    public HistorialEstado asignarHistorialEstado(EstadoPedido estado, String detalle){
        HistorialEstado historial = new HistorialEstado();
        historial.setEstado(estado);
        historial.setFechaEstado(Instant.now());
        historial.setDetalle(detalle);

        return historial;
    }

    public boolean hayStockSuficiente(List<DetallePedido> detallesPedido) {
        for (DetallePedido dp : detallesPedido) {
            Long productId = dp.getProducto().getId();
            Integer cantidad = dp.getCantidad();
    
            boolean stockSuficiente = stockClientFeign.verificarStockSuficiente(productId, cantidad);
    
            if (!stockSuficiente) {
                log.info("Stock insuficiente para el producto: {}", productId);
                return false;
            }
        }
        return true;
    }
}
