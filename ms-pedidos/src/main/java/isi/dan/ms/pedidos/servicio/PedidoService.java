package isi.dan.ms.pedidos.servicio;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
import isi.dan.ms.pedidos.exception.PedidoNotFoundException;
import isi.dan.ms.pedidos.exception.PedidosClienteNotFoundException;
import isi.dan.ms.pedidos.exception.StateChangeException;
import isi.dan.ms.pedidos.modelo.DetallePedido;
import isi.dan.ms.pedidos.modelo.EstadoPedido;
import isi.dan.ms.pedidos.modelo.HistorialEstado;
import isi.dan.ms.pedidos.modelo.Pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // --- 1) Asignar número de pedido y fecha de pedido ---
        pedido.setNumeroPedido(pedidoCounterService.getNextPedidoNumber());
        log.info("Creando pedido {}.", pedido.getNumeroPedido());
        pedido.setFecha(Instant.now());

        // --- 2) Calcular el monto total del pedido y el de cada línea de detalle del pedido ---
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

        // --- 3) Verificar si el cliente tiene el saldo suficiente para hacer el pedido ---
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

        // --- 4) Verificar si hay stock para los productos (en caso de que el pedido se haya ACEPTADO) ---
        if(estado == EstadoPedido.ACEPTADO){
            log.info("Se verifica si hay stock suficiente como para preparar el pedido...");
            if(hayStockSuficiente(pedido.getDetalle())){
                actualizarStock(pedido, false);
                pedido.setEstado(EstadoPedido.EN_PREPARACION);
                pedido.getEstados().add(asignarHistorialEstado(EstadoPedido.EN_PREPARACION, "Hay stock suficiente, por lo que se comienza a preparar el pedido."));
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

    @Transactional
    public Pedido updateEstado(String id, EstadoPedido estadoActual) throws PedidoNotFoundException, StateChangeException, JsonProcessingException {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new PedidoNotFoundException(id));
        EstadoPedido estadoAnterior = pedido.getEstado();
        log.info("Validando cambio de estado para el pedido {}: de {} a {}", pedido.getId(), estadoAnterior, estadoActual);
        
        // Se verifica que el cambio de estado sea válido
        if(cambioEstadoValido(estadoAnterior, estadoActual)){
            log.info("Cambio válido");

            if(estadoActual == EstadoPedido.CANCELADO){
                if(estadoAnterior == EstadoPedido.ACEPTADO){
                    // ACEPTADO -> CANCELADO
                    pedido.setEstado(estadoActual);
                    pedido.getEstados().add(asignarHistorialEstado(estadoActual, "El cliente confirma la cancelación del Pedido."));
                    log.info("Estado del Pedido: CANCELADO.");
                } else {
                    // EN_PREPARACION -> CANCELADO
                    log.info("Se devuelve el stock a los Productos del Pedido");
                    actualizarStock(pedido, true);
                    pedido.setEstado(estadoActual);
                    pedido.getEstados().add(asignarHistorialEstado(estadoActual, "El cliente confirma la cancelación del Pedido."));
                    log.info("Estado del Pedido: CANCELADO.");
                }
            } else{
                // EN_PREPARACION -> ENTREGADO
                pedido.setEstado(estadoActual);
                    pedido.getEstados().add(asignarHistorialEstado(estadoActual, "El cliente confirma la entrega del Pedido."));
                    log.info("Estado del Pedido: ENTREGADO.");
            }
            
            return pedidoRepository.save(pedido);

        } else{
            log.info("Cambio NO válido");
            throw new StateChangeException(String.format("Un Pedido no puede pasar de %s a %s.", estadoAnterior, estadoActual));
            //throw new StateChangeException("Un pedido no puede pasar de "+estadoAnterior+" a "+estadoActual+".");
        }
    }

    // Crea y devuelve un HistorialEstado con los datos indicados
    private HistorialEstado asignarHistorialEstado(EstadoPedido estado, String detalle){
        HistorialEstado historial = new HistorialEstado();
        historial.setEstado(estado);
        historial.setFechaEstado(Instant.now());
        historial.setDetalle(detalle);

        return historial;
    }

    // Llama a MS-PRODUCTOS para verificar si hay stock para todos los Productos dentro de la lista de detalles
    private boolean hayStockSuficiente(List<DetallePedido> detallesPedido) {
        Map<Long, Integer> productsToCheck = new HashMap<>();
        for (DetallePedido dp : detallesPedido) {
            Long productId = dp.getProducto().getId();
            Integer cantidad = dp.getCantidad();
            productsToCheck.put(productId, cantidad);
        }
        return (stockClientFeign.verificarStockSuficiente(productsToCheck));
    }

    // Se actualiza por medio de RabbitMQ y se pasa el Pedido a EN_PREPARACION
    @Transactional
    private void actualizarStock(Pedido pedido, boolean reponerStock) throws JsonProcessingException {
        log.info("Actualizando stock...");
        for (DetallePedido dp : pedido.getDetalle()) {
            StockUpdateDTO stockUpdate = new StockUpdateDTO();
            stockUpdate.setIdProducto(dp.getProducto().getId());
            stockUpdate.setCantidad(dp.getCantidad());
            stockUpdate.setReponerStock(reponerStock);

            String jsonMessage = objectMapper.writeValueAsString(stockUpdate);
            log.info("Enviando actualización de stock: {}", jsonMessage);
            rabbitTemplate.convertAndSend(RabbitMQConfig.STOCK_UPDATE_QUEUE, jsonMessage);
        }
    }

    // Verifica si el cambio de estado a realizar es válido
    private boolean cambioEstadoValido(EstadoPedido estadoAnterior, EstadoPedido estadoActual){
        /* Cambios válidos:
         * ACEPTADO -> CANCELADO
         * EN_PREPARACION -> ENTREGADO, CANCELADO
         */
        switch (estadoAnterior) {
            case EstadoPedido.ACEPTADO:
                return estadoActual == EstadoPedido.CANCELADO;
            case EstadoPedido.EN_PREPARACION:
                return estadoActual == EstadoPedido.ENTREGADO || estadoActual == EstadoPedido.CANCELADO;
            default:
                return false;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_CHECK_QUEUE)
    public void checkStockUpdate(String jsonMessage) throws JsonProcessingException {
        log.info("Recibido mensaje de actualización de stock: {}", jsonMessage);
        StockUpdateDTO stockUpdate = objectMapper.readValue(jsonMessage, StockUpdateDTO.class);

        // Se obtienen los pedidos ACEPTADOS que contienen al Producto cuyo stock fue actualizado
        log.info("Buscando pedidos ACEPTADOS que contengan el producto {}", stockUpdate.getIdProducto());
        Long idProducto = stockUpdate.getIdProducto();
        List<Pedido> pedidosAceptados = pedidoRepository.findPedidosAceptadosByProductoId(idProducto);
        Integer stockDisponible = stockUpdate.getCantidad();

        for(Pedido pedido: pedidosAceptados){
            // Si hay stock para el pedido completo, se pasa de ACEPTADO a EN_PREPARACION
            if(hayStockSuficiente(pedido.getDetalle())){
                log.info("Hay stock suficiente para el pedido {}.", pedido.getId());
                actualizarStock(pedido, false);
                pedido.setEstado(EstadoPedido.EN_PREPARACION);
                pedido.getEstados().add(asignarHistorialEstado(EstadoPedido.EN_PREPARACION, "Hay stock suficiente, por lo que se comienza a preparar el pedido."));
                log.info("Estado del Pedido: EN_PREPARACION.");
                pedidoRepository.save(pedido);
                // Si todavía hay stock se continúa, sino finaliza
                DetallePedido detallePedido = pedido.getDetalle().stream()
                    .filter(dp -> dp.getProducto().getId().equals(idProducto))
                    .findFirst()
                    .orElse(null);

                stockDisponible -= detallePedido.getCantidad();
                
                if(stockDisponible == 0){
                    log.info("Stock agotado, no se revisan más pedidos");
                    return;
                }
            }
        }  
    }

    public String obtenerEstadoPedido(String id) throws PedidoNotFoundException {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
        return pedido.getEstado().name();
    }

    public List<Pedido> obtenerPedidosPorCliente(Integer idCliente) throws PedidosClienteNotFoundException {
        List<Pedido> pedidos = pedidoRepository.findByCliente_Id(idCliente);
        if (pedidos.isEmpty()) {
            log.info("No hay pedidos realizados por el cliente {}.", idCliente);
            throw new PedidosClienteNotFoundException(idCliente);
        }
        return pedidos;
    }

}
