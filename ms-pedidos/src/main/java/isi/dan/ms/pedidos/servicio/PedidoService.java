package isi.dan.ms.pedidos.servicio;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import feign.FeignException;
import isi.dan.ms.pedidos.conf.RabbitMQConfig;
import isi.dan.ms.pedidos.dao.PedidoRepository;
import isi.dan.ms.pedidos.exception.ObraConDistintoClienteAsignadoException;
import isi.dan.ms.pedidos.exception.ObraSinClienteAsignadoException;
import isi.dan.ms.pedidos.exception.PedidoNotFoundException;
import isi.dan.ms.pedidos.mapper.ClienteMapper;
import isi.dan.ms.pedidos.mapper.ObraMapper;
import isi.dan.ms.pedidos.mapper.ProductoMapper;
import isi.dan.ms.pedidos.modelo.DetallePedido;
import isi.dan.ms.pedidos.modelo.EstadoPedido;
import isi.dan.ms.pedidos.modelo.HistorialEstado;
import isi.dan.ms.pedidos.dto.ClienteDTO;
import isi.dan.ms.pedidos.dto.ObraDTO;
import isi.dan.ms.pedidos.dto.ProductoDTO;
import isi.dan.ms.pedidos.modelo.Pedido;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PedidoService {
    
    @Autowired
    private ClienteClientFeign clienteClientFeign;

    @Autowired
    private ProductoClientFeign productoClientFeign;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoCounterService pedidoCounterService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ClienteMapper clienteMapper;

    @Autowired
    private ObraMapper obraMapper;

    @Autowired
    private ProductoMapper productoMapper;

    Logger log = LoggerFactory.getLogger(PedidoService.class);
    private final ConcurrentHashMap<String, Map<Long, Boolean>> stockStatus = new ConcurrentHashMap<>();

    @Transactional
    public Pedido savePedido(Pedido pedido) throws FeignException, ObraSinClienteAsignadoException, ObraConDistintoClienteAsignadoException, PedidoNotFoundException {
         
        Integer idCliente = pedido.getCliente().getId();
        log.info("Buscando cliente {}...", idCliente);
        ClienteDTO clienteDTO = clienteClientFeign.getClienteById(idCliente);
        pedido.setCliente(clienteMapper.transformarDTOEnCliente(clienteDTO));
        log.info("...cliente encontrado y asignado.");

         /* Igual que el paso anterior, pero con la obra */ 
        Integer idObra = pedido.getObra().getId();
        log.info("Buscando obra {}...", idObra);
        ObraDTO obraDTO = clienteClientFeign.getObraById(idObra);
        log.info("...obra encontrada.");

        /* Hay que verificar que la obra tiene asignada al cliente indicado anteriormente.
            Si tiene asignado otro cliente, o no tiene un cliente asignado, lanza una excepción */
        if(obraDTO.getCliente() != null){
            Integer idClienteObra = obraDTO.getCliente().getId();
            if(idClienteObra != idCliente){
                log.info("El cliente de la obra no coincide con el indicado.");
                throw new ObraConDistintoClienteAsignadoException(idCliente, idClienteObra);
            }
        } else{
            log.info("La obra no tiene un cliente asignado.");
            throw new ObraSinClienteAsignadoException();
        }

        pedido.setObra(obraMapper.transformarDTOEnObra(obraDTO));
        log.info("Obra asignada.");

        //2) Asignar número de pedido y fecha de pedido
        pedido.setNumeroPedido(pedidoCounterService.getNextPedidoNumber());
        pedido.setFecha(Instant.now());

        //3) Calcular monto total del pedido y el de cada línea de detalle del pedido
        /* Al igual que con cliente y obra, traigo los productos de ms-productos */
        ProductoDTO productoDTO;
        Float descuento;
        BigDecimal precioUnitario, totalProducto;
        BigDecimal totalPedido = BigDecimal.ZERO;
        for( DetallePedido dp : pedido.getDetalle()){
            productoDTO = productoClientFeign.getProductoById(dp.getProducto().getId());
            // Se asignan descuento, precio unitario y producto al detalle
            descuento = productoDTO.getDescuento();
            precioUnitario = productoDTO.getPrecio();
            dp.setDescuento(descuento);
            dp.setPrecioUnitario(precioUnitario);
            dp.setProducto(productoMapper.transformarDTOEnProducto(productoDTO));
            log.info("Producto {} asignado.", productoDTO.getId());
            
            /* Se agrega el total del producto, considerando la cantidad adquirida, al total del producto.
            Si hay un descuento, se aplica */ 
            totalProducto = precioUnitario.multiply(BigDecimal.valueOf(dp.getCantidad()));
            if(descuento != null){
                BigDecimal descuentoDecimal = BigDecimal.valueOf(descuento).divide(BigDecimal.valueOf(100));
                totalProducto = totalProducto.multiply(BigDecimal.ONE.subtract(descuentoDecimal));
            } 
            dp.setPrecioFinal(totalProducto);
            totalPedido = totalPedido.add(totalProducto);
        }
        pedido.setTotal(totalPedido);
        log.info("Total asignado: {}", totalPedido);


        //4) Verificar si el cliente tiene saldo suficiente
        List<Pedido> listaPedidosCliente = pedidoRepository.findByCliente_Id(idCliente);
        EstadoPedido estado = EstadoPedido.RECIBIDO;
        pedido.setEstado(estado);
        pedido.getEstados().add(asignarHistorialEstado(estado, "Pedido Recibido."));
        log.info("Pedido RECIBIDO");
        BigDecimal saldo = BigDecimal.ZERO;
        
        /* Si hay pedidos, para aquellos que esten ACEPTADOS o EN_PREPARACION se toma su total y se suma.
        Al finalizar se agrega el total del pedido que se está intentando crear, y se llama a una función que asigna el estado
        en base al máximo descubierto del cliente y el total calculado */
        if(listaPedidosCliente != null){
            for(Pedido p: listaPedidosCliente){
                estado = p.getEstado();
                if(estado == EstadoPedido.ACEPTADO || estado == EstadoPedido.EN_PREPARACION){
                    saldo =  saldo.add(p.getTotal());
                }
            }
            saldo = saldo.add(totalPedido);
            estado = asignarEstado(clienteDTO.getMaximoDescubierto(), saldo);
            pedido.setEstado(estado);
            if(estado == EstadoPedido.ACEPTADO){
                pedido.getEstados().add(asignarHistorialEstado(estado, "El cliente tiene el saldo suficiente para realizar el pedido."));
                log.info("Pedido ACEPTADO");
            } else{
                pedido.getEstados().add(asignarHistorialEstado(estado, "El cliente NO tiene el saldo suficiente para realizar el pedido."));
                log.info("Pedido RECHAZADO");
            }

        }

        // Guardar el pedido en la base de datos 
        pedidoRepository.save(pedido);

        //5) Verificar si hay stock de los productos
        if(estado == EstadoPedido.ACEPTADO){
            /*  Se inicializa el mapa donde se va a guardar, por cada producto del pedido, si hay stock para el mismo.
            Boolean representa si hay stock o no, y se inicializa como null para indicar que todavía no se asignó el valor. */
            Map<Long, Boolean> productosPendientes = pedido.getDetalle().stream()
                .collect(Collectors.toMap(dp -> dp.getProducto().getId(), dp -> false));
            stockStatus.put(pedido.getId(), productosPendientes);
            
            for( DetallePedido dp : pedido.getDetalle()){
                log.info("Enviando {}", dp.getProducto().getId()+";"+dp.getCantidad());
                rabbitTemplate.convertAndSend(RabbitMQConfig.STOCK_UPDATE_QUEUE, dp.getProducto().getId()+";"+dp.getCantidad()+";"+pedido.getId());
            }
        }
        
        // El pedido ya fue guardado anteriormente, lo cual es necesario para obtener en el paso 5 su ID
        return pedido;
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_RESPONSE_QUEUE)
    public void handleStockResponse(Message msg) throws PedidoNotFoundException{
        log.info("Recibido {}", msg);
        String body = new String(msg.getBody(), StandardCharsets.UTF_8);
        String[] parts = body.split(";");
        
        if (parts.length != 3) {
            log.error("Mensaje mal formado: {}", body);
            return; // Evita procesar mensajes incorrectos
        }

        String pedidoId = parts[0].trim();
        Long productId = Long.parseLong(parts[1].trim());;
        boolean stockSuficiente = Boolean.parseBoolean(parts[2].trim());;
    
        // Se actualiza el estado en memoria
        Map<Long, Boolean> productosPedido = stockStatus.get(pedidoId);
        productosPedido.put(productId, stockSuficiente);

        // Si hay stock para todos los productos, se cambia el estado del pedido a EN_PREPARACION
        if (productosPedido.values().stream().allMatch(value -> value == true)) {
            Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNotFoundException(pedidoId));
            
            pedido.setEstado(EstadoPedido.EN_PREPARACION);
            pedido.getEstados().add(asignarHistorialEstado(EstadoPedido.EN_PREPARACION, "Hay stock para preparar el pedido."));
            log.info("Pedido {} EN_PREPARACION.", pedidoId);
            pedidoRepository.save(pedido);
            stockStatus.remove(pedidoId);
        } else {
            log.warn("Pedido {} sigue ACEPTADO por falta de stock.", pedidoId);
        }
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

    public EstadoPedido asignarEstado(BigDecimal maxDescubierto, BigDecimal saldo){
        EstadoPedido estado;
        if(saldo.compareTo(maxDescubierto) <= 0){
            estado = EstadoPedido.ACEPTADO;
        } else{
            estado = EstadoPedido.RECHAZADO;
        }
        return estado;
    }

    public HistorialEstado asignarHistorialEstado(EstadoPedido estado, String detalle){
        HistorialEstado historial = new HistorialEstado();
        historial.setEstado(estado);
        historial.setFechaEstado(Instant.now());
        historial.setDetalle(detalle);

        return historial;
    }
}
