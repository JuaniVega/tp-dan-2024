package isi.dan.ms.pedidos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.ms.pedidos.dto.ClienteDTO;
import isi.dan.ms.pedidos.dto.ObraDTO;
import isi.dan.ms.pedidos.dto.ProductoDTO;
import isi.dan.ms.pedidos.modelo.Cliente;
import isi.dan.ms.pedidos.modelo.Obra;
import isi.dan.ms.pedidos.modelo.Pedido;
import isi.dan.ms.pedidos.modelo.Producto;
import isi.dan.ms.pedidos.servicio.ClienteClientFeign;
import isi.dan.ms.pedidos.servicio.PedidoService;
import isi.dan.ms.pedidos.servicio.ProductoClientFeign;

@WebMvcTest(PedidoController.class)
public class PedidoControllerTest {

    @Autowired
	private MockMvc mockMvc;
    
    @MockBean
	private PedidoService pedidoService;

    @MockBean
	private ClienteClientFeign clienteClientFeign;

    @MockBean
	private ProductoClientFeign productoClientFeign;

    private Pedido pedido;
    private ClienteDTO clienteDTO;
    private Cliente cliente;
    private ObraDTO obraDTO;
    private Obra obra;
    private ProductoDTO productoDTO;
    private Producto producto;

    @BeforeEach
    void setUp(){
        clienteDTO = new ClienteDTO();
        clienteDTO.setId(1);
        clienteDTO.setNombre("Cliente Test");

        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Cliente Test");
        
        obraDTO = new ObraDTO();
        obraDTO.setId(1);
        obraDTO.setDireccion("Direccion Test");

        obra = new Obra();
        obra.setId(1);
        obra.setDireccion("Direccion Test");
        
        productoDTO = new ProductoDTO();
        productoDTO.setId(1L);
        productoDTO.setNombre("Producto Test");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
    
        pedido = new Pedido();
        pedido.setId("PedidoID");
        pedido.setCliente(cliente);
        pedido.setObra(obra);
        pedido.setTotal(BigDecimal.valueOf(500));
    }

    @Test
    public void testCreatePedido() throws Exception{
        Mockito.when(clienteClientFeign.getClienteById(Mockito.anyInt())).thenReturn(clienteDTO);
        Mockito.when(clienteClientFeign.getObraById(Mockito.anyInt())).thenReturn(obraDTO);
        Mockito.when(productoClientFeign.getProductoById(Mockito.anyLong())).thenReturn(productoDTO);
        Mockito.when(pedidoService.savePedido(Mockito.any(Pedido.class)))
        .thenAnswer(invocation -> {
            Pedido pedidoMock = invocation.getArgument(0);
            pedidoMock.setCliente(cliente);
            pedidoMock.setObra(obra);
            return pedidoMock;
        });

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(pedido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PedidoID"))
                .andExpect(jsonPath("$.cliente.id").value(1))
                .andExpect(jsonPath("$.obra.id").value(1))
                .andExpect(jsonPath("$.total").value(500));
    }

    private static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
}
