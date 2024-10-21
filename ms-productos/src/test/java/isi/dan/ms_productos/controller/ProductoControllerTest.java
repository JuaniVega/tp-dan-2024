package isi.dan.ms_productos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.ms_productos.dto.DescuentoDto;
import isi.dan.ms_productos.dto.StockUpdateDTO;
import isi.dan.ms_productos.modelo.Producto;
import isi.dan.ms_productos.servicio.EchoClientFeign;
import isi.dan.ms_productos.servicio.ProductoService;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductoService productoService;

	@MockBean
	private EchoClientFeign echoSvc;

	private Producto producto;
	private DescuentoDto descuentoDto;
	private StockUpdateDTO stockUpdate;

	@BeforeEach
	void setup() {
		producto = new Producto();
		producto.setId(1L);
		producto.setNombre("Producto Test");

		descuentoDto = new DescuentoDto();
		descuentoDto.setIdProducto(1L);
		descuentoDto.setDescuento(0.2f);

		stockUpdate = new StockUpdateDTO();
		stockUpdate.setIdProducto(1L);
		stockUpdate.setCantidad(10);
		stockUpdate.setPrecio(BigDecimal.valueOf(300));

	}

	@Test
	void testCreateProducto() throws Exception {
		Mockito.when(productoService.saveProducto(Mockito.any(Producto.class))).thenReturn(producto);

		mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(asJsonString(producto)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.nombre").value("Producto Test"));
	}

	@Test
	void testGetAllProductos() throws Exception {
		List<Producto> productos = Arrays.asList(producto);
		Mockito.when(productoService.getAllProductos()).thenReturn(productos);

		mockMvc.perform(get("/api/productos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].nombre").value("Producto Test"));
	}

	@Test
	void testGetProductoById() throws Exception {
		Mockito.when(productoService.getProductoById(1L)).thenReturn(producto);

		mockMvc.perform(get("/api/productos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.nombre").value("Producto Test"));
	}

	@Test
	void testDeleteProducto() throws Exception {
		Mockito.doNothing().when(productoService).deleteProducto(1L);

		mockMvc.perform(delete("/api/productos/1")).andExpect(status().isNoContent());
	}

	@Test
	void testEnterOrdenProvision() throws Exception {
		Mockito.when(productoService.putOrdenProvision(Mockito.any(StockUpdateDTO.class))).thenReturn(producto);

		mockMvc.perform(put("/api/productos/enter-orden").contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(stockUpdate))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.nombre").value("Producto Test"));
	}

	@Test
	void testUpdateDescuento() throws Exception {
		Mockito.when(productoService.updateDescuento(Mockito.any(DescuentoDto.class))).thenReturn(producto);

		mockMvc.perform(put("/api/productos/descuento").contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(descuentoDto))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.nombre").value("Producto Test"));
	}

	private static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}