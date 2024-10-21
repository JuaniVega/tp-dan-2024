package isi.dan.ms_productos.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import isi.dan.ms_productos.dao.CategoriaRepository;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.servicio.CategoriaService;

@SpringBootTest
class CategoriaServiceTest {

	@Mock
	private CategoriaRepository repository;

	@InjectMocks
	private CategoriaService categoriaService;

	private Categoria categoria;

	@BeforeEach
	void setUp() {
		categoria = new Categoria();
		categoria.setId(1L);
		categoria.setNombre("Categoría 1");
	}

	@Test
	void testSaveCategoria() {
		Mockito.when(repository.save(Mockito.any(Categoria.class))).thenReturn(categoria);

		Categoria savedCategoria = categoriaService.saveCategoria(categoria);

		assertNotNull(savedCategoria);
		assertEquals(Long.valueOf(1), savedCategoria.getId());
		assertEquals("Categoría 1", savedCategoria.getNombre());
		verify(repository, times(1)).save(categoria);
	}

	@Test
	void testFindAllCategorias() {
		List<Categoria> categorias = Arrays.asList(categoria);
		Mockito.when(repository.findAll()).thenReturn(categorias);

		List<Categoria> result = categoriaService.findAllCategorias();

		assertEquals(1, result.size());
		assertEquals(Long.valueOf(1), result.get(0).getId());
		verify(repository, times(1)).findAll();
	}

	@Test
	void testFindCategoriaById() throws CategoriaNotFoundException {
		Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.of(categoria));

		Categoria foundCategoria = categoriaService.findCategoriaById(1L);

		assertNotNull(foundCategoria);
		assertEquals(Long.valueOf(1), foundCategoria.getId());
		verify(repository, times(1)).findById(1L);
	}

	@Test
	void testFindCategoriaByIdNotFound() {
		Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

		assertThrows(CategoriaNotFoundException.class, () -> {
			categoriaService.findCategoriaById(1L);
		});

		verify(repository, times(1)).findById(1L);
	}

	@Test
	void testDeleteCategoriaById() {
		Mockito.doNothing().when(repository).deleteById(Mockito.anyLong());

		categoriaService.deleteCategoriaById(1L);

		verify(repository, times(1)).deleteById(1L);
	}
}
