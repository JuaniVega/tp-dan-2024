package isi.dan.msclientes.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.ObraNotFoundException;
import isi.dan.msclientes.exception.StateErrorException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.EstadoObraEnum;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.servicios.ClienteService;
import isi.dan.msclientes.servicios.ObraService;

class ObraServiceTest {

	@Mock
	private ClienteService clienteService;

	@Mock
	private ObraRepository obraRepository;

	@InjectMocks
	private ObraService obraService;

	private Obra obra;
	private Cliente cliente;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		cliente = new Cliente();
		cliente.setId(1);
		cliente.setMaxObrasEjecucion(2);

		obra = new Obra();
		obra.setId(1);
		obra.setCliente(cliente);
		obra.setEstado(EstadoObraEnum.PENDIENTE);
	}

	@Test
    void testFindAll() {
        when(obraRepository.findAll()).thenReturn(Arrays.asList(obra));

        List<Obra> obras = obraService.findAll();

        assertEquals(1, obras.size());
        verify(obraRepository, times(1)).findAll();
    }

	@Test
    void testFindById() {
        when(obraRepository.findById(1)).thenReturn(Optional.of(obra));

        Optional<Obra> found = obraService.findById(1);

        assertTrue(found.isPresent());
        assertEquals(obra, found.get());
        verify(obraRepository, times(1)).findById(1);
    }

	@Test
    void testSaveObra_Habilitada() {
        when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        when(obraRepository.save(any(Obra.class))).thenReturn(obra);
        when(obraRepository.countByClienteIdAndEstado(cliente.getId(), EstadoObraEnum.HABILITADA)).thenReturn(0);

        Obra savedObra = obraService.save(obra);

        assertEquals(EstadoObraEnum.HABILITADA, savedObra.getEstado());
        verify(obraRepository, times(1)).save(obra);
    }

	@Test
    void testSaveObra_Pendiente() {
        when(obraRepository.countByClienteIdAndEstado(cliente.getId(), EstadoObraEnum.HABILITADA))
                .thenReturn(cliente.getMaxObrasEjecucion());
        obra.setEstado(EstadoObraEnum.PENDIENTE);
        when(obraRepository.save(any(Obra.class))).thenReturn(obra);

        Obra savedObra = obraService.save(obra);
        
        assertNotNull(savedObra);
        assertEquals(EstadoObraEnum.PENDIENTE, savedObra.getEstado());
        verify(obraRepository, times(1)).save(obra);
    }

	@Test
	void testUpdateObra_ValidStateTransition() throws StateErrorException {
		obra.setEstado(EstadoObraEnum.HABILITADA);
		when(obraRepository.findById(obra.getId())).thenReturn(Optional.of(obra));
		when(obraRepository.save(obra)).thenReturn(obra);

		Obra updatedObra = obraService.update(obra);

		assertEquals(EstadoObraEnum.HABILITADA, updatedObra.getEstado());
		verify(obraRepository, times(1)).save(obra);
	}

	@Test
	void testUpdateObra_InvalidStateTransition() {
		obra.setEstado(EstadoObraEnum.HABILITADA);
		Obra obraFinalizada = new Obra();
		obraFinalizada.setEstado(EstadoObraEnum.FINALIZADA);
		when(obraRepository.findById(obra.getId())).thenReturn(Optional.of(obraFinalizada));
		when(obraRepository.save(any(Obra.class))).thenReturn(obra);

		Obra updatedObra = obraService.update(obra);

		assertEquals(EstadoObraEnum.FINALIZADA, updatedObra.getEstado());
		verify(obraRepository, times(1)).save(obra);
	}

	@Test
    void testAsignarCliente_ClienteNotFound() {
        when(clienteService.findById(1)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> {
            obraService.asignarCliente(1, 1);
        });
    }

	@Test
    void testAsignarCliente_ObraNotFound() {
        when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        when(obraRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ObraNotFoundException.class, () -> {
            obraService.asignarCliente(1, 1);
        });
    }

	@Test
    void testAsignarCliente_Valid() throws ClienteNotFoundException, ObraNotFoundException, StateErrorException {
        when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        when(obraRepository.findById(1)).thenReturn(Optional.of(obra));
        when(obraRepository.save(obra)).thenReturn(obra);
        when(obraRepository.countByClienteIdAndEstado(cliente.getId(), EstadoObraEnum.HABILITADA)).thenReturn(1);

        obraService.asignarCliente(1, 1);

        assertEquals(cliente, obra.getCliente());
        verify(obraRepository, times(1)).save(obra);
    }
}
