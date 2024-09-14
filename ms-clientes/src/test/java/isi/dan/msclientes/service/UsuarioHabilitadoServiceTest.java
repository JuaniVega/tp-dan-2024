package isi.dan.msclientes.service;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isi.dan.msclientes.dao.UsuarioHabilitadoRepository;
import isi.dan.msclientes.model.UsuarioHabilitado;
import isi.dan.msclientes.servicios.UsuarioHabilitadoService;

@ExtendWith(MockitoExtension.class)
public class UsuarioHabilitadoServiceTest {
	
	@InjectMocks
	private UsuarioHabilitadoService usuarioHabilitadoService;

	@Mock
	private UsuarioHabilitadoRepository usuarioHabilitadoRepository;

	private UsuarioHabilitado usuario;
	
	private UsuarioHabilitado usuario2;

	@BeforeEach
	void setUp() {
		usuario = new UsuarioHabilitado();
		usuario.setId(1);
		usuario.setNombre("Usuario Prueba");

		usuario2 = new UsuarioHabilitado();
		usuario2.setId(2);
		usuario2.setNombre("Usuario Prueba 2");
	}

	@Test
	void testFindAll() {
		when(usuarioHabilitadoRepository.findAll()).thenReturn(Arrays.asList(usuario));

		List<UsuarioHabilitado> usuarios = usuarioHabilitadoService.findAll();

		assertNotNull(usuarios);
		assertEquals(1, usuarios.size());
		assertEquals("Usuario Prueba", usuarios.get(0).getNombre());

		verify(usuarioHabilitadoRepository, times(1)).findAll();
	}

	@Test
	void testFindById_found() {
		when(usuarioHabilitadoRepository.findById(1)).thenReturn(Optional.of(usuario));

		Optional<UsuarioHabilitado> foundUsuario = usuarioHabilitadoService.findById(1);

		assertTrue(foundUsuario.isPresent());
		assertEquals("Usuario Prueba", foundUsuario.get().getNombre());

		verify(usuarioHabilitadoRepository, times(1)).findById(1);
	}

	@Test
	void testFindById_notFound() {
		when(usuarioHabilitadoRepository.findById(1)).thenReturn(Optional.empty());

		Optional<UsuarioHabilitado> foundUsuario = usuarioHabilitadoService.findById(1);

		assertFalse(foundUsuario.isPresent());

		verify(usuarioHabilitadoRepository, times(1)).findById(1);
	}

	@Test
	void testSave() {
		when(usuarioHabilitadoRepository.save(any(UsuarioHabilitado.class))).thenReturn(usuario);

		UsuarioHabilitado savedUsuario = usuarioHabilitadoService.save(usuario);

		assertNotNull(savedUsuario);
		assertEquals("Usuario Prueba", savedUsuario.getNombre());

		verify(usuarioHabilitadoRepository, times(1)).save(any(UsuarioHabilitado.class));
	}

	@Test
	void testUpdate() {
		when(usuarioHabilitadoRepository.save(any(UsuarioHabilitado.class))).thenReturn(usuario);

		UsuarioHabilitado updatedUsuario = usuarioHabilitadoService.update(usuario);

		assertNotNull(updatedUsuario);
		assertEquals("Usuario Prueba", updatedUsuario.getNombre());

		verify(usuarioHabilitadoRepository, times(1)).save(any(UsuarioHabilitado.class));
	}

	@Test
	void testDeleteById() {
		usuarioHabilitadoService.deleteById(1);

		verify(usuarioHabilitadoRepository, times(1)).deleteById(1);
	}

	@Test
	void testFindAllById() {
		List<Integer> ids = Arrays.asList(1, 2);
		List<UsuarioHabilitado> usuarios = Arrays.asList(usuario, usuario2);

		when(usuarioHabilitadoRepository.findAllById(ids)).thenReturn(usuarios);

		List<UsuarioHabilitado> foundUsuarios = usuarioHabilitadoService.findAllById(ids);

		assertNotNull(foundUsuarios);
		assertEquals(2, foundUsuarios.size());

		verify(usuarioHabilitadoRepository, times(1)).findAllById(ids);
	}
}