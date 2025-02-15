package isi.dan.ms.pedidos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.ms.pedidos.dao.PedidoRepository;
import isi.dan.ms.pedidos.modelo.Cliente;
import isi.dan.ms.pedidos.modelo.DetallePedido;
import isi.dan.ms.pedidos.modelo.Pedido;
import isi.dan.ms.pedidos.modelo.Producto;
import isi.dan.ms.pedidos.servicio.PedidoCounterService;
import isi.dan.ms.pedidos.servicio.PedidoService;
import isi.dan.ms.pedidos.servicio.SaldoClientFeign;
import isi.dan.ms.pedidos.servicio.StockClientFeign;

@RunWith(SpringRunner.class)
@WebMvcTest(PedidoController.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private PedidoCounterService pedidoCounterService;

    @MockBean
    private SaldoClientFeign saldoClientFeign;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private StockClientFeign stockClientFeign;

    private Pedido pedido;
    private Cliente cliente;
    private DetallePedido detallePedido;
    private Producto producto;

    @BeforeEach
    void setup() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Cliente Test");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(BigDecimal.valueOf(100));
        producto.setDescuento(10f);

        detallePedido = new DetallePedido();
        detallePedido.setProducto(producto);
        detallePedido.setCantidad(2);

        pedido = new Pedido();
        pedido.setId("PedidoTest");
        pedido.setCliente(cliente);
        pedido.setDetalle(List.of(detallePedido));
        pedido.setTotal(BigDecimal.valueOf(100));
    }

    @Test
    public void testCreatePedido() throws Exception{
        Mockito.when(pedidoService.savePedido(Mockito.any(Pedido.class))).thenReturn(pedido);

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(pedido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PedidoTest"))
                .andExpect(jsonPath("$.total").value(180));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
