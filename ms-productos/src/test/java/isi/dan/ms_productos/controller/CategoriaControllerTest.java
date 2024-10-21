package isi.dan.ms_productos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.servicio.CategoriaService;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CategoriaService categoriaService;

	@InjectMocks
	private CategoriaController categoriaController;

	@Autowired
	private ObjectMapper objectMapper;

	private Categoria categoria;

	@BeforeEach
	void setUp() {
		categoria = new Categoria();
		categoria.setId(1L);
		categoria.setNombre("Categoría 1");
	}

	@Test
	void testSaveCategoria() throws Exception {
		Mockito.when(categoriaService.saveCategoria(Mockito.any(Categoria.class))).thenReturn(categoria);

		mockMvc.perform(post("/api/categoria").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(categoria))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.nombre").value("Categoría 1"));
	}

	@Test
	void testGetAllCategorias() throws Exception {
		List<Categoria> categorias = Arrays.asList(categoria);
		Mockito.when(categoriaService.findAllCategorias()).thenReturn(categorias);

		mockMvc.perform(get("/api/categoria")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].nombre").value("Categoría 1"));
	}

	@Test
	void testGetCategoriaById() throws Exception {
		Mockito.when(categoriaService.findCategoriaById(Mockito.anyLong())).thenReturn(categoria);

		mockMvc.perform(get("/api/categoria/{id}", 1L)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.nombre").value("Categoría 1"));
	}

	@Test
	void testDeleteCategoria() throws Exception {
		Mockito.doNothing().when(categoriaService).deleteCategoriaById(Mockito.anyLong());

		mockMvc.perform(delete("/api/categoria/{id}", 1L)).andExpect(status().isNoContent());
	}
}
