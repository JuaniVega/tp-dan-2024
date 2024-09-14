package isi.dan.msclientes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import isi.dan.msclientes.model.UsuarioHabilitado;
import isi.dan.msclientes.servicios.UsuarioHabilitadoService;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UsuarioHabilitadoController.class)
class UsuarioHabilitadoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UsuarioHabilitadoService usuarioHabilitadoService;

	private UsuarioHabilitado usuario;

	@BeforeEach
	void setUp() {
		usuario = new UsuarioHabilitado();
		usuario.setId(1);
		usuario.setNombre("Usuario Prueba");
	}

	@Test
	void testGetAll() throws Exception {
		List<UsuarioHabilitado> usuarios = Arrays.asList(usuario);
		when(usuarioHabilitadoService.findAll()).thenReturn(usuarios);

		mockMvc.perform(get("/api/usuario-habilitado")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].nombre").value("Usuario Prueba"));

		verify(usuarioHabilitadoService, times(1)).findAll();
	}

	@Test
	void testGetById_found() throws Exception {
		when(usuarioHabilitadoService.findById(1)).thenReturn(Optional.of(usuario));

		mockMvc.perform(get("/api/usuario-habilitado/1"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.nombre").value("Usuario Prueba"));

		verify(usuarioHabilitadoService, times(1)).findById(1);
	}

	@Test
	void testGetById_notFound() throws Exception {
		when(usuarioHabilitadoService.findById(1)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/usuario-habilitado/1"))
			.andExpect(status().isNotFound());

		verify(usuarioHabilitadoService, times(1)).findById(1);
	}

	@Test
	void testCreate() throws Exception {
		when(usuarioHabilitadoService.save(any(UsuarioHabilitado.class))).thenReturn(usuario);

		mockMvc.perform(post("/api/usuario-habilitado")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Usuario Prueba\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Usuario Prueba"));

		verify(usuarioHabilitadoService, times(1)).save(any(UsuarioHabilitado.class));
	}

	@Test
	void testUpdate_found() throws Exception {
		when(usuarioHabilitadoService.findById(1)).thenReturn(Optional.of(usuario));
		when(usuarioHabilitadoService.update(any(UsuarioHabilitado.class))).thenReturn(usuario);

		mockMvc.perform(put("/api/usuario-habilitado/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Usuario Actualizado\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Usuario Prueba"));

		verify(usuarioHabilitadoService, times(1)).findById(1);
		verify(usuarioHabilitadoService, times(1)).update(any(UsuarioHabilitado.class));
	}

	@Test
	void testUpdate_notFound() throws Exception {
		when(usuarioHabilitadoService.findById(1)).thenReturn(Optional.empty());

		mockMvc.perform(put("/api/usuario-habilitado/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Usuario Actualizado\"}"))
			.andExpect(status().isNotFound());

		verify(usuarioHabilitadoService, times(1)).findById(1);
		verify(usuarioHabilitadoService, times(0)).update(any(UsuarioHabilitado.class));
	}

	@Test
	void testDelete_found() throws Exception {
		when(usuarioHabilitadoService.findById(1)).thenReturn(Optional.of(usuario));

		mockMvc.perform(delete("/api/usuario-habilitado/1"))
			.andExpect(status().isNoContent());

		verify(usuarioHabilitadoService, times(1)).findById(1);
		verify(usuarioHabilitadoService, times(1)).deleteById(1);
	}

	@Test
	void testDelete_notFound() throws Exception {
		when(usuarioHabilitadoService.findById(1)).thenReturn(Optional.empty());

		mockMvc.perform(delete("/api/usuario-habilitado/1"))
			.andExpect(status().isNotFound());

		verify(usuarioHabilitadoService, times(1)).findById(1);
		verify(usuarioHabilitadoService, times(0)).deleteById(1);
	}
}
