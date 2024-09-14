package isi.dan.msclientes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.UsuarioHabilitado;
import isi.dan.msclientes.servicios.ClienteService;
import isi.dan.msclientes.servicios.UsuarioHabilitadoService;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

	@InjectMocks
	private ClienteService clienteService;

	@Mock
	private UsuarioHabilitadoService usuarioHabilitadoService;

	@Mock
	private ClienteRepository clienteRepository;

	private Cliente cliente;
	
	private UsuarioHabilitado usuarioHabilitado1;
	
	private UsuarioHabilitado usuarioHabilitado2;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		cliente = new Cliente();
		cliente.setId(1);
		cliente.setNombre("Juan Pérez");
		cliente.setMaximoDescubierto(new BigDecimal(500));
		
		usuarioHabilitado1 = new UsuarioHabilitado();
		usuarioHabilitado1.setId(1);
		
		usuarioHabilitado2 = new UsuarioHabilitado();
		usuarioHabilitado2.setId(1);
	}

	@Test
    void testFindAll() {
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente));

        List<Cliente> clientes = clienteService.findAll();

        assertNotNull(clientes);
        assertEquals(1, clientes.size());
        assertEquals("Juan Pérez", clientes.get(0).getNombre());

        verify(clienteRepository, times(1)).findAll();
    }

	@Test
    void testFindById_found() {
        when(clienteRepository.findById(eq(1))).thenReturn(Optional.of(cliente));

        Optional<Cliente> foundCliente = clienteService.findById(1);

        assertTrue(foundCliente.isPresent());
        assertEquals("Juan Pérez", foundCliente.get().getNombre());

        verify(clienteRepository, times(1)).findById(eq(1));
    }

	@Test
    void testFindById_notFound() {
        when(clienteRepository.findById(eq(1))).thenReturn(Optional.empty());

        Optional<Cliente> foundCliente = clienteService.findById(1);

        assertFalse(foundCliente.isPresent());

        verify(clienteRepository, times(1)).findById(eq(1));
    }

	@Test
	void testSave() {
		when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

		Cliente savedCliente = clienteService.save(cliente);

		assertNotNull(savedCliente);
		assertEquals("Juan Pérez", savedCliente.getNombre());
		assertEquals(new BigDecimal(500), savedCliente.getMaximoDescubierto());

		verify(clienteRepository, times(1)).save(any(Cliente.class));
	}

	@Test
    void testUpdate() {
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente updatedCliente = clienteService.update(cliente);

        assertNotNull(updatedCliente);
        assertEquals("Juan Pérez", updatedCliente.getNombre());

        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

	@Test
	void testDeleteById() {
		clienteService.deleteById(1);

		verify(clienteRepository, times(1)).deleteById(eq(1));
	}
	
	@Test
	void testAddEnabledUser() throws ClienteNotFoundException {
	    List<UsuarioHabilitado> usuariosHabilitados = Arrays.asList(usuarioHabilitado1, usuarioHabilitado2);
	    List<Integer> usuariosHabilitadosId = Arrays.asList(1001, 1002);

	    cliente.setUsuariosHabilitados(new ArrayList<>());

	    when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
	    when(usuarioHabilitadoService.findAllById(usuariosHabilitadosId)).thenReturn(usuariosHabilitados);
	    when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

	    Cliente result = clienteService.addEnabledUser(1, usuariosHabilitadosId);

	    // Verificar que los usuarios habilitados se hayan agregado correctamente
	    assertNotNull(result);
	    assertEquals(2, result.getUsuariosHabilitados().size());
	    verify(clienteRepository, times(1)).findById(1);
	    verify(clienteRepository, times(1)).save(cliente);
	}

}
